/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package cros.mail.chain.core;

import com.google.common.annotations.*;
import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.base.Objects.*;
import com.google.common.collect.*;
import com.google.common.primitives.*;
import com.google.common.util.concurrent.*;
import com.google.protobuf.*;

import cros.mail.chain.blockdata.InvalidWalletException;
import cros.mail.chain.blockdata.WalletProtoUtil;
import cros.mail.chain.core.TransactionDegree.*;
import cros.mail.chain.encrypt.Child;
import cros.mail.chain.encrypt.DeterKey;
import cros.mail.chain.encrypt.KeyCrypt;
import cros.mail.chain.encrypt.KeyCrypterScrypt;
import cros.mail.chain.misc.BaseTagObject;
import cros.mail.chain.misc.InterchangeRate;
import cros.mail.chain.misc.ListenerRegister;
import cros.mail.chain.misc.Threading;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.script.ChainScriptBuilder;
import cros.mail.chain.script.ChainScriptChunk;
import cros.mail.chain.signature.LocalTransactionSignature;
import cros.mail.chain.signature.MissingSignature;
import cros.mail.chain.signature.TransactionSignature;
import cros.mail.chain.wallet.AllowUnconfirmedTokenSelector;
import cros.mail.chain.wallet.*;
import cros.mail.chain.wallet.Protos.Wallet.*;
import cros.mail.chain.wallet.CrosWalletTransaction.*;
import net.jcip.annotations.*;
import org.bitcoin.protocols.payments.Protos.*;
import org.slf4j.*;
import org.spongycastle.crypto.params.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

import static com.google.common.base.Preconditions.*;

public class Wallet extends BaseTagObject
		implements Serializable, CrosChainListener, PeerProvider, KeyPackage, TransactionBlock {
	private static final Logger log = LoggerFactory.getLogger(Wallet.class);
	private static final long serialVersionUID = 2L;
	private static final int MINIMUM_BLOOM_DATA_LENGTH = 8;

	protected final ReentrantLock lock = Threading.lock("wallet");
	protected final ReentrantLock keychainLock = Threading.lock("wallet-keychain");

	@VisibleForTesting
	final Map<Sha256Hash, Transaction> pending;
	@VisibleForTesting
	final Map<Sha256Hash, Transaction> unspent;
	@VisibleForTesting
	final Map<Sha256Hash, Transaction> spent;
	@VisibleForTesting
	final Map<Sha256Hash, Transaction> dead;

	protected final Map<Sha256Hash, Transaction> transactions;

	protected final HashSet<TxOutput> myUnspents = Sets.newHashSet();

	private final LinkedHashMap<Sha256Hash, Transaction> riskDropped = new LinkedHashMap<Sha256Hash, Transaction>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Sha256Hash, Transaction> eldest) {
			return size() > 1000;
		}
	};

	@GuardedBy("keychainLock")
	protected CrosKeyChainGroup keychain;

	@GuardedBy("keychainLock")
	private Set<ChainScript> watchedScripts;

	protected final Context context;
	protected final NetworkParams params;

	@Nullable
	private Sha256Hash lastBlockSeenHash;
	private int lastBlockSeenHeight;
	private long lastBlockSeenTimeSecs;

	private transient CopyOnWriteArrayList<ListenerRegister<WalletListener>> eventListeners;

	private transient TransactionDegree.Listener txConfidenceListener;

	private transient HashSet<Sha256Hash> ignoreNextNewBlock;

	private boolean acceptRiskyTransactions;

	private int onWalletChangedSuppressions;
	private boolean insideReorg;
	private Map<Transaction, TransactionDegree.Listener.ChangeReason> confidenceChanged;
	protected volatile CrosWalletFile vFileManager;

	protected volatile TransactionSender vTransactionBroadcaster;

	private volatile long vKeyRotationTimestamp;

	protected transient TokenSelector tokenSelector = new DefaultKeySelector();

	private int version;

	private String description;

	private final HashMap<String, WalletExtension> extensions;

	private RiskAssess.Analyzer riskAnalyzer = DefaultRiskAssess.FACTORY;

	@GuardedBy("lock")
	private List<TransactionSignature> signers;

	@Nullable
	private volatile UnspentProvider vUTXOProvider;

	public Wallet(NetworkParams params) {
		this(Context.getOrCreate(params));
	}

	public Wallet(Context context) {
		this(context, new CrosKeyChainGroup(context.getParams()));
	}

	public static Wallet fromSeed(NetworkParams params, DeterSeed seed) {
		return new Wallet(params, new CrosKeyChainGroup(params, seed));
	}

	public static Wallet fromWatchingKey(NetworkParams params, DeterKey watchKey, long creationTimeSeconds) {
		return new Wallet(params, new CrosKeyChainGroup(params, watchKey, creationTimeSeconds));
	}

	public static Wallet fromWatchingKey(NetworkParams params, DeterKey watchKey) {
		return new Wallet(params, new CrosKeyChainGroup(params, watchKey));
	}

	public static Wallet fromKeys(NetworkParams params, List<ECKey> keys) {
		for (ECKey key : keys)
			checkArgument(!(key instanceof DeterKey));

		CrosKeyChainGroup group = new CrosKeyChainGroup(params);
		group.importKeys(keys);
		return new Wallet(params, group);
	}

	public Wallet(NetworkParams params, CrosKeyChainGroup crosKeyChainGroup) {
		this(Context.getOrCreate(params), crosKeyChainGroup);
	}

	public Wallet(Context context, CrosKeyChainGroup crosKeyChainGroup) {
		this.context = context;
		this.params = context.getParams();
		this.keychain = checkNotNull(crosKeyChainGroup);
		if (params.getId().equals(NetworkParams.ID_UNITTESTNET))
			this.keychain.setLookaheadSize(5);

		if (this.keychain.numKeys() == 0)
			this.keychain.createAndActivateNewHDChain();
		watchedScripts = Sets.newHashSet();
		unspent = new HashMap<Sha256Hash, Transaction>();
		spent = new HashMap<Sha256Hash, Transaction>();
		pending = new HashMap<Sha256Hash, Transaction>();
		dead = new HashMap<Sha256Hash, Transaction>();
		transactions = new HashMap<Sha256Hash, Transaction>();
		eventListeners = new CopyOnWriteArrayList<ListenerRegister<WalletListener>>();
		extensions = new HashMap<String, WalletExtension>();

		confidenceChanged = new LinkedHashMap<Transaction, TransactionDegree.Listener.ChangeReason>();
		signers = new ArrayList<TransactionSignature>();
		addTransactionSigner(new LocalTransactionSignature());
		createTransientState();
	}

	private void createTransientState() {
		ignoreNextNewBlock = new HashSet<Sha256Hash>();
		txConfidenceListener = new TransactionDegree.Listener() {
			@Override
			public void onConfidenceChanged(TransactionDegree confidence,
					TransactionDegree.Listener.ChangeReason reason) {

				if (reason == ChangeReason.SEEN_PEERS) {
					lock.lock();
					try {
						checkBalanceFuturesLocked(null);
						Transaction tx = getTransaction(confidence.getTransactionHash());
						queueOnTransactionConfidenceChanged(tx);
						maybeQueueOnWalletChanged();
					} finally {
						lock.unlock();
					}
				}
			}
		};
		acceptRiskyTransactions = false;
	}

	public NetworkParams getNetworkParameters() {
		return params;
	}

	public DeterKeyChain getActiveKeychain() {
		return keychain.getActiveKeyChain();
	}

	public void addTransactionSigner(TransactionSignature signer) {
		lock.lock();
		try {
			if (signer.isReady())
				signers.add(signer);
			else
				throw new IllegalStateException(
						"Signer instance is not ready to be added into Wallet: " + signer.getClass());
		} finally {
			lock.unlock();
		}
	}

	public List<TransactionSignature> getTransactionSigners() {
		lock.lock();
		try {
			return ImmutableList.copyOf(signers);
		} finally {
			lock.unlock();
		}
	}

	public DeterKey currentKey(CrosKeyChain.KeyPurpose purpose) {
		keychainLock.lock();
		try {
			maybeUpgradeToHD();
			return keychain.currentKey(purpose);
		} finally {
			keychainLock.unlock();
		}
	}

	public DeterKey currentReceiveKey() {
		return currentKey(CrosKeyChain.KeyPurpose.RECEIVE_FUNDS);
	}

	public Address currentAddress(CrosKeyChain.KeyPurpose purpose) {
		keychainLock.lock();
		try {
			maybeUpgradeToHD();
			return keychain.currentAddress(purpose);
		} finally {
			keychainLock.unlock();
		}
	}

	public Address currentReceiveAddress() {
		return currentAddress(CrosKeyChain.KeyPurpose.RECEIVE_FUNDS);
	}

	public DeterKey freshKey(CrosKeyChain.KeyPurpose purpose) {
		return freshKeys(purpose, 1).get(0);
	}

	public List<DeterKey> freshKeys(CrosKeyChain.KeyPurpose purpose, int numberOfKeys) {
		List<DeterKey> keys;
		keychainLock.lock();
		try {
			maybeUpgradeToHD();
			keys = keychain.freshKeys(purpose, numberOfKeys);
		} finally {
			keychainLock.unlock();
		}

		saveNow();
		return keys;
	}

	public DeterKey freshReceiveKey() {
		return freshKey(CrosKeyChain.KeyPurpose.RECEIVE_FUNDS);
	}

	public Address freshAddress(CrosKeyChain.KeyPurpose purpose) {
		Address key;
		keychainLock.lock();
		try {
			key = keychain.freshAddress(purpose);
		} finally {
			keychainLock.unlock();
		}
		saveNow();
		return key;
	}

	public Address freshReceiveAddress() {
		return freshAddress(CrosKeyChain.KeyPurpose.RECEIVE_FUNDS);
	}

	public List<ECKey> getIssuedReceiveKeys() {
		keychainLock.lock();
		try {
			return keychain.getActiveKeyChain().getIssuedReceiveKeys();
		} finally {
			keychainLock.unlock();
		}
	}

	public List<Address> getIssuedReceiveAddresses() {
		final List<ECKey> keys = getIssuedReceiveKeys();
		List<Address> addresses = new ArrayList<Address>(keys.size());
		for (ECKey key : keys)
			addresses.add(key.toAddress(getParams()));
		return addresses;
	}

	public void upgradeToDeterministic(@Nullable KeyParameter aesKey) throws DeterUpgradePassword {
		keychainLock.lock();
		try {
			keychain.upgradeToDeterministic(vKeyRotationTimestamp, aesKey);
		} finally {
			keychainLock.unlock();
		}
	}

	public boolean isDeterministicUpgradeRequired() {
		keychainLock.lock();
		try {
			return keychain.isDeterministicUpgradeRequired();
		} finally {
			keychainLock.unlock();
		}
	}

	private void maybeUpgradeToHD() throws DeterUpgradePassword {
		maybeUpgradeToHD(null);
	}

	@GuardedBy("keychainLock")
	private void maybeUpgradeToHD(@Nullable KeyParameter aesKey) throws DeterUpgradePassword {
		checkState(keychainLock.isHeldByCurrentThread());
		if (keychain.isDeterministicUpgradeRequired()) {
			log.info("Upgrade to HD wallets is required, attempting to do so.");
			try {
				upgradeToDeterministic(aesKey);
			} catch (DeterUpgradePassword e) {
				log.error("Failed to auto upgrade due to encryption. You should call wallet.upgradeToDeterministic "
						+ "with the users AES key to avoid this error.");
				throw e;
			}
		}
	}

	public List<ChainScript> getWatchedScripts() {
		keychainLock.lock();
		try {
			return new ArrayList<ChainScript>(watchedScripts);
		} finally {
			keychainLock.unlock();
		}
	}

	public boolean removeKey(ECKey key) {
		keychainLock.lock();
		try {
			return keychain.removeImportedKey(key);
		} finally {
			keychainLock.unlock();
		}
	}

	public int getKeychainSize() {
		keychainLock.lock();
		try {
			return keychain.numKeys();
		} finally {
			keychainLock.unlock();
		}
	}

	public List<ECKey> getImportedKeys() {
		keychainLock.lock();
		try {
			return keychain.getImportedKeys();
		} finally {
			keychainLock.unlock();
		}
	}

	public Address getChangeAddress() {
		return currentAddress(CrosKeyChain.KeyPurpose.CHANGE);
	}

	@Deprecated
	public boolean addKey(ECKey key) {
		return importKey(key);
	}

	public boolean importKey(ECKey key) {
		return importKeys(Lists.newArrayList(key)) == 1;
	}

	@Deprecated
	public int addKeys(List<ECKey> keys) {
		return importKeys(keys);
	}

	public int importKeys(final List<ECKey> keys) {

		checkNoDeterministicKeys(keys);
		int result;
		keychainLock.lock();
		try {
			result = keychain.importKeys(keys);
		} finally {
			keychainLock.unlock();
		}
		saveNow();
		return result;
	}

	private void checkNoDeterministicKeys(List<ECKey> keys) {

		for (ECKey key : keys)
			if (key instanceof DeterKey)
				throw new IllegalArgumentException("Cannot import HD keys back into the wallet");
	}

	public int importKeysAndEncrypt(final List<ECKey> keys, CharSequence password) {
		keychainLock.lock();
		try {
			checkNotNull(getKeyCrypter(), "Wallet is not encrypted");
			return importKeysAndEncrypt(keys, getKeyCrypter().deriveKey(password));
		} finally {
			keychainLock.unlock();
		}
	}

	public int importKeysAndEncrypt(final List<ECKey> keys, KeyParameter aesKey) {
		keychainLock.lock();
		try {
			checkNoDeterministicKeys(keys);
			return keychain.importKeysAndEncrypt(keys, aesKey);
		} finally {
			keychainLock.unlock();
		}
	}

	public void addAndActivateHDChain(DeterKeyChain chain) {
		keychainLock.lock();
		try {
			keychain.addAndActivateHDChain(chain);
		} finally {
			keychainLock.unlock();
		}
	}

	public void setKeychainLookaheadSize(int lookaheadSize) {
		keychainLock.lock();
		try {
			keychain.setLookaheadSize(lookaheadSize);
		} finally {
			keychainLock.unlock();
		}
	}

	public int getKeychainLookaheadSize() {
		keychainLock.lock();
		try {
			return keychain.getLookaheadSize();
		} finally {
			keychainLock.unlock();
		}
	}

	public void setKeychainLookaheadThreshold(int num) {
		keychainLock.lock();
		try {
			maybeUpgradeToHD();
			keychain.setLookaheadThreshold(num);
		} finally {
			keychainLock.unlock();
		}
	}

	public int getKeychainLookaheadThreshold() {
		keychainLock.lock();
		try {
			maybeUpgradeToHD();
			return keychain.getLookaheadThreshold();
		} finally {
			keychainLock.unlock();
		}
	}

	public DeterKey getWatchingKey() {
		keychainLock.lock();
		try {
			maybeUpgradeToHD();
			return keychain.getActiveKeyChain().getWatchingKey();
		} finally {
			keychainLock.unlock();
		}
	}

	public boolean isWatching() {
		keychainLock.lock();
		try {
			maybeUpgradeToHD();
			return keychain.isWatching();
		} finally {
			keychainLock.unlock();
		}
	}

	public boolean isAddressWatched(Address address) {
		ChainScript chainScript = ChainScriptBuilder.createOutputScript(address);
		return isWatchedScript(chainScript);
	}

	public boolean addWatchedAddress(final Address address) {
		long now = Utils.currentTimeMillis() / 1000;
		return addWatchedAddresses(Lists.newArrayList(address), now) == 1;
	}

	public boolean addWatchedAddress(final Address address, long creationTime) {
		return addWatchedAddresses(Lists.newArrayList(address), creationTime) == 1;
	}

	public int addWatchedAddresses(final List<Address> addresses, long creationTime) {
		List<ChainScript> chainScripts = Lists.newArrayList();

		for (Address address : addresses) {
			ChainScript chainScript = ChainScriptBuilder.createOutputScript(address);
			chainScript.setCreationTimeSeconds(creationTime);
			chainScripts.add(chainScript);
		}

		return addWatchedScripts(chainScripts);
	}

	public int addWatchedScripts(final List<ChainScript> chainScripts) {
		int added = 0;
		keychainLock.lock();
		try {
			for (final ChainScript chainScript : chainScripts) {

				if (watchedScripts.contains(chainScript))
					watchedScripts.remove(chainScript);
				if (chainScript.getCreationTimeSeconds() == 0)
					log.warn(
							"Adding a script to the wallet with a creation time of zero, this will disable the checkpointing optimization!    {}",
							chainScript);
				watchedScripts.add(chainScript);
				added++;
			}
		} finally {
			keychainLock.unlock();
		}
		if (added > 0) {
			queueOnScriptsChanged(chainScripts, true);
			saveNow();
		}
		return added;
	}

	public boolean removeWatchedAddress(final Address address) {
		return removeWatchedAddresses(ImmutableList.of(address));
	}

	public boolean removeWatchedAddresses(final List<Address> addresses) {
		List<ChainScript> chainScripts = Lists.newArrayList();

		for (Address address : addresses) {
			ChainScript chainScript = ChainScriptBuilder.createOutputScript(address);
			chainScripts.add(chainScript);
		}

		return removeWatchedScripts(chainScripts);
	}

	public boolean removeWatchedScripts(final List<ChainScript> chainScripts) {
		lock.lock();
		try {
			for (final ChainScript chainScript : chainScripts) {
				if (!watchedScripts.contains(chainScript))
					continue;

				watchedScripts.remove(chainScript);
			}

			queueOnScriptsChanged(chainScripts, false);
			saveNow();
			return true;
		} finally {
			lock.unlock();
		}
	}

	public List<Address> getWatchedAddresses() {
		keychainLock.lock();
		try {
			List<Address> addresses = new LinkedList<Address>();
			for (ChainScript chainScript : watchedScripts)
				if (chainScript.isSentToAddress())
					addresses.add(chainScript.getToAddress(params));
			return addresses;
		} finally {
			keychainLock.unlock();
		}
	}

	@Override
	@Nullable
	public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
		keychainLock.lock();
		try {
			return keychain.findKeyFromPubHash(pubkeyHash);
		} finally {
			keychainLock.unlock();
		}
	}

	public boolean hasKey(ECKey key) {
		keychainLock.lock();
		try {
			return keychain.hasKey(key);
		} finally {
			keychainLock.unlock();
		}
	}

	@Override
	public boolean isPubKeyHashMine(byte[] pubkeyHash) {
		return findKeyFromPubHash(pubkeyHash) != null;
	}

	@Override
	public boolean isWatchedScript(ChainScript chainScript) {
		keychainLock.lock();
		try {
			return watchedScripts.contains(chainScript);
		} finally {
			keychainLock.unlock();
		}
	}

	@Override
	@Nullable
	public ECKey findKeyFromPubKey(byte[] pubkey) {
		keychainLock.lock();
		try {
			return keychain.findKeyFromPubKey(pubkey);
		} finally {
			keychainLock.unlock();
		}
	}

	@Override
	public boolean isPubKeyMine(byte[] pubkey) {
		return findKeyFromPubKey(pubkey) != null;
	}

	@Nullable
	@Override
	public RedeemBlockData findRedeemDataFromScriptHash(byte[] payToScriptHash) {
		keychainLock.lock();
		try {
			return keychain.findRedeemDataFromScriptHash(payToScriptHash);
		} finally {
			keychainLock.unlock();
		}
	}

	@Override
	public boolean isPayToScriptHashMine(byte[] payToScriptHash) {
		return findRedeemDataFromScriptHash(payToScriptHash) != null;
	}

	private void markKeysAsUsed(Transaction tx) {
		keychainLock.lock();
		try {
			for (TxOutput o : tx.getOutputs()) {
				try {
					ChainScript chainScript = o.getScriptPubKey();
					if (chainScript.isSentToRawPubKey()) {
						byte[] pubkey = chainScript.getPubKey();
						keychain.markPubKeyAsUsed(pubkey);
					} else if (chainScript.isSentToAddress()) {
						byte[] pubkeyHash = chainScript.getPubKeyHash();
						keychain.markPubKeyHashAsUsed(pubkeyHash);
					} else if (chainScript.isPayToScriptHash() && keychain.isMarried()) {
						Address a = Address.fromP2SHScript(tx.getParams(), chainScript);
						keychain.markP2SHAddressAsUsed(a);
					}
				} catch (ScriptException e) {

					log.warn("Could not parse tx output script: {}", e.toString());
				}
			}
		} finally {
			keychainLock.unlock();
		}
	}

	public DeterSeed getKeyChainSeed() {
		keychainLock.lock();
		try {
			DeterSeed seed = keychain.getActiveKeyChain().getSeed();
			if (seed == null)
				throw new ECKey.MissingPrivateKeyException();
			return seed;
		} finally {
			keychainLock.unlock();
		}
	}

	public DeterKey getKeyByPath(List<Child> path) {
		keychainLock.lock();
		try {
			maybeUpgradeToHD();
			return keychain.getActiveKeyChain().getKeyByPath(path, false);
		} finally {
			keychainLock.unlock();
		}
	}

	public void encrypt(CharSequence password) {
		keychainLock.lock();
		try {
			final KeyCrypterScrypt scrypt = new KeyCrypterScrypt();
			keychain.encrypt(scrypt, scrypt.deriveKey(password));
		} finally {
			keychainLock.unlock();
		}
		saveNow();
	}

	public void encrypt(KeyCrypt keyCrypt, KeyParameter aesKey) {
		keychainLock.lock();
		try {
			keychain.encrypt(keyCrypt, aesKey);
		} finally {
			keychainLock.unlock();
		}
		saveNow();
	}

	public void decrypt(CharSequence password) {
		keychainLock.lock();
		try {
			final KeyCrypt crypter = keychain.getKeyCrypter();
			checkState(crypter != null, "Not encrypted");
			keychain.decrypt(crypter.deriveKey(password));
		} finally {
			keychainLock.unlock();
		}
		saveNow();
	}

	public void decrypt(KeyParameter aesKey) {
		keychainLock.lock();
		try {
			keychain.decrypt(aesKey);
		} finally {
			keychainLock.unlock();
		}
		saveNow();
	}

	public boolean checkPassword(CharSequence password) {
		keychainLock.lock();
		try {
			return keychain.checkPassword(password);
		} finally {
			keychainLock.unlock();
		}
	}

	public boolean checkAESKey(KeyParameter aesKey) {
		keychainLock.lock();
		try {
			return keychain.checkAESKey(aesKey);
		} finally {
			keychainLock.unlock();
		}
	}

	@Nullable
	public KeyCrypt getKeyCrypter() {
		keychainLock.lock();
		try {
			return keychain.getKeyCrypter();
		} finally {
			keychainLock.unlock();
		}
	}

	public EncryptionType getEncryptionType() {
		keychainLock.lock();
		try {
			KeyCrypt crypter = keychain.getKeyCrypter();
			if (crypter != null)
				return crypter.getUnderstoodEncryptionType();
			else
				return EncryptionType.UNENCRYPTED;
		} finally {
			keychainLock.unlock();
		}
	}

	public boolean isEncrypted() {
		return getEncryptionType() != EncryptionType.UNENCRYPTED;
	}

	public void changeEncryptionPassword(CharSequence currentPassword, CharSequence newPassword) {
		keychainLock.lock();
		try {
			decrypt(currentPassword);
			encrypt(newPassword);
		} finally {
			keychainLock.unlock();
		}
	}

	public void changeEncryptionKey(KeyCrypt keyCrypt, KeyParameter currentAesKey, KeyParameter newAesKey) {
		keychainLock.lock();
		try {
			decrypt(currentAesKey);
			encrypt(keyCrypt, newAesKey);
		} finally {
			keychainLock.unlock();
		}
	}

	public List<Protos.Key> serializeKeychainToProtobuf() {
		keychainLock.lock();
		try {
			return keychain.serializeToProtobuf();
		} finally {
			keychainLock.unlock();
		}
	}

	public void saveToFile(File temp, File destFile) throws IOException {
		FileOutputStream stream = null;
		lock.lock();
		try {
			stream = new FileOutputStream(temp);
			saveToFileStream(stream);

			stream.flush();
			stream.getFD().sync();
			stream.close();
			stream = null;
			if (Utils.isWindows()) {

				File canonical = destFile.getCanonicalFile();
				if (canonical.exists() && !canonical.delete())
					throw new IOException("Failed to delete canonical wallet file for replacement with autosave");
				if (temp.renameTo(canonical))
					return;
				throw new IOException("Failed to rename " + temp + " to " + canonical);
			} else if (!temp.renameTo(destFile)) {
				throw new IOException("Failed to rename " + temp + " to " + destFile);
			}
		} catch (RuntimeException e) {
			log.error("Failed whilst saving wallet", e);
			throw e;
		} finally {
			lock.unlock();
			if (stream != null) {
				stream.close();
			}
			if (temp.exists()) {
				log.warn("Temp file still exists after failed save.");
			}
		}
	}

	public void saveToFile(File f) throws IOException {
		File directory = f.getAbsoluteFile().getParentFile();
		File temp = File.createTempFile("wallet", null, directory);
		saveToFile(temp, f);
	}

	public void setAcceptRiskyTransactions(boolean acceptRiskyTransactions) {
		lock.lock();
		try {
			this.acceptRiskyTransactions = acceptRiskyTransactions;
		} finally {
			lock.unlock();
		}
	}

	public boolean doesAcceptRiskyTransactions() {
		lock.lock();
		try {
			return acceptRiskyTransactions;
		} finally {
			lock.unlock();
		}
	}

	public void setRiskAnalyzer(RiskAssess.Analyzer analyzer) {
		lock.lock();
		try {
			this.riskAnalyzer = checkNotNull(analyzer);
		} finally {
			lock.unlock();
		}
	}

	public RiskAssess.Analyzer getRiskAnalyzer() {
		lock.lock();
		try {
			return riskAnalyzer;
		} finally {
			lock.unlock();
		}
	}

	public CrosWalletFile autosaveToFile(File f, long delayTime, TimeUnit timeUnit,
			@Nullable CrosWalletFile.Listener eventListener) {
		lock.lock();
		try {
			checkState(vFileManager == null, "Already auto saving this wallet.");
			CrosWalletFile manager = new CrosWalletFile(this, f, delayTime, timeUnit);
			if (eventListener != null)
				manager.setListener(eventListener);
			vFileManager = manager;
			return manager;
		} finally {
			lock.unlock();
		}
	}

	public void shutdownAutosaveAndWait() {
		lock.lock();
		try {
			CrosWalletFile files = vFileManager;
			vFileManager = null;
			checkState(files != null, "Auto saving not enabled.");
			files.shutdownAndWait();
		} finally {
			lock.unlock();
		}
	}

	protected void saveLater() {
		CrosWalletFile files = vFileManager;
		if (files != null)
			files.saveLater();
	}

	protected void saveNow() {
		CrosWalletFile files = vFileManager;
		if (files != null) {
			try {
				files.saveNow();
			} catch (IOException e) {

				log.error("Failed to save wallet to disk!", e);
				Thread.UncaughtExceptionHandler handler = Threading.uncaughtExceptionHandler;
				if (handler != null)
					handler.uncaughtException(Thread.currentThread(), e);
			}
		}
	}

	public void saveToFileStream(OutputStream f) throws IOException {
		lock.lock();
		try {
			new WalletProtoUtil().writeWallet(this, f);
		} finally {
			lock.unlock();
		}
	}

	public NetworkParams getParams() {
		return params;
	}

	public Context getContext() {
		return context;
	}

	public static Wallet loadFromFile(File file, @Nullable WalletExtension... walletExtensions)
			throws InvalidWalletException {
		try {
			FileInputStream stream = null;
			try {
				stream = new FileInputStream(file);
				return loadFromFileStream(stream, walletExtensions);
			} finally {
				if (stream != null)
					stream.close();
			}
		} catch (IOException e) {
			throw new InvalidWalletException("Could not open file", e);
		}
	}

	public boolean isConsistent() {
		try {
			isConsistentOrThrow();
			return true;
		} catch (IllegalStateException x) {
			log.error(x.getMessage());
			try {
				log.error(toString());
			} catch (RuntimeException x2) {
				log.error("Printing inconsistent wallet failed", x2);
			}
			return false;
		}
	}

	public void isConsistentOrThrow() throws IllegalStateException {
		lock.lock();
		try {
			Set<Transaction> transactions = getTransactions(true);

			Set<Sha256Hash> hashes = new HashSet<Sha256Hash>();
			for (Transaction tx : transactions) {
				hashes.add(tx.getHash());
			}

			int size1 = transactions.size();
			if (size1 != hashes.size()) {
				throw new IllegalStateException("Two transactions with same hash");
			}

			int size2 = unspent.size() + spent.size() + pending.size() + dead.size();
			if (size1 != size2) {
				throw new IllegalStateException("Inconsistent wallet sizes: " + size1 + ", " + size2);
			}

			for (Transaction tx : unspent.values()) {
				if (!tx.isConsistent(this, false)) {
					throw new IllegalStateException("Inconsistent unspent tx: " + tx.getHashAsString());
				}
			}

			for (Transaction tx : spent.values()) {
				if (!tx.isConsistent(this, true)) {
					throw new IllegalStateException("Inconsistent spent tx: " + tx.getHashAsString());
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public static Wallet loadFromFileStream(InputStream stream, @Nullable WalletExtension... walletExtensions)
			throws InvalidWalletException {
		Wallet wallet = new WalletProtoUtil().readWallet(stream, walletExtensions);
		if (!wallet.isConsistent()) {
			log.error("Loaded an inconsistent wallet");
		}
		return wallet;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		createTransientState();
	}

	@Override
	public boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredDataBlock block,
			CrosChain.NewBlockType blockType, int relativityOffset) throws VeriException {
		lock.lock();
		try {
			Transaction tx = transactions.get(txHash);
			if (tx == null) {
				tx = riskDropped.get(txHash);
				if (tx != null) {

					log.info("Risk analysis dropped tx {} but was included in block anyway", tx.getHash());
				} else {

					return false;
				}
			}
			receive(tx, block, blockType, relativityOffset);
			return true;
		} finally {
			lock.unlock();
		}
	}

	public void receivePending(Transaction tx, @Nullable List<Transaction> dependencies, boolean overrideIsRelevant)
			throws VeriException {

		lock.lock();
		try {
			tx.verify();

			EnumSet<Pool> containingPools = getContainingPools(tx);
			if (!containingPools.equals(EnumSet.noneOf(Pool.class))) {
				log.debug("Received tx we already saw in a block or created ourselves: " + tx.getHashAsString());
				return;
			}

			if (!overrideIsRelevant && !isPendingTransactionRelevant(tx))
				return;
			if (isTransactionRisky(tx, dependencies) && !acceptRiskyTransactions) {

				riskDropped.put(tx.getHash(), tx);
				log.warn("There are now {} risk dropped transactions being kept in memory", riskDropped.size());
				return;
			}
			Coin valueSentToMe = tx.getValueSentToMe(this);
			Coin valueSentFromMe = tx.getValueSentFromMe(this);
			if (log.isInfoEnabled()) {
				log.info(String.format(Locale.US,
						"Received a pending transaction %s that spends %s from our own wallet," + " and sends us %s",
						tx.getHashAsString(), valueSentFromMe.toFriendlyString(), valueSentToMe.toFriendlyString()));
			}
			if (tx.getConfidence().getSource().equals(TransactionDegree.Source.UNKNOWN)) {
				log.warn("Wallet received transaction with an unknown source. Consider tagging it!");
			}

			commitTx(tx);
		} finally {
			lock.unlock();
		}

	}

	public boolean isTransactionRisky(Transaction tx, @Nullable List<Transaction> dependencies) {
		lock.lock();
		try {
			if (dependencies == null)
				dependencies = ImmutableList.of();
			RiskAssess analysis = riskAnalyzer.create(this, tx, dependencies);
			RiskAssess.Result result = analysis.analyze();
			if (result != RiskAssess.Result.OK) {
				log.warn("Pending transaction was considered risky: {}\n{}", analysis, tx);
				return true;
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	public void receivePending(Transaction tx, @Nullable List<Transaction> dependencies) throws VeriException {
		receivePending(tx, dependencies, false);
	}

	public boolean isPendingTransactionRelevant(Transaction tx) throws ScriptException {
		lock.lock();
		try {

			EnumSet<Pool> containingPools = getContainingPools(tx);
			if (!containingPools.equals(EnumSet.noneOf(Pool.class))) {
				log.debug("Received tx we already saw in a block or created ourselves: " + tx.getHashAsString());
				return false;
			}

			if (!isTransactionRelevant(tx)) {
				log.debug("Received tx that isn't relevant to this wallet, discarding.");
				return false;
			}
			return true;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isTransactionRelevant(Transaction tx) throws ScriptException {
		lock.lock();
		try {
			return tx.getValueSentFromMe(this).signum() > 0 || tx.getValueSentToMe(this).signum() > 0
					|| checkForDoubleSpendAgainstPending(tx, false);
		} finally {
			lock.unlock();
		}
	}

	private boolean checkForDoubleSpendAgainstPending(Transaction tx, boolean takeAction) {
		checkState(lock.isHeldByCurrentThread());

		HashSet<TxOutPoint> outpoints = new HashSet<TxOutPoint>();
		for (TxInput input : tx.getInputs()) {
			outpoints.add(input.getOutpoint());
		}

		LinkedList<Transaction> doubleSpentTxns = Lists.newLinkedList();
		for (Transaction p : pending.values()) {
			for (TxInput input : p.getInputs()) {

				TxOutPoint outpoint = input.getOutpoint();
				if (outpoints.contains(outpoint)) {

					if (!doubleSpentTxns.isEmpty() && doubleSpentTxns.getLast() == p)
						continue;
					doubleSpentTxns.add(p);
				}
			}
		}
		if (takeAction && !doubleSpentTxns.isEmpty()) {
			killTx(tx, doubleSpentTxns);
		}
		return !doubleSpentTxns.isEmpty();
	}

	@Override
	public void receiveFromBlock(Transaction tx, StoredDataBlock block, CrosChain.NewBlockType blockType,
			int relativityOffset) throws VeriException {
		lock.lock();
		try {
			receive(tx, block, blockType, relativityOffset);
		} finally {
			lock.unlock();
		}
	}

	private void receive(Transaction tx, StoredDataBlock block, CrosChain.NewBlockType blockType, int relativityOffset)
			throws VeriException {

		checkState(lock.isHeldByCurrentThread());
		Coin prevBalance = getBalance();
		Sha256Hash txHash = tx.getHash();
		boolean bestChain = blockType == CrosChain.NewBlockType.BEST_CHAIN;
		boolean sideChain = blockType == CrosChain.NewBlockType.SIDE_CHAIN;

		Coin valueSentFromMe = tx.getValueSentFromMe(this);
		Coin valueSentToMe = tx.getValueSentToMe(this);
		Coin valueDifference = valueSentToMe.subtract(valueSentFromMe);

		log.info("Received tx{} for {}: {} [{}] in block {}", sideChain ? " on a side chain" : "",
				valueDifference.toFriendlyString(), tx.getHashAsString(), relativityOffset,
				block != null ? block.getHeader().getHash() : "(unit test)");

		markKeysAsUsed(tx);

		onWalletChangedSuppressions++;

		{
			Transaction tmp = transactions.get(tx.getHash());
			if (tmp != null)
				tx = tmp;
		}

		boolean wasPending = pending.remove(txHash) != null;
		if (wasPending)
			log.info("  <-pending");

		if (bestChain) {
			if (wasPending) {

				for (TxOutput output : tx.getOutputs()) {
					final TxInput spentBy = output.getSpentBy();
					if (spentBy != null) {
						checkState(myUnspents.add(output));
						spentBy.disconnect();
					}
				}
			}
			processTxFromBestChain(tx, wasPending);
		} else {
			checkState(sideChain);

			if (wasPending) {

				addWalletTransaction(Pool.PENDING, tx);
				log.info("  ->pending");
			} else {

				Sha256Hash hash = tx.getHash();
				if (!unspent.containsKey(hash) && !spent.containsKey(hash)) {

					commitTx(tx);
				}
			}
		}

		if (block != null) {

			tx.setBlockAppearance(block, bestChain, relativityOffset);
			if (bestChain) {

				ignoreNextNewBlock.add(txHash);
			}
		}

		onWalletChangedSuppressions--;

		if (bestChain) {

			confidenceChanged.put(tx, TransactionDegree.Listener.ChangeReason.TYPE);
		} else {
			maybeQueueOnWalletChanged();
		}

		if (!insideReorg && bestChain) {
			Coin newBalance = getBalance();
			log.info("Balance is now: " + newBalance.toFriendlyString());
			if (!wasPending) {
				int diff = valueDifference.signum();

				if (diff > 0) {
					queueOnCoinsReceived(tx, prevBalance, newBalance);
				} else if (diff < 0) {
					queueOnCoinsSent(tx, prevBalance, newBalance);
				}
			}
			checkBalanceFuturesLocked(newBalance);
		}

		informConfidenceListenersIfNotReorganizing();
		isConsistentOrThrow();
		saveNow();
	}

	private void informConfidenceListenersIfNotReorganizing() {
		if (insideReorg)
			return;
		for (Map.Entry<Transaction, TransactionDegree.Listener.ChangeReason> entry : confidenceChanged.entrySet()) {
			final Transaction tx = entry.getKey();
			tx.getConfidence().queueListeners(entry.getValue());
			queueOnTransactionConfidenceChanged(tx);
		}
		confidenceChanged.clear();
	}

	@Override
	public void notifyNewBestBlock(StoredDataBlock block) throws VeriException {

		Sha256Hash newBlockHash = block.getHeader().getHash();
		if (newBlockHash.equals(getLastBlockSeenHash()))
			return;
		lock.lock();
		try {

			setLastBlockSeenHash(newBlockHash);
			setLastBlockSeenHeight(block.getHeight());
			setLastBlockSeenTimeSecs(block.getHeader().getTimeSeconds());

			Set<Transaction> transactions = getTransactions(true);
			for (Transaction tx : transactions) {
				if (ignoreNextNewBlock.contains(tx.getHash())) {

					ignoreNextNewBlock.remove(tx.getHash());
				} else {
					TransactionDegree confidence = tx.getConfidence();
					if (confidence.getConfidenceType() == ConfidenceType.BUILDING) {

						if (confidence.incrementDepthInBlocks() > context.getEventHorizon())
							confidence.clearBroadcastBy();
						confidenceChanged.put(tx, TransactionDegree.Listener.ChangeReason.DEPTH);
					}
				}
			}

			informConfidenceListenersIfNotReorganizing();
			maybeQueueOnWalletChanged();

			saveLater();
		} finally {
			lock.unlock();
		}
	}

	private void processTxFromBestChain(Transaction tx, boolean forceAddToPool) throws VeriException {
		checkState(lock.isHeldByCurrentThread());
		checkState(!pending.containsKey(tx.getHash()));

		boolean isDeadCoinbase = tx.isCoinBase() && dead.containsKey(tx.getHash());
		if (isDeadCoinbase) {

			log.info("  coinbase tx <-dead: confidence {}", tx.getHashAsString(),
					tx.getConfidence().getConfidenceType().name());
			dead.remove(tx.getHash());
		}

		updateForSpends(tx, true);

		boolean hasOutputsToMe = tx.getValueSentToMe(this, true).signum() > 0;
		if (hasOutputsToMe) {

			if (tx.isEveryOwnedOutputSpent(this)) {
				log.info("  tx {} ->spent (by pending)", tx.getHashAsString());
				addWalletTransaction(Pool.SPENT, tx);
			} else {
				log.info("  tx {} ->unspent", tx.getHashAsString());
				addWalletTransaction(Pool.UNSPENT, tx);
			}
		} else if (tx.getValueSentFromMe(this).signum() > 0) {

			log.info("  tx {} ->spent", tx.getHashAsString());
			addWalletTransaction(Pool.SPENT, tx);
		} else if (forceAddToPool) {

			log.info("  tx {} ->spent (manually added)", tx.getHashAsString());
			addWalletTransaction(Pool.SPENT, tx);
		}

		checkForDoubleSpendAgainstPending(tx, true);
	}

	private void updateForSpends(Transaction tx, boolean fromChain) throws VeriException {
		checkState(lock.isHeldByCurrentThread());
		if (fromChain)
			checkState(!pending.containsKey(tx.getHash()));
		for (TxInput input : tx.getInputs()) {
			TxInput.ConnectionResult result = input.connect(unspent, TxInput.ConnectMode.ABORT_ON_CONFLICT);
			if (result == TxInput.ConnectionResult.NO_SUCH_TX) {

				result = input.connect(spent, TxInput.ConnectMode.ABORT_ON_CONFLICT);
				if (result == TxInput.ConnectionResult.NO_SUCH_TX) {

					result = input.connect(pending, TxInput.ConnectMode.ABORT_ON_CONFLICT);
					if (result == TxInput.ConnectionResult.NO_SUCH_TX) {

						continue;
					}
				}
			}

			TxOutput output = checkNotNull(input.getConnectedOutput());
			if (result == TxInput.ConnectionResult.ALREADY_SPENT) {
				if (fromChain) {

				} else {

					log.warn("Saw two pending transactions double spend each other");
					log.warn("  offending input is input {}", tx.getInputs().indexOf(input));
					log.warn("{}: {}", tx.getHash(), Utils.HEX.encode(tx.unsafeBitcoinSerialize()));
					Transaction other = output.getSpentBy().getParentTransaction();
					log.warn("{}: {}", other.getHash(), Utils.HEX.encode(other.unsafeBitcoinSerialize()));
				}
			} else if (result == TxInput.ConnectionResult.SUCCESS) {

				Transaction connected = checkNotNull(input.getOutpoint().fromTx);
				log.info("  marked {} as spent by {}", input.getOutpoint(), tx.getHashAsString());
				maybeMovePool(connected, "prevtx");

				if (output.isMineOrWatched(this)) {
					checkState(myUnspents.remove(output));
				}
			}
		}

		for (Transaction pendingTx : pending.values()) {
			for (TxInput input : pendingTx.getInputs()) {
				TxInput.ConnectionResult result = input.connect(tx, TxInput.ConnectMode.ABORT_ON_CONFLICT);
				if (fromChain) {

					checkState(result != TxInput.ConnectionResult.ALREADY_SPENT);
				}
				if (result == TxInput.ConnectionResult.SUCCESS) {
					log.info("Connected pending tx input {}:{}", pendingTx.getHashAsString(),
							pendingTx.getInputs().indexOf(input));

					if (myUnspents.remove(input.getConnectedOutput()))
						log.info("Removed from UNSPENTS: {}", input.getConnectedOutput());
				}
			}
		}
		if (!fromChain) {
			maybeMovePool(tx, "pendingtx");
		} else {

		}
	}

	private void killTx(@Nullable Transaction overridingTx, List<Transaction> killedTx) {
		LinkedList<Transaction> work = new LinkedList<Transaction>(killedTx);
		while (!work.isEmpty()) {
			final Transaction tx = work.poll();
			log.warn("TX {} killed{}", tx.getHashAsString(),
					overridingTx != null ? " by " + overridingTx.getHashAsString() : "");
			log.warn("Disconnecting each input and moving connected transactions.");

			pending.remove(tx.getHash());
			unspent.remove(tx.getHash());
			spent.remove(tx.getHash());
			addWalletTransaction(Pool.DEAD, tx);
			for (TxInput deadInput : tx.getInputs()) {
				Transaction connected = deadInput.getOutpoint().fromTx;
				if (connected == null)
					continue;
				if (connected.getConfidence().getConfidenceType() != ConfidenceType.DEAD) {
					checkState(myUnspents.add(deadInput.getConnectedOutput()));
					log.info("Added to UNSPENTS: {} in {}", deadInput.getConnectedOutput(),
							deadInput.getConnectedOutput().getParentTransaction().getHash());
				}
				deadInput.disconnect();
				maybeMovePool(connected, "kill");
			}
			tx.getConfidence().setOverridingTransaction(overridingTx);
			confidenceChanged.put(tx, TransactionDegree.Listener.ChangeReason.TYPE);

			for (TxOutput deadOutput : tx.getOutputs()) {
				if (myUnspents.remove(deadOutput))
					log.info("XX Removed from UNSPENTS: {}", deadOutput);
				TxInput connected = deadOutput.getSpentBy();
				if (connected == null)
					continue;
				final Transaction parentTransaction = connected.getParentTransaction();
				log.info("This death invalidated dependent tx {}", parentTransaction.getHash());
				work.push(parentTransaction);
			}
		}
		if (overridingTx == null)
			return;
		log.warn("Now attempting to connect the inputs of the overriding transaction.");
		for (TxInput input : overridingTx.getInputs()) {
			TxInput.ConnectionResult result = input.connect(unspent, TxInput.ConnectMode.DISCONNECT_ON_CONFLICT);
			if (result == TxInput.ConnectionResult.SUCCESS) {
				maybeMovePool(input.getOutpoint().fromTx, "kill");
				myUnspents.remove(input.getConnectedOutput());
				log.info("Removing from UNSPENTS: {}", input.getConnectedOutput());
			} else {
				result = input.connect(spent, TxInput.ConnectMode.DISCONNECT_ON_CONFLICT);
				if (result == TxInput.ConnectionResult.SUCCESS) {
					maybeMovePool(input.getOutpoint().fromTx, "kill");
					myUnspents.remove(input.getConnectedOutput());
					log.info("Removing from UNSPENTS: {}", input.getConnectedOutput());
				}
			}
		}
	}

	private void maybeMovePool(Transaction tx, String context) {
		checkState(lock.isHeldByCurrentThread());
		if (tx.isEveryOwnedOutputSpent(this)) {

			if (unspent.remove(tx.getHash()) != null) {
				if (log.isInfoEnabled()) {
					log.info("  {} {} <-unspent ->spent", tx.getHashAsString(), context);
				}
				spent.put(tx.getHash(), tx);
			}
		} else {
			if (spent.remove(tx.getHash()) != null) {
				if (log.isInfoEnabled()) {
					log.info("  {} {} <-spent ->unspent", tx.getHashAsString(), context);
				}
				unspent.put(tx.getHash(), tx);
			}
		}
	}

	public boolean maybeCommitTx(Transaction tx) throws VeriException {
		tx.verify();
		lock.lock();
		try {
			if (pending.containsKey(tx.getHash()))
				return false;
			log.info("commitTx of {}", tx.getHashAsString());
			Coin balance = getBalance();
			tx.setUpdateTime(Utils.now());

			Coin valueSentToMe = Coin.ZERO;
			for (TxOutput o : tx.getOutputs()) {
				if (!o.isMineOrWatched(this))
					continue;
				valueSentToMe = valueSentToMe.add(o.getValue());
			}

			updateForSpends(tx, false);

			log.info("->pending: {}", tx.getHashAsString());
			tx.getConfidence().setConfidenceType(ConfidenceType.PENDING);
			confidenceChanged.put(tx, TransactionDegree.Listener.ChangeReason.TYPE);
			addWalletTransaction(Pool.PENDING, tx);
			if (log.isInfoEnabled())
				log.info("Estimated balance is now: {}", getBalance(BalanceType.ESTIMATED).toFriendlyString());

			markKeysAsUsed(tx);
			try {
				Coin valueSentFromMe = tx.getValueSentFromMe(this);
				Coin newBalance = balance.add(valueSentToMe).subtract(valueSentFromMe);
				if (valueSentToMe.signum() > 0) {
					checkBalanceFuturesLocked(null);
					queueOnCoinsReceived(tx, balance, newBalance);
				}
				if (valueSentFromMe.signum() > 0)
					queueOnCoinsSent(tx, balance, newBalance);

				maybeQueueOnWalletChanged();
			} catch (ScriptException e) {

				throw new RuntimeException(e);
			}

			isConsistentOrThrow();
			informConfidenceListenersIfNotReorganizing();
			saveNow();
		} finally {
			lock.unlock();
		}
		return true;
	}

	public void commitTx(Transaction tx) throws VeriException {
		checkArgument(maybeCommitTx(tx), "commitTx called on the same transaction twice");
	}

	public void addEventListener(WalletListener listener) {
		addEventListener(listener, Threading.USER_THREAD);
	}

	public void addEventListener(WalletListener listener, Executor executor) {

		eventListeners.add(new ListenerRegister<WalletListener>(listener, executor));
		keychain.addEventListener(listener, executor);
	}

	public boolean removeEventListener(WalletListener listener) {
		keychain.removeEventListener(listener);
		return ListenerRegister.removeFromList(listener, eventListeners);
	}

	private void queueOnTransactionConfidenceChanged(final Transaction tx) {
		checkState(lock.isHeldByCurrentThread());
		for (final ListenerRegister<WalletListener> registration : eventListeners) {
			if (registration.executor == Threading.SAME_THREAD) {
				registration.listener.onTransactionConfidenceChanged(this, tx);
			} else {
				registration.executor.execute(new Runnable() {
					@Override
					public void run() {
						registration.listener.onTransactionConfidenceChanged(Wallet.this, tx);
					}
				});
			}
		}
	}

	protected void maybeQueueOnWalletChanged() {

		checkState(lock.isHeldByCurrentThread());
		checkState(onWalletChangedSuppressions >= 0);
		if (onWalletChangedSuppressions > 0)
			return;
		for (final ListenerRegister<WalletListener> registration : eventListeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onWalletChanged(Wallet.this);
				}
			});
		}
	}

	protected void queueOnCoinsReceived(final Transaction tx, final Coin balance, final Coin newBalance) {
		checkState(lock.isHeldByCurrentThread());
		for (final ListenerRegister<WalletListener> registration : eventListeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onCoinsReceived(Wallet.this, tx, balance, newBalance);
				}
			});
		}
	}

	protected void queueOnCoinsSent(final Transaction tx, final Coin prevBalance, final Coin newBalance) {
		checkState(lock.isHeldByCurrentThread());
		for (final ListenerRegister<WalletListener> registration : eventListeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onCoinsSent(Wallet.this, tx, prevBalance, newBalance);
				}
			});
		}
	}

	protected void queueOnReorganize() {
		checkState(lock.isHeldByCurrentThread());
		checkState(insideReorg);
		for (final ListenerRegister<WalletListener> registration : eventListeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onReorganize(Wallet.this);
				}
			});
		}
	}

	protected void queueOnScriptsChanged(final List<ChainScript> chainScripts, final boolean isAddingScripts) {
		for (final ListenerRegister<WalletListener> registration : eventListeners) {
			registration.executor.execute(new Runnable() {
				@Override
				public void run() {
					registration.listener.onScriptsChanged(Wallet.this, chainScripts, isAddingScripts);
				}
			});
		}
	}

	public Set<Transaction> getTransactions(boolean includeDead) {
		lock.lock();
		try {
			Set<Transaction> all = new HashSet<Transaction>();
			all.addAll(unspent.values());
			all.addAll(spent.values());
			all.addAll(pending.values());
			if (includeDead)
				all.addAll(dead.values());
			return all;
		} finally {
			lock.unlock();
		}
	}

	public Iterable<CrosWalletTransaction> getWalletTransactions() {
		lock.lock();
		try {
			Set<CrosWalletTransaction> all = new HashSet<CrosWalletTransaction>();
			addWalletTransactionsToSet(all, Pool.UNSPENT, unspent.values());
			addWalletTransactionsToSet(all, Pool.SPENT, spent.values());
			addWalletTransactionsToSet(all, Pool.DEAD, dead.values());
			addWalletTransactionsToSet(all, Pool.PENDING, pending.values());
			return all;
		} finally {
			lock.unlock();
		}
	}

	private static void addWalletTransactionsToSet(Set<CrosWalletTransaction> txs, Pool poolType,
			Collection<Transaction> pool) {
		for (Transaction tx : pool) {
			txs.add(new CrosWalletTransaction(poolType, tx));
		}
	}

	public void addWalletTransaction(CrosWalletTransaction wtx) {
		lock.lock();
		try {
			addWalletTransaction(wtx.getPool(), wtx.getTransaction());
		} finally {
			lock.unlock();
		}
	}

	private void addWalletTransaction(Pool pool, Transaction tx) {
		checkState(lock.isHeldByCurrentThread());
		transactions.put(tx.getHash(), tx);
		switch (pool) {
		case UNSPENT:
			checkState(unspent.put(tx.getHash(), tx) == null);
			break;
		case SPENT:
			checkState(spent.put(tx.getHash(), tx) == null);
			break;
		case PENDING:
			checkState(pending.put(tx.getHash(), tx) == null);
			break;
		case DEAD:
			checkState(dead.put(tx.getHash(), tx) == null);
			break;
		default:
			throw new RuntimeException("Unknown wallet transaction type " + pool);
		}
		if (pool == Pool.UNSPENT || pool == Pool.PENDING) {
			for (TxOutput output : tx.getOutputs()) {
				if (output.isAvailableForSpending() && output.isMineOrWatched(this))
					myUnspents.add(output);
			}
		}

		tx.getConfidence().addEventListener(txConfidenceListener, Threading.SAME_THREAD);
	}

	public List<Transaction> getTransactionsByTime() {
		return getRecentTransactions(0, false);
	}

	public List<Transaction> getRecentTransactions(int numTransactions, boolean includeDead) {
		lock.lock();
		try {
			checkArgument(numTransactions >= 0);

			int size = getPoolSize(Pool.UNSPENT) + getPoolSize(Pool.SPENT) + getPoolSize(Pool.PENDING);
			if (numTransactions > size || numTransactions == 0) {
				numTransactions = size;
			}
			ArrayList<Transaction> all = new ArrayList<Transaction>(getTransactions(includeDead));

			Collections.sort(all, Transaction.SORT_TX_BY_UPDATE_TIME);
			if (numTransactions == all.size()) {
				return all;
			} else {
				all.subList(numTransactions, all.size()).clear();
				return all;
			}
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	public Transaction getTransaction(Sha256Hash hash) {
		lock.lock();
		try {
			return transactions.get(hash);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Map<Sha256Hash, Transaction> getTransactionPool(Pool pool) {
		lock.lock();
		try {
			switch (pool) {
			case UNSPENT:
				return unspent;
			case SPENT:
				return spent;
			case PENDING:
				return pending;
			case DEAD:
				return dead;
			default:
				throw new RuntimeException("Unknown wallet transaction type " + pool);
			}
		} finally {
			lock.unlock();
		}
	}

	public void reset() {
		lock.lock();
		try {
			clearTransactions();
			lastBlockSeenHash = null;
			lastBlockSeenHeight = -1;
			lastBlockSeenTimeSecs = 0;
			saveLater();
			maybeQueueOnWalletChanged();
		} finally {
			lock.unlock();
		}
	}

	public void clearTransactions(int fromHeight) {
		lock.lock();
		try {
			if (fromHeight == 0) {
				clearTransactions();
				saveLater();
			} else {
				throw new UnsupportedOperationException();
			}
		} finally {
			lock.unlock();
		}
	}

	private void clearTransactions() {
		unspent.clear();
		spent.clear();
		pending.clear();
		dead.clear();
		transactions.clear();
		myUnspents.clear();
	}

	public List<TxOutput> getWatchedOutputs(boolean excludeImmatureCoinbases) {
		lock.lock();
		keychainLock.lock();
		try {
			LinkedList<TxOutput> candidates = Lists.newLinkedList();
			for (Transaction tx : Iterables.concat(unspent.values(), pending.values())) {
				if (excludeImmatureCoinbases && !tx.isMature())
					continue;
				for (TxOutput output : tx.getOutputs()) {
					if (!output.isAvailableForSpending())
						continue;
					try {
						ChainScript scriptPubKey = output.getScriptPubKey();
						if (!watchedScripts.contains(scriptPubKey))
							continue;
						candidates.add(output);
					} catch (ScriptException e) {

					}
				}
			}
			return candidates;
		} finally {
			keychainLock.unlock();
			lock.unlock();
		}
	}

	public void cleanup() {
		lock.lock();
		try {
			boolean dirty = false;
			for (Iterator<Transaction> i = pending.values().iterator(); i.hasNext();) {
				Transaction tx = i.next();
				if (isTransactionRisky(tx, null) && !acceptRiskyTransactions) {
					log.debug("Found risky transaction {} in wallet during cleanup.", tx.getHashAsString());
					if (!tx.isAnyOutputSpent()) {

						for (TxInput input : tx.getInputs()) {
							TxOutput output = input.getConnectedOutput();
							if (output == null)
								continue;
							if (output.isMineOrWatched(this))
								checkState(myUnspents.add(output));
							input.disconnect();
						}
						for (TxOutput output : tx.getOutputs())
							myUnspents.remove(output);

						i.remove();
						transactions.remove(tx.getHash());
						dirty = true;
						log.info("Removed transaction {} from pending pool during cleanup.", tx.getHashAsString());
					} else {
						log.info(
								"Cannot remove transaction {} from pending pool during cleanup, as it's already spent partially.",
								tx.getHashAsString());
					}
				}
			}
			if (dirty) {
				isConsistentOrThrow();
				saveLater();
				if (log.isInfoEnabled())
					log.info("Estimated balance is now: {}", getBalance(BalanceType.ESTIMATED).toFriendlyString());
			}
		} finally {
			lock.unlock();
		}
	}

	EnumSet<Pool> getContainingPools(Transaction tx) {
		lock.lock();
		try {
			EnumSet<Pool> result = EnumSet.noneOf(Pool.class);
			Sha256Hash txHash = tx.getHash();
			if (unspent.containsKey(txHash)) {
				result.add(Pool.UNSPENT);
			}
			if (spent.containsKey(txHash)) {
				result.add(Pool.SPENT);
			}
			if (pending.containsKey(txHash)) {
				result.add(Pool.PENDING);
			}
			if (dead.containsKey(txHash)) {
				result.add(Pool.DEAD);
			}
			return result;
		} finally {
			lock.unlock();
		}
	}

	int getPoolSize(CrosWalletTransaction.Pool pool) {
		lock.lock();
		try {
			switch (pool) {
			case UNSPENT:
				return unspent.size();
			case SPENT:
				return spent.size();
			case PENDING:
				return pending.size();
			case DEAD:
				return dead.size();
			}
			throw new RuntimeException("Unreachable");
		} finally {
			lock.unlock();
		}
	}

	List<TxOutput> getUnspents() {
		lock.lock();
		try {
			return new ArrayList<TxOutput>(myUnspents);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public String toString() {
		return toString(false, true, true, null);
	}

	public String toString(boolean includePrivateKeys, boolean includeTransactions, boolean includeExtensions,
			@Nullable AbstractChain chain) {
		lock.lock();
		keychainLock.lock();
		try {
			StringBuilder builder = new StringBuilder();
			Coin estimatedBalance = getBalance(BalanceType.ESTIMATED);
			Coin availableBalance = getBalance(BalanceType.AVAILABLE_SPENDABLE);
			builder.append(String.format(Locale.US, "Wallet containing %s BTC (spendable: %s BTC) in:%n",
					estimatedBalance.toPlainString(), availableBalance.toPlainString()));
			builder.append(String.format(Locale.US, "  %d pending transactions%n", pending.size()));
			builder.append(String.format(Locale.US, "  %d unspent transactions%n", unspent.size()));
			builder.append(String.format(Locale.US, "  %d spent transactions%n", spent.size()));
			builder.append(String.format(Locale.US, "  %d dead transactions%n", dead.size()));
			final Date lastBlockSeenTime = getLastBlockSeenTime();
			final String lastBlockSeenTimeStr = lastBlockSeenTime == null ? "time unknown"
					: lastBlockSeenTime.toString();
			builder.append(String.format(Locale.US, "Last seen best block: %d (%s): %s%n", getLastBlockSeenHeight(),
					lastBlockSeenTimeStr, getLastBlockSeenHash()));
			final KeyCrypt crypter = keychain.getKeyCrypter();
			if (crypter != null)
				builder.append(String.format(Locale.US, "Encryption: %s%n", crypter));
			if (isWatching())
				builder.append("Wallet is watching.\n");

			builder.append("\nKeys:\n");
			builder.append("Earliest creation time: ").append(Utils.dateTimeFormat(getEarliestKeyCreationTime() * 1000))
					.append('\n');
			final Date keyRotationTime = getKeyRotationTime();
			if (keyRotationTime != null)
				builder.append("Key rotation time: ").append(Utils.dateTimeFormat(keyRotationTime)).append('\n');
			builder.append(keychain.toString(includePrivateKeys));

			if (!watchedScripts.isEmpty()) {
				builder.append("\nWatched scripts:\n");
				for (ChainScript chainScript : watchedScripts) {
					builder.append("  ").append(chainScript).append("\n");
				}
			}

			if (includeTransactions) {

				if (pending.size() > 0) {
					builder.append("\n>>> PENDING:\n");
					toStringHelper(builder, pending, chain, Transaction.SORT_TX_BY_UPDATE_TIME);
				}
				if (unspent.size() > 0) {
					builder.append("\n>>> UNSPENT:\n");
					toStringHelper(builder, unspent, chain, Transaction.SORT_TX_BY_HEIGHT);
				}
				if (spent.size() > 0) {
					builder.append("\n>>> SPENT:\n");
					toStringHelper(builder, spent, chain, Transaction.SORT_TX_BY_HEIGHT);
				}
				if (dead.size() > 0) {
					builder.append("\n>>> DEAD:\n");
					toStringHelper(builder, dead, chain, Transaction.SORT_TX_BY_UPDATE_TIME);
				}
			}
			if (includeExtensions && extensions.size() > 0) {
				builder.append("\n>>> EXTENSIONS:\n");
				for (WalletExtension extension : extensions.values()) {
					builder.append(extension).append("\n\n");
				}
			}
			return builder.toString();
		} finally {
			keychainLock.unlock();
			lock.unlock();
		}
	}

	private void toStringHelper(StringBuilder builder, Map<Sha256Hash, Transaction> transactionMap,
			@Nullable AbstractChain chain, @Nullable Comparator<Transaction> sortOrder) {
		checkState(lock.isHeldByCurrentThread());

		final Collection<Transaction> txns;
		if (sortOrder != null) {
			txns = new TreeSet<Transaction>(sortOrder);
			txns.addAll(transactionMap.values());
		} else {
			txns = transactionMap.values();
		}

		for (Transaction tx : txns) {
			try {
				builder.append("Sends ");
				builder.append(tx.getValueSentFromMe(this).toFriendlyString());
				builder.append(" and receives ");
				builder.append(tx.getValueSentToMe(this).toFriendlyString());
				builder.append(", total value ");
				builder.append(tx.getValue(this).toFriendlyString());
				builder.append(".\n");
			} catch (ScriptException e) {

			}
			builder.append(tx.toString(chain));
		}
	}

	public Collection<Transaction> getPendingTransactions() {
		lock.lock();
		try {
			return Collections.unmodifiableCollection(pending.values());
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long getEarliestKeyCreationTime() {
		keychainLock.lock();
		try {
			long earliestTime = keychain.getEarliestKeyCreationTime();
			for (ChainScript chainScript : watchedScripts)
				earliestTime = Math.min(chainScript.getCreationTimeSeconds(), earliestTime);
			if (earliestTime == Long.MAX_VALUE)
				return Utils.currentTimeSeconds();
			return earliestTime;
		} finally {
			keychainLock.unlock();
		}
	}

	@Nullable
	public Sha256Hash getLastBlockSeenHash() {
		lock.lock();
		try {
			return lastBlockSeenHash;
		} finally {
			lock.unlock();
		}
	}

	public void setLastBlockSeenHash(@Nullable Sha256Hash lastBlockSeenHash) {
		lock.lock();
		try {
			this.lastBlockSeenHash = lastBlockSeenHash;
		} finally {
			lock.unlock();
		}
	}

	public void setLastBlockSeenHeight(int lastBlockSeenHeight) {
		lock.lock();
		try {
			this.lastBlockSeenHeight = lastBlockSeenHeight;
		} finally {
			lock.unlock();
		}
	}

	public void setLastBlockSeenTimeSecs(long timeSecs) {
		lock.lock();
		try {
			lastBlockSeenTimeSecs = timeSecs;
		} finally {
			lock.unlock();
		}
	}

	public long getLastBlockSeenTimeSecs() {
		lock.lock();
		try {
			return lastBlockSeenTimeSecs;
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	public Date getLastBlockSeenTime() {
		final long secs = getLastBlockSeenTimeSecs();
		if (secs == 0)
			return null;
		else
			return new Date(secs * 1000);
	}

	public int getLastBlockSeenHeight() {
		lock.lock();
		try {
			return lastBlockSeenHeight;
		} finally {
			lock.unlock();
		}
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public enum BalanceType {

		ESTIMATED,

		AVAILABLE,

		ESTIMATED_SPENDABLE,

		AVAILABLE_SPENDABLE
	}

	@Deprecated
	public Coin getWatchedBalance() {
		return getBalance();
	}

	@Deprecated
	public Coin getWatchedBalance(TokenSelector selector) {
		return getBalance(selector);
	}

	public Coin getBalance() {
		return getBalance(BalanceType.AVAILABLE);
	}

	public Coin getBalance(BalanceType balanceType) {
		lock.lock();
		try {
			if (balanceType == BalanceType.AVAILABLE || balanceType == BalanceType.AVAILABLE_SPENDABLE) {
				List<TxOutput> candidates = calculateAllSpendCandidates(true,
						balanceType == BalanceType.AVAILABLE_SPENDABLE);
				TokenSelection selection = tokenSelector.select(NetworkParams.MAX_MONEY, candidates);
				return selection.valueGathered;
			} else if (balanceType == BalanceType.ESTIMATED || balanceType == BalanceType.ESTIMATED_SPENDABLE) {
				List<TxOutput> all = calculateAllSpendCandidates(false, balanceType == BalanceType.ESTIMATED_SPENDABLE);
				Coin value = Coin.ZERO;
				for (TxOutput out : all)
					value = value.add(out.getValue());
				return value;
			} else {
				throw new AssertionError("Unknown balance type");
			}
		} finally {
			lock.unlock();
		}
	}

	public Coin getBalance(TokenSelector selector) {
		lock.lock();
		try {
			checkNotNull(selector);
			List<TxOutput> candidates = calculateAllSpendCandidates(true, false);
			TokenSelection selection = selector.select(params.getMaxMoney(), candidates);
			return selection.valueGathered;
		} finally {
			lock.unlock();
		}
	}

	private static class BalanceFutureRequest {
		public SettableFuture<Coin> future;
		public Coin value;
		public BalanceType type;
	}

	@GuardedBy("lock")
	private List<BalanceFutureRequest> balanceFutureRequests = Lists.newLinkedList();

	public ListenableFuture<Coin> getBalanceFuture(final Coin value, final BalanceType type) {
		lock.lock();
		try {
			final SettableFuture<Coin> future = SettableFuture.create();
			final Coin current = getBalance(type);
			if (current.compareTo(value) >= 0) {

				future.set(current);
			} else {

				BalanceFutureRequest req = new BalanceFutureRequest();
				req.future = future;
				req.value = value;
				req.type = type;
				balanceFutureRequests.add(req);
			}
			return future;
		} finally {
			lock.unlock();
		}
	}

	@SuppressWarnings("FieldAccessNotGuarded")
	private void checkBalanceFuturesLocked(@Nullable Coin avail) {
		checkState(lock.isHeldByCurrentThread());
		Coin estimated = null;
		final ListIterator<BalanceFutureRequest> it = balanceFutureRequests.listIterator();
		while (it.hasNext()) {
			final BalanceFutureRequest req = it.next();
			Coin val = null;
			if (req.type == BalanceType.AVAILABLE) {
				if (avail == null)
					avail = getBalance(BalanceType.AVAILABLE);
				if (avail.compareTo(req.value) < 0)
					continue;
				val = avail;
			} else if (req.type == BalanceType.ESTIMATED) {
				if (estimated == null)
					estimated = getBalance(BalanceType.ESTIMATED);
				if (estimated.compareTo(req.value) < 0)
					continue;
				val = estimated;
			}

			it.remove();
			final Coin v = checkNotNull(val);

			Threading.USER_THREAD.execute(new Runnable() {
				@Override
				public void run() {
					req.future.set(v);
				}
			});
		}
	}

	public static class SendResult {

		public Transaction tx;

		public ListenableFuture<Transaction> broadcastComplete;

		public TransactionSend broadcast;
	}

	public enum MissingSigsMode {

		USE_OP_ZERO,

		USE_DUMMY_SIG,

		THROW
	}

	public static class SendRequest {

		public Transaction tx;

		public boolean emptyWallet = false;

		public Address changeAddress = null;

		public Coin fee = null;

		public Coin feePerKb = DEFAULT_FEE_PER_KB;

		public static Coin DEFAULT_FEE_PER_KB = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;

		public boolean ensureMinRequiredFee = true;

		public boolean signInputs = true;

		public KeyParameter aesKey = null;

		public TokenSelector tokenSelector = null;

		public boolean shuffleOutputs = true;

		public MissingSigsMode missingSigsMode = MissingSigsMode.THROW;

		public InterchangeRate interchangeRate = null;

		public String memo = null;

		private boolean completed;

		private SendRequest() {
		}

		public static SendRequest to(Address destination, Coin value) {
			SendRequest req = new SendRequest();
			final NetworkParams parameters = destination.getParameters();
			checkNotNull(parameters, "Address is for an unknown network");
			req.tx = new Transaction(parameters);
			req.tx.addOutput(value, destination);
			return req;
		}

		public static SendRequest to(NetworkParams params, ECKey destination, Coin value) {
			SendRequest req = new SendRequest();
			req.tx = new Transaction(params);
			req.tx.addOutput(value, destination);
			return req;
		}

		public static SendRequest forTx(Transaction tx) {
			SendRequest req = new SendRequest();
			req.tx = tx;
			return req;
		}

		public static SendRequest emptyWallet(Address destination) {
			SendRequest req = new SendRequest();
			final NetworkParams parameters = destination.getParameters();
			checkNotNull(parameters, "Address is for an unknown network");
			req.tx = new Transaction(parameters);
			req.tx.addOutput(Coin.ZERO, destination);
			req.emptyWallet = true;
			return req;
		}

		public SendRequest fromPaymentDetails(PaymentDetails paymentDetails) {
			if (paymentDetails.hasMemo())
				this.memo = paymentDetails.getMemo();
			return this;
		}

		@Override
		public String toString() {

			ToStringHelper helper = Objects.toStringHelper(this).omitNullValues();
			helper.add("emptyWallet", emptyWallet);
			helper.add("changeAddress", changeAddress);
			helper.add("fee", fee);
			helper.add("feePerKb", feePerKb);
			helper.add("ensureMinRequiredFee", ensureMinRequiredFee);
			helper.add("signInputs", signInputs);
			helper.add("aesKey", aesKey != null ? "set" : null);
			helper.add("tokenSelector", tokenSelector);
			helper.add("shuffleOutputs", shuffleOutputs);
			return helper.toString();
		}
	}

	public Transaction createSend(Address address, Coin value) throws InsufficientFundException {
		SendRequest req = SendRequest.to(address, value);
		if (params.getId().equals(NetworkParams.ID_UNITTESTNET))
			req.shuffleOutputs = false;
		completeTx(req);
		return req.tx;
	}

	public Transaction sendCoinsOffline(SendRequest request) throws InsufficientFundException {
		lock.lock();
		try {
			completeTx(request);
			commitTx(request.tx);
			return request.tx;
		} finally {
			lock.unlock();
		}
	}

	public SendResult sendCoins(TransactionSender broadcaster, Address to, Coin value)
			throws InsufficientFundException {
		SendRequest request = SendRequest.to(to, value);
		return sendCoins(broadcaster, request);
	}

	public SendResult sendCoins(TransactionSender broadcaster, SendRequest request) throws InsufficientFundException {

		checkState(!lock.isHeldByCurrentThread());

		Transaction tx = sendCoinsOffline(request);
		SendResult result = new SendResult();
		result.tx = tx;

		result.broadcast = broadcaster.broadcastTransaction(tx);
		result.broadcastComplete = result.broadcast.future();
		return result;
	}

	public SendResult sendCoins(SendRequest request) throws InsufficientFundException {
		TransactionSender broadcaster = vTransactionBroadcaster;
		checkState(broadcaster != null, "No transaction broadcaster is configured");
		return sendCoins(broadcaster, request);
	}

	public Transaction sendCoins(Peer peer, SendRequest request) throws InsufficientFundException {
		Transaction tx = sendCoinsOffline(request);
		peer.sendMessage(tx);
		return tx;
	}

	public static class CompletionException extends RuntimeException {
	}

	public static class DustySendRequested extends CompletionException {
	}

	public static class MultipleOpReturnRequested extends CompletionException {
	}

	public static class CouldNotAdjustDownwards extends CompletionException {
	}

	public static class ExceededMaxTransactionSize extends CompletionException {
	}

	public void completeTx(SendRequest req) throws InsufficientFundException {
		lock.lock();
		try {
			checkArgument(!req.completed, "Given SendRequest has already been completed.");

			Coin value = Coin.ZERO;
			for (TxOutput output : req.tx.getOutputs()) {
				value = value.add(output.getValue());
			}

			log.info("Completing send tx with {} outputs totalling {} (not including fees)", req.tx.getOutputs().size(),
					value.toFriendlyString());

			Coin totalInput = Coin.ZERO;
			for (TxInput input : req.tx.getInputs())
				if (input.getConnectedOutput() != null)
					totalInput = totalInput.add(input.getConnectedOutput().getValue());
				else
					log.warn(
							"SendRequest transaction already has inputs but we don't know how much they are worth - they will be added to fee.");
			value = value.subtract(totalInput);

			List<TxInput> originalInputs = new ArrayList<TxInput>(req.tx.getInputs());
			int opReturnCount = 0;

			boolean needAtLeastReferenceFee = false;
			if (req.ensureMinRequiredFee && !req.emptyWallet) {
				for (TxOutput output : req.tx.getOutputs()) {
					if (output.getValue().compareTo(Coin.CENT) < 0) {
						needAtLeastReferenceFee = true;
						if (output.getValue().compareTo(output.getMinNonDustValue()) < 0) {
							if (output.getScriptPubKey().isOpReturn()) {
								++opReturnCount;
								continue;
							} else {
								throw new DustySendRequested();
							}
						}
						break;
					}
				}
			}

			if (opReturnCount > 1) {
				throw new MultipleOpReturnRequested();
			}

			List<TxOutput> candidates = calculateAllSpendCandidates(true, req.missingSigsMode == MissingSigsMode.THROW);

			TokenSelection bestCoinSelection;
			TxOutput bestChangeOutput = null;
			if (!req.emptyWallet) {

				FeeCalculation feeCalculation;
				feeCalculation = calculateFee(req, value, originalInputs, needAtLeastReferenceFee, candidates);
				bestCoinSelection = feeCalculation.bestCoinSelection;
				bestChangeOutput = feeCalculation.bestChangeOutput;
			} else {

				checkState(req.tx.getOutputs().size() == 1, "Empty wallet TX must have a single output only.");
				TokenSelector selector = req.tokenSelector == null ? tokenSelector : req.tokenSelector;
				bestCoinSelection = selector.select(params.getMaxMoney(), candidates);
				candidates = null;
				req.tx.getOutput(0).setValue(bestCoinSelection.valueGathered);
				log.info("  emptying {}", bestCoinSelection.valueGathered.toFriendlyString());
			}

			for (TxOutput output : bestCoinSelection.gathered)
				req.tx.addInput(output);

			if (req.ensureMinRequiredFee && req.emptyWallet) {
				final Coin baseFee = req.fee == null ? Coin.ZERO : req.fee;
				final Coin feePerKb = req.feePerKb == null ? Coin.ZERO : req.feePerKb;
				Transaction tx = req.tx;
				if (!adjustOutputDownwardsForFee(tx, bestCoinSelection, baseFee, feePerKb))
					throw new CouldNotAdjustDownwards();
			}

			if (bestChangeOutput != null) {
				req.tx.addOutput(bestChangeOutput);
				log.info("  with {} change", bestChangeOutput.getValue().toFriendlyString());
			}

			if (req.shuffleOutputs)
				req.tx.shuffleOutputs();

			if (req.signInputs)
				signTransaction(req);

			final int size = req.tx.unsafeBitcoinSerialize().length;
			if (size > Transaction.MAX_STANDARD_TX_SIZE)
				throw new ExceededMaxTransactionSize();

			final Coin calculatedFee = req.tx.getFee();
			if (calculatedFee != null)
				log.info("  with a fee of {}/kB, {} for {} bytes",
						calculatedFee.multiply(1000).divide(size).toFriendlyString(), calculatedFee.toFriendlyString(),
						size);

			req.tx.getConfidence().setSource(TransactionDegree.Source.SELF);

			req.tx.setPurpose(Transaction.Purpose.USER_PAYMENT);

			req.tx.setExchangeRate(req.interchangeRate);
			req.tx.setMemo(req.memo);
			req.completed = true;
			req.fee = calculatedFee;
			log.info("  completed: {}", req.tx);
		} finally {
			lock.unlock();
		}
	}

	public void signTransaction(SendRequest req) {
		lock.lock();
		try {
			Transaction tx = req.tx;
			List<TxInput> inputs = tx.getInputs();
			List<TxOutput> outputs = tx.getOutputs();
			checkState(inputs.size() > 0);
			checkState(outputs.size() > 0);

			KeyPackage maybeDecryptingKeyBag = new DecryptKeyPack(this, req.aesKey);

			int numInputs = tx.getInputs().size();
			for (int i = 0; i < numInputs; i++) {
				TxInput txIn = tx.getInput(i);
				if (txIn.getConnectedOutput() == null) {

					continue;
				}

				try {

					txIn.getScriptSig().correctlySpends(tx, i, txIn.getConnectedOutput().getScriptPubKey());
					log.warn(
							"Input {} already correctly spends output, assuming SIGHASH type used will be safe and skipping signing.",
							i);
					continue;
				} catch (ScriptException e) {

				}

				ChainScript scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();
				RedeemBlockData redeemBlockData = txIn.getConnectedRedeemData(maybeDecryptingKeyBag);
				checkNotNull(redeemBlockData, "Transaction exists in wallet that we cannot redeem: %s",
						txIn.getOutpoint().getHash());
				txIn.setScriptSig(scriptPubKey.createEmptyInputScript(redeemBlockData.keys.get(0), redeemBlockData.redeemScript));
			}

			TransactionSignature.ProposedTransaction proposal = new TransactionSignature.ProposedTransaction(tx);
			for (TransactionSignature signer : signers) {
				if (!signer.signInputs(proposal, maybeDecryptingKeyBag))
					log.info("{} returned false for the tx", signer.getClass().getName());
			}

			new MissingSignature(req.missingSigsMode).signInputs(proposal, maybeDecryptingKeyBag);
		} finally {
			lock.unlock();
		}
	}

	private boolean adjustOutputDownwardsForFee(Transaction tx, TokenSelection tokenSelection, Coin baseFee,
			Coin feePerKb) {
		TxOutput output = tx.getOutput(0);

		int size = tx.bitcoinSerialize().length;
		size += estimateBytesForSigning(tokenSelection);
		Coin fee = baseFee.add(feePerKb.multiply((size / 1000) + 1));
		output.setValue(output.getValue().subtract(fee));

		if (output.getValue().compareTo(Coin.CENT) < 0 && fee.compareTo(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE) < 0)
			output.setValue(output.getValue().subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.subtract(fee)));
		return output.getMinNonDustValue().compareTo(output.getValue()) <= 0;
	}

	public List<TxOutput> calculateAllSpendCandidates() {
		return calculateAllSpendCandidates(true, true);
	}

	@Deprecated
	public List<TxOutput> calculateAllSpendCandidates(boolean excludeImmatureCoinbases) {
		return calculateAllSpendCandidates(excludeImmatureCoinbases, true);
	}

	public List<TxOutput> calculateAllSpendCandidates(boolean excludeImmatureCoinbases, boolean excludeUnsignable) {
		lock.lock();
		try {
			List<TxOutput> candidates;
			if (vUTXOProvider == null) {
				candidates = new ArrayList<TxOutput>(myUnspents.size());
				for (TxOutput output : myUnspents) {
					if (excludeUnsignable && !canSignFor(output.getScriptPubKey()))
						continue;
					Transaction transaction = checkNotNull(output.getParentTransaction());
					if (excludeImmatureCoinbases && !transaction.isMature())
						continue;
					candidates.add(output);
				}
			} else {
				candidates = calculateAllSpendCandidatesFromUTXOProvider(excludeImmatureCoinbases);
			}
			return candidates;
		} finally {
			lock.unlock();
		}
	}

	public boolean canSignFor(ChainScript chainScript) {
		if (chainScript.isSentToRawPubKey()) {
			byte[] pubkey = chainScript.getPubKey();
			ECKey key = findKeyFromPubKey(pubkey);
			return key != null && (key.isEncrypted() || key.hasPrivKey());
		}
		if (chainScript.isPayToScriptHash()) {
			RedeemBlockData data = findRedeemDataFromScriptHash(chainScript.getPubKeyHash());
			return data != null && canSignFor(data.redeemScript);
		} else if (chainScript.isSentToAddress()) {
			ECKey key = findKeyFromPubHash(chainScript.getPubKeyHash());
			return key != null && (key.isEncrypted() || key.hasPrivKey());
		} else if (chainScript.isSentToMultiSig()) {
			for (ECKey pubkey : chainScript.getPubKeys()) {
				ECKey key = findKeyFromPubKey(pubkey.getPubKey());
				if (key != null && (key.isEncrypted() || key.hasPrivKey()))
					return true;
			}
		}
		return false;
	}

	protected LinkedList<TxOutput> calculateAllSpendCandidatesFromUTXOProvider(boolean excludeImmatureCoinbases) {
		checkState(lock.isHeldByCurrentThread());
		UnspentProvider utxoProvider = checkNotNull(vUTXOProvider, "No Unspent provider has been set");
		LinkedList<TxOutput> candidates = Lists.newLinkedList();
		try {
			int chainHeight = utxoProvider.getChainHeadHeight();
			for (Unspent output : getStoredOutputsFromUTXOProvider()) {
				boolean coinbase = output.isCoinbase();
				int depth = chainHeight - output.getHeight() + 1;

				if (!excludeImmatureCoinbases || !coinbase || depth >= params.getSpendableCoinbaseDepth()) {
					candidates.add(new FreeStandingTransactionOutput(params, output, chainHeight));
				}
			}
		} catch (UnspentException e) {
			throw new RuntimeException("Unspent provider error", e);
		}

		for (Transaction tx : pending.values()) {

			for (TxInput input : tx.getInputs()) {
				if (input.getConnectedOutput().isMine(this)) {
					candidates.remove(input.getConnectedOutput());
				}
			}

			if (!excludeImmatureCoinbases || tx.isMature()) {
				for (TxOutput output : tx.getOutputs()) {
					if (output.isAvailableForSpending() && output.isMine(this)) {
						candidates.add(output);
					}
				}
			}
		}
		return candidates;
	}

	protected List<Unspent> getStoredOutputsFromUTXOProvider() throws UnspentException {
		UnspentProvider utxoProvider = checkNotNull(vUTXOProvider, "No Unspent provider has been set");
		List<Unspent> candidates = new ArrayList<Unspent>();
		List<ECKey> keys = getImportedKeys();
		keys.addAll(getActiveKeychain().getLeafKeys());
		List<Address> addresses = new ArrayList<Address>();
		for (ECKey key : keys) {
			Address address = new Address(params, key.getPubKeyHash());
			addresses.add(address);
		}
		candidates.addAll(utxoProvider.getOpenTransactionOutputs(addresses));
		return candidates;
	}

	public TokenSelector getCoinSelector() {
		lock.lock();
		try {
			return tokenSelector;
		} finally {
			lock.unlock();
		}
	}

	public void setCoinSelector(TokenSelector tokenSelector) {
		lock.lock();
		try {
			this.tokenSelector = checkNotNull(tokenSelector);
		} finally {
			lock.unlock();
		}
	}

	public void allowSpendingUnconfirmedTransactions() {
		setCoinSelector(AllowUnconfirmedTokenSelector.get());
	}

	@Nullable
	public UnspentProvider getUTXOProvider() {
		lock.lock();
		try {
			return vUTXOProvider;
		} finally {
			lock.unlock();
		}
	}

	public void setUTXOProvider(@Nullable UnspentProvider provider) {
		lock.lock();
		try {
			checkArgument(provider == null || provider.getParams().equals(params));
			this.vUTXOProvider = provider;
		} finally {
			lock.unlock();
		}
	}

	private class FreeStandingTransactionOutput extends TxOutput {
		private Unspent output;
		private int chainHeight;

		public FreeStandingTransactionOutput(NetworkParams params, Unspent output, int chainHeight) {
			super(params, null, output.getValue(), output.getScript().getProgram());
			this.output = output;
			this.chainHeight = chainHeight;
		}

		public Unspent getUTXO() {
			return output;
		}

		@Override
		public int getParentTransactionDepthInBlocks() {
			return chainHeight - output.getHeight() + 1;
		}

		@Override
		public int getIndex() {
			return (int) output.getIndex();
		}

		@Override
		public Sha256Hash getParentTransactionHash() {
			return output.getHash();
		}
	}

	private static class TxOffsetPair implements Comparable<TxOffsetPair> {
		public final Transaction tx;
		public final int offset;

		public TxOffsetPair(Transaction tx, int offset) {
			this.tx = tx;
			this.offset = offset;
		}

		@Override
		public int compareTo(TxOffsetPair o) {
			return Ints.compare(offset, o.offset);
		}
	}

	@Override
	public void reorganize(StoredDataBlock splitPoint, List<StoredDataBlock> oldBlocks, List<StoredDataBlock> newBlocks)
			throws VeriException {
		lock.lock();
		try {

			checkState(confidenceChanged.size() == 0);
			checkState(!insideReorg);
			insideReorg = true;
			checkState(onWalletChangedSuppressions == 0);
			onWalletChangedSuppressions++;

			ArrayListMultimap<Sha256Hash, TxOffsetPair> mapBlockTx = ArrayListMultimap.create();
			for (Transaction tx : getTransactions(true)) {
				Map<Sha256Hash, Integer> appearsIn = tx.getAppearsInHashes();
				if (appearsIn == null)
					continue;
				for (Map.Entry<Sha256Hash, Integer> block : appearsIn.entrySet())
					mapBlockTx.put(block.getKey(), new TxOffsetPair(tx, block.getValue()));
			}
			for (Sha256Hash blockHash : mapBlockTx.keySet())
				Collections.sort(mapBlockTx.get(blockHash));

			List<Sha256Hash> oldBlockHashes = new ArrayList<Sha256Hash>(oldBlocks.size());
			log.info("Old part of chain (top to bottom):");
			for (StoredDataBlock b : oldBlocks) {
				log.info("  {}", b.getHeader().getHashAsString());
				oldBlockHashes.add(b.getHeader().getHash());
			}
			log.info("New part of chain (top to bottom):");
			for (StoredDataBlock b : newBlocks) {
				log.info("  {}", b.getHeader().getHashAsString());
			}

			Collections.reverse(newBlocks);

			LinkedList<Transaction> oldChainTxns = Lists.newLinkedList();
			for (Sha256Hash blockHash : oldBlockHashes) {
				for (TxOffsetPair pair : mapBlockTx.get(blockHash)) {
					Transaction tx = pair.tx;
					final Sha256Hash txHash = tx.getHash();
					if (tx.isCoinBase()) {

						log.warn("Coinbase killed by re-org: {}", tx.getHashAsString());
						killTx(null, ImmutableList.of(tx));
					} else {
						for (TxOutput output : tx.getOutputs()) {
							TxInput input = output.getSpentBy();
							if (input != null) {
								if (output.isMineOrWatched(this))
									checkState(myUnspents.add(output));
								input.disconnect();
							}
						}
						oldChainTxns.add(tx);
						unspent.remove(txHash);
						spent.remove(txHash);
						checkState(!pending.containsKey(txHash));
						checkState(!dead.containsKey(txHash));
					}
				}
			}

			for (Transaction tx : oldChainTxns) {

				if (tx.isCoinBase())
					continue;
				log.info("  ->pending {}", tx.getHash());
				tx.getConfidence().setConfidenceType(ConfidenceType.PENDING);
				confidenceChanged.put(tx, TransactionDegree.Listener.ChangeReason.TYPE);
				addWalletTransaction(Pool.PENDING, tx);
				updateForSpends(tx, false);
			}

			int depthToSubtract = oldBlocks.size();
			log.info("depthToSubtract = " + depthToSubtract);

			subtractDepth(depthToSubtract, spent.values());
			subtractDepth(depthToSubtract, unspent.values());
			subtractDepth(depthToSubtract, dead.values());

			setLastBlockSeenHash(splitPoint.getHeader().getHash());

			for (StoredDataBlock block : newBlocks) {
				log.info("Replaying block {}", block.getHeader().getHashAsString());
				for (TxOffsetPair pair : mapBlockTx.get(block.getHeader().getHash())) {
					log.info("  tx {}", pair.tx.getHash());
					try {
						receive(pair.tx, block, CrosChain.NewBlockType.BEST_CHAIN, pair.offset);
					} catch (ScriptException e) {
						throw new RuntimeException(e);
					}
				}
				notifyNewBestBlock(block);
			}
			isConsistentOrThrow();
			final Coin balance = getBalance();
			log.info("post-reorg balance is {}", balance.toFriendlyString());

			queueOnReorganize();
			insideReorg = false;
			onWalletChangedSuppressions--;
			maybeQueueOnWalletChanged();
			checkBalanceFuturesLocked(balance);
			informConfidenceListenersIfNotReorganizing();
			saveLater();
		} finally {
			lock.unlock();
		}
	}

	private void subtractDepth(int depthToSubtract, Collection<Transaction> transactions) {
		for (Transaction tx : transactions) {
			if (tx.getConfidence().getConfidenceType() == ConfidenceType.BUILDING) {
				tx.getConfidence().setDepthInBlocks(tx.getConfidence().getDepthInBlocks() - depthToSubtract);
				confidenceChanged.put(tx, TransactionDegree.Listener.ChangeReason.DEPTH);
			}
		}
	}

	private final ArrayList<TxOutPoint> bloomOutPoints = Lists.newArrayList();

	private final AtomicInteger bloomFilterGuard = new AtomicInteger(0);

	@Override
	public void beginBloomFilterCalculation() {
		if (bloomFilterGuard.incrementAndGet() > 1)
			return;
		lock.lock();
		keychainLock.lock();

		calcBloomOutPointsLocked();
	}

	private void calcBloomOutPointsLocked() {

		bloomOutPoints.clear();
		Set<Transaction> all = new HashSet<Transaction>();
		all.addAll(unspent.values());
		all.addAll(spent.values());
		all.addAll(pending.values());
		for (Transaction tx : all) {
			for (TxOutput out : tx.getOutputs()) {
				try {
					if (isTxOutputBloomFilterable(out))
						bloomOutPoints.add(out.getOutPointFor());
				} catch (ScriptException e) {

					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	@GuardedBy("keychainLock")
	public void endBloomFilterCalculation() {
		if (bloomFilterGuard.decrementAndGet() > 0)
			return;
		bloomOutPoints.clear();
		keychainLock.unlock();
		lock.unlock();
	}

	@Override
	public int getBloomFilterElementCount() {
		beginBloomFilterCalculation();
		try {
			int size = bloomOutPoints.size();
			size += keychain.getBloomFilterElementCount();

			size += watchedScripts.size();
			return size;
		} finally {
			endBloomFilterCalculation();
		}
	}

	@Override
	public boolean isRequiringUpdateAllBloomFilter() {

		keychainLock.lock();
		try {
			return !watchedScripts.isEmpty();
		} finally {
			keychainLock.unlock();
		}
	}

	public BloomFilter getBloomFilter(double falsePositiveRate) {
		beginBloomFilterCalculation();
		try {
			return getBloomFilter(getBloomFilterElementCount(), falsePositiveRate,
					(long) (Math.random() * Long.MAX_VALUE));
		} finally {
			endBloomFilterCalculation();
		}
	}

	@Override
	@GuardedBy("keychainLock")
	public BloomFilter getBloomFilter(int size, double falsePositiveRate, long nTweak) {
		beginBloomFilterCalculation();
		try {
			BloomFilter filter = keychain.getBloomFilter(size, falsePositiveRate, nTweak);
			for (ChainScript chainScript : watchedScripts) {
				for (ChainScriptChunk chunk : chainScript.getChunks()) {

					if (!chunk.isOpCode() && chunk.data.length >= MINIMUM_BLOOM_DATA_LENGTH) {
						filter.insert(chunk.data);
					}
				}
			}
			for (TxOutPoint point : bloomOutPoints)
				filter.insert(point.bitcoinSerialize());
			return filter;
		} finally {
			endBloomFilterCalculation();
		}
	}

	private boolean isTxOutputBloomFilterable(TxOutput out) {
		ChainScript chainScript = out.getScriptPubKey();
		boolean isScriptTypeSupported = chainScript.isSentToRawPubKey() || chainScript.isPayToScriptHash();
		return (isScriptTypeSupported && myUnspents.contains(out)) || watchedScripts.contains(chainScript);
	}

	public boolean checkForFilterExhaustion(LightBlock block) {
		keychainLock.lock();
		try {
			int epoch = keychain.getCombinedKeyLookaheadEpochs();
			for (Transaction tx : block.getAssociatedTransactions().values()) {
				markKeysAsUsed(tx);
			}
			int newEpoch = keychain.getCombinedKeyLookaheadEpochs();
			checkState(newEpoch >= epoch);

			return newEpoch > epoch;
		} finally {
			keychainLock.unlock();
		}
	}

	public void addExtension(WalletExtension extension) {
		String id = checkNotNull(extension).getWalletExtensionID();
		lock.lock();
		try {
			if (extensions.containsKey(id))
				throw new IllegalStateException("Cannot add two extensions with the same ID: " + id);
			extensions.put(id, extension);
			saveNow();
		} finally {
			lock.unlock();
		}
	}

	public WalletExtension addOrGetExistingExtension(WalletExtension extension) {
		String id = checkNotNull(extension).getWalletExtensionID();
		lock.lock();
		try {
			WalletExtension previousExtension = extensions.get(id);
			if (previousExtension != null)
				return previousExtension;
			extensions.put(id, extension);
			saveNow();
			return extension;
		} finally {
			lock.unlock();
		}
	}

	public void addOrUpdateExtension(WalletExtension extension) {
		String id = checkNotNull(extension).getWalletExtensionID();
		lock.lock();
		try {
			extensions.put(id, extension);
			saveNow();
		} finally {
			lock.unlock();
		}
	}

	public Map<String, WalletExtension> getExtensions() {
		lock.lock();
		try {
			return ImmutableMap.copyOf(extensions);
		} finally {
			lock.unlock();
		}
	}

	public void deserializeExtension(WalletExtension extension, byte[] data) throws Exception {
		lock.lock();
		keychainLock.lock();
		try {

			extension.deserializeWalletExtension(this, data);
			extensions.put(extension.getWalletExtensionID(), extension);
		} catch (Throwable throwable) {
			log.error("Error during extension deserialization", throwable);
			extensions.remove(extension.getWalletExtensionID());
			Throwables.propagate(throwable);
		} finally {
			keychainLock.unlock();
			lock.unlock();
		}
	}

	@Override
	public void setTag(String tag, ByteString value) {
		super.setTag(tag, value);
		saveNow();
	}

	private static class FeeCalculation {
		public TokenSelection bestCoinSelection;
		public TxOutput bestChangeOutput;
	}

	public FeeCalculation calculateFee(SendRequest req, Coin value, List<TxInput> originalInputs,
			boolean needAtLeastReferenceFee, List<TxOutput> candidates) throws InsufficientFundException {
		checkState(lock.isHeldByCurrentThread());
		FeeCalculation result = new FeeCalculation();

		Coin additionalValueForNextCategory = null;
		TokenSelection selection3 = null;
		TokenSelection selection2 = null;
		TxOutput selection2Change = null;
		TokenSelection selection1 = null;
		TxOutput selection1Change = null;

		int lastCalculatedSize = 0;
		Coin valueNeeded, valueMissing = null;
		while (true) {
			resetTxInputs(req, originalInputs);

			Coin fees = req.fee == null ? Coin.ZERO : req.fee;
			if (lastCalculatedSize > 0) {

				fees = fees.add(req.feePerKb.multiply((lastCalculatedSize / 1000) + 1));
			} else {
				fees = fees.add(req.feePerKb);
			}
			if (needAtLeastReferenceFee && fees.compareTo(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE) < 0)
				fees = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;

			valueNeeded = value.add(fees);
			if (additionalValueForNextCategory != null)
				valueNeeded = valueNeeded.add(additionalValueForNextCategory);
			Coin additionalValueSelected = additionalValueForNextCategory;

			TokenSelector selector = req.tokenSelector == null ? tokenSelector : req.tokenSelector;

			TokenSelection selection = selector.select(valueNeeded, new LinkedList<TxOutput>(candidates));

			if (selection.valueGathered.compareTo(valueNeeded) < 0) {
				valueMissing = valueNeeded.subtract(selection.valueGathered);
				break;
			}
			checkState(selection.gathered.size() > 0 || originalInputs.size() > 0);

			boolean eitherCategory2Or3 = false;
			boolean isCategory3 = false;

			Coin change = selection.valueGathered.subtract(valueNeeded);
			if (additionalValueSelected != null)
				change = change.add(additionalValueSelected);

			if (req.ensureMinRequiredFee && !change.equals(Coin.ZERO) && change.compareTo(Coin.CENT) < 0
					&& fees.compareTo(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE) < 0) {

				eitherCategory2Or3 = true;
				additionalValueForNextCategory = Coin.CENT;

				change = change.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.subtract(fees));
			}

			int size = 0;
			TxOutput changeOutput = null;
			if (change.signum() > 0) {

				Address changeAddress = req.changeAddress;
				if (changeAddress == null)
					changeAddress = getChangeAddress();
				changeOutput = new TxOutput(params, req.tx, change, changeAddress);

				if (req.ensureMinRequiredFee && Transaction.MIN_NONDUST_OUTPUT.compareTo(change) >= 0) {

					isCategory3 = true;
					additionalValueForNextCategory = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE
							.add(Transaction.MIN_NONDUST_OUTPUT.add(Coin.SATOSHI));
				} else {
					size += changeOutput.bitcoinSerialize().length + VariableInt.sizeOf(req.tx.getOutputs().size())
							- VariableInt.sizeOf(req.tx.getOutputs().size() - 1);

					if (!eitherCategory2Or3)
						additionalValueForNextCategory = null;
				}
			} else {
				if (eitherCategory2Or3) {

					isCategory3 = true;
					additionalValueForNextCategory = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.add(Coin.SATOSHI);
				}
			}

			for (TxOutput output : selection.gathered) {
				TxInput input = req.tx.addInput(output);

				checkState(input.getScriptBytes().length == 0);
			}

			size += req.tx.bitcoinSerialize().length;
			size += estimateBytesForSigning(selection);
			if (size / 1000 > lastCalculatedSize / 1000 && req.feePerKb.signum() > 0) {
				lastCalculatedSize = size;

				additionalValueForNextCategory = additionalValueSelected;
				continue;
			}

			if (isCategory3) {
				if (selection3 == null)
					selection3 = selection;
			} else if (eitherCategory2Or3) {

				checkState(selection2 == null);
				checkState(additionalValueForNextCategory.equals(Coin.CENT));
				selection2 = selection;
				selection2Change = checkNotNull(changeOutput);
			} else {

				checkState(selection1 == null);
				checkState(additionalValueForNextCategory == null);
				selection1 = selection;
				selection1Change = changeOutput;
			}

			if (additionalValueForNextCategory != null) {
				if (additionalValueSelected != null)
					checkState(additionalValueForNextCategory.compareTo(additionalValueSelected) > 0);
				continue;
			}
			break;
		}

		resetTxInputs(req, originalInputs);

		if (selection3 == null && selection2 == null && selection1 == null) {
			checkNotNull(valueMissing);
			log.warn("Insufficient value in wallet for send: needed {} more", valueMissing.toFriendlyString());
			throw new InsufficientFundException(valueMissing);
		}

		Coin lowestFee = null;
		result.bestCoinSelection = null;
		result.bestChangeOutput = null;
		if (selection1 != null) {
			if (selection1Change != null)
				lowestFee = selection1.valueGathered.subtract(selection1Change.getValue());
			else
				lowestFee = selection1.valueGathered;
			result.bestCoinSelection = selection1;
			result.bestChangeOutput = selection1Change;
		}

		if (selection2 != null) {
			Coin fee = selection2.valueGathered.subtract(checkNotNull(selection2Change).getValue());
			if (lowestFee == null || fee.compareTo(lowestFee) < 0) {
				lowestFee = fee;
				result.bestCoinSelection = selection2;
				result.bestChangeOutput = selection2Change;
			}
		}

		if (selection3 != null) {
			if (lowestFee == null || selection3.valueGathered.compareTo(lowestFee) < 0) {
				result.bestCoinSelection = selection3;
				result.bestChangeOutput = null;
			}
		}
		return result;
	}

	private void resetTxInputs(SendRequest req, List<TxInput> originalInputs) {
		req.tx.clearInputs();
		for (TxInput input : originalInputs)
			req.tx.addInput(input);
	}

	private int estimateBytesForSigning(TokenSelection selection) {
		int size = 0;
		for (TxOutput output : selection.gathered) {
			try {
				ChainScript chainScript = output.getScriptPubKey();
				ECKey key = null;
				ChainScript redeemScript = null;
				if (chainScript.isSentToAddress()) {
					key = findKeyFromPubHash(chainScript.getPubKeyHash());
					checkNotNull(key, "Coin selection includes unspendable outputs");
				} else if (chainScript.isPayToScriptHash()) {
					redeemScript = findRedeemDataFromScriptHash(chainScript.getPubKeyHash()).redeemScript;
					checkNotNull(redeemScript, "Coin selection includes unspendable outputs");
				}
				size += chainScript.getNumberOfBytesRequiredToSpend(key, redeemScript);
			} catch (ScriptException e) {

				throw new IllegalStateException(e);
			}
		}
		return size;
	}

	public void setTransactionBroadcaster(@Nullable cros.mail.chain.core.TransactionSender broadcaster) {
		Transaction[] toBroadcast = {};
		lock.lock();
		try {
			if (vTransactionBroadcaster == broadcaster)
				return;
			vTransactionBroadcaster = broadcaster;
			if (broadcaster == null)
				return;
			toBroadcast = pending.values().toArray(toBroadcast);
		} finally {
			lock.unlock();
		}

		for (Transaction tx : toBroadcast) {
			checkState(tx.getConfidence().getConfidenceType() == ConfidenceType.PENDING);

			log.info("New broadcaster so uploading waiting tx {}", tx.getHash());
			broadcaster.broadcastTransaction(tx);
		}
	}

	public void setKeyRotationTime(Date time) {
		setKeyRotationTime(time.getTime() / 1000);
	}

	public Date getKeyRotationTime() {
		return new Date(vKeyRotationTimestamp * 1000);
	}

	public void setKeyRotationTime(long unixTimeSeconds) {
		checkArgument(unixTimeSeconds <= Utils.currentTimeSeconds(), "Given time (%s) cannot be in the future.",
				Utils.dateTimeFormat(unixTimeSeconds * 1000));
		vKeyRotationTimestamp = unixTimeSeconds;
		saveNow();
	}

	public boolean isKeyRotating(ECKey key) {
		long time = vKeyRotationTimestamp;
		return time != 0 && key.getCreationTimeSeconds() < time;
	}

	@Deprecated
	public ListenableFuture<List<Transaction>> maybeDoMaintenance(@Nullable KeyParameter aesKey, boolean andSend)
			throws DeterUpgradePassword {
		return doMaintenance(aesKey, andSend);
	}

	public ListenableFuture<List<Transaction>> doMaintenance(@Nullable KeyParameter aesKey, boolean signAndSend)
			throws DeterUpgradePassword {
		List<Transaction> txns;
		lock.lock();
		keychainLock.lock();
		try {
			txns = maybeRotateKeys(aesKey, signAndSend);
			if (!signAndSend)
				return Futures.immediateFuture(txns);
		} finally {
			keychainLock.unlock();
			lock.unlock();
		}
		checkState(!lock.isHeldByCurrentThread());
		ArrayList<ListenableFuture<Transaction>> futures = new ArrayList<ListenableFuture<Transaction>>(txns.size());
		TransactionSender broadcaster = vTransactionBroadcaster;
		for (Transaction tx : txns) {
			try {
				final ListenableFuture<Transaction> future = broadcaster.broadcastTransaction(tx).future();
				futures.add(future);
				Futures.addCallback(future, new FutureCallback<Transaction>() {
					@Override
					public void onSuccess(Transaction transaction) {
						log.info("Successfully broadcast key rotation tx: {}", transaction);
					}

					@Override
					public void onFailure(Throwable throwable) {
						log.error("Failed to broadcast key rotation tx", throwable);
					}
				});
			} catch (Exception e) {
				log.error("Failed to broadcast rekey tx", e);
			}
		}
		return Futures.allAsList(futures);
	}

	@GuardedBy("keychainLock")
	private List<Transaction> maybeRotateKeys(@Nullable KeyParameter aesKey, boolean sign)
			throws DeterUpgradePassword {
		checkState(lock.isHeldByCurrentThread());
		checkState(keychainLock.isHeldByCurrentThread());
		List<Transaction> results = Lists.newLinkedList();

		final long keyRotationTimestamp = vKeyRotationTimestamp;
		if (keyRotationTimestamp == 0)
			return results;

		boolean allChainsRotating = true;
		for (DeterKeyChain chain : keychain.getDeterministicKeyChains()) {
			if (chain.getEarliestKeyCreationTime() >= keyRotationTimestamp) {
				allChainsRotating = false;
				break;
			}
		}
		if (allChainsRotating) {
			try {
				if (keychain.getImportedKeys().isEmpty()) {
					log.info(
							"All HD chains are currently rotating and we have no random keys, creating fresh HD chain ...");
					keychain.createAndActivateNewHDChain();
				} else {
					log.info(
							"All HD chains are currently rotating, attempting to create a new one from the next oldest non-rotating key material ...");
					keychain.upgradeToDeterministic(keyRotationTimestamp, aesKey);
					log.info(" ... upgraded to HD again, based on next best oldest key.");
				}
			} catch (TotalRandomKeysRotating rotating) {
				log.info(
						" ... no non-rotating random keys available, generating entirely new HD tree: backup required after this.");
				keychain.createAndActivateNewHDChain();
			}
			saveNow();
		}

		Transaction tx;
		do {
			tx = rekeyOneBatch(keyRotationTimestamp, aesKey, results, sign);
			if (tx != null)
				results.add(tx);
		} while (tx != null && tx.getInputs().size() == KeyTimeTokenSelector.MAX_SIMULTANEOUS_INPUTS);
		return results;
	}

	@Nullable
	private Transaction rekeyOneBatch(long timeSecs, @Nullable KeyParameter aesKey, List<Transaction> others,
			boolean sign) {
		lock.lock();
		try {

			TokenSelector keyTimeSelector = new KeyTimeTokenSelector(this, timeSecs, true);
			FilterTokenSelector selector = new FilterTokenSelector(keyTimeSelector);
			for (Transaction other : others)
				selector.excludeOutputsSpentBy(other);

			TokenSelection toMove = selector.select(Coin.ZERO, calculateAllSpendCandidates());
			if (toMove.valueGathered.equals(Coin.ZERO))
				return null;
			maybeUpgradeToHD(aesKey);
			Transaction rekeyTx = new Transaction(params);
			for (TxOutput output : toMove.gathered) {
				rekeyTx.addInput(output);
			}

			rekeyTx.addOutput(toMove.valueGathered, sign ? freshReceiveAddress() : currentReceiveAddress());
			if (!adjustOutputDownwardsForFee(rekeyTx, toMove, Coin.ZERO, Transaction.REFERENCE_DEFAULT_MIN_TX_FEE)) {
				log.error("Failed to adjust rekey tx for fees.");
				return null;
			}
			rekeyTx.getConfidence().setSource(TransactionDegree.Source.SELF);
			rekeyTx.setPurpose(Transaction.Purpose.KEY_ROTATION);
			SendRequest req = SendRequest.forTx(rekeyTx);
			req.aesKey = aesKey;
			if (sign)
				signTransaction(req);

			checkState(rekeyTx.bitcoinSerialize().length < Transaction.MAX_STANDARD_TX_SIZE);
			return rekeyTx;
		} catch (VeriException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

}
