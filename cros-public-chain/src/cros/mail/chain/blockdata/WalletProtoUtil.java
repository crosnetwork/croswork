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
package cros.mail.chain.blockdata;

import cros.mail.chain.core.*;
import cros.mail.chain.core.TransactionDegree.ConfidenceType;
import cros.mail.chain.encrypt.KeyCrypt;
import cros.mail.chain.encrypt.KeyCrypterScrypt;
import cros.mail.chain.misc.InterchangeRate;
import cros.mail.chain.misc.FiatMoney;
import cros.mail.chain.script.ChainScript;
import cros.mail.chain.signature.LocalTransactionSignature;
import cros.mail.chain.signature.TransactionSignature;
import cros.mail.chain.wallet.DefaultBaseKeyChainFactory;
import cros.mail.chain.wallet.CrosKeyChainFactory;
import cros.mail.chain.wallet.CrosKeyChainGroup;
import cros.mail.chain.wallet.Protos;
import cros.mail.chain.wallet.CrosWalletTransaction;
import cros.mail.chain.wallet.Protos.Wallet.EncryptionType;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.TextFormat;
import com.google.protobuf.WireFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class WalletProtoUtil {
	private static final Logger log = LoggerFactory.getLogger(WalletProtoUtil.class);

	public static final int CURRENT_WALLET_VERSION = Protos.Wallet.getDefaultInstance().getVersion();

	private static final int WALLET_SIZE_LIMIT = 512 * 1024 * 1024;

	protected Map<ByteString, Transaction> txMap;

	private boolean requireMandatoryExtensions = true;

	public interface WalletFactory {
		Wallet create(NetworkParams params, CrosKeyChainGroup crosKeyChainGroup);
	}

	private final WalletFactory factory;
	private CrosKeyChainFactory crosKeyChainFactory;

	public WalletProtoUtil() {
		this(new WalletFactory() {
			@Override
			public Wallet create(NetworkParams params, CrosKeyChainGroup crosKeyChainGroup) {
				return new Wallet(params, crosKeyChainGroup);
			}
		});
	}

	public WalletProtoUtil(WalletFactory factory) {
		txMap = new HashMap<ByteString, Transaction>();
		this.factory = factory;
		this.crosKeyChainFactory = new DefaultBaseKeyChainFactory();
	}

	public void setKeyChainFactory(CrosKeyChainFactory crosKeyChainFactory) {
		this.crosKeyChainFactory = crosKeyChainFactory;
	}

	public void setRequireMandatoryExtensions(boolean value) {
		requireMandatoryExtensions = value;
	}

	public void writeWallet(Wallet wallet, OutputStream output) throws IOException {
		Protos.Wallet walletProto = walletToProto(wallet);
		walletProto.writeTo(output);
	}

	public String walletToText(Wallet wallet) {
		Protos.Wallet walletProto = walletToProto(wallet);
		return TextFormat.printToString(walletProto);
	}

	public Protos.Wallet walletToProto(Wallet wallet) {
		Protos.Wallet.Builder walletBuilder = Protos.Wallet.newBuilder();
		walletBuilder.setNetworkIdentifier(wallet.getNetworkParameters().getId());
		if (wallet.getDescription() != null) {
			walletBuilder.setDescription(wallet.getDescription());
		}

		for (CrosWalletTransaction wtx : wallet.getWalletTransactions()) {
			Protos.Transaction txProto = makeTxProto(wtx);
			walletBuilder.addTransaction(txProto);
		}

		walletBuilder.addAllKey(wallet.serializeKeychainToProtobuf());

		for (ChainScript chainScript : wallet.getWatchedScripts()) {
			Protos.Script protoScript = Protos.Script.newBuilder()
					.setProgram(ByteString.copyFrom(chainScript.getProgram()))
					.setCreationTimestamp(chainScript.getCreationTimeSeconds() * 1000).build();

			walletBuilder.addWatchedScript(protoScript);
		}

		Sha256Hash lastSeenBlockHash = wallet.getLastBlockSeenHash();
		if (lastSeenBlockHash != null) {
			walletBuilder.setLastSeenBlockHash(hashToByteString(lastSeenBlockHash));
			walletBuilder.setLastSeenBlockHeight(wallet.getLastBlockSeenHeight());
		}
		if (wallet.getLastBlockSeenTimeSecs() > 0)
			walletBuilder.setLastSeenBlockTimeSecs(wallet.getLastBlockSeenTimeSecs());

		KeyCrypt keyCrypt = wallet.getKeyCrypter();
		if (keyCrypt == null) {

			walletBuilder.setEncryptionType(EncryptionType.UNENCRYPTED);
		} else {

			walletBuilder.setEncryptionType(keyCrypt.getUnderstoodEncryptionType());
			if (keyCrypt instanceof KeyCrypterScrypt) {
				KeyCrypterScrypt keyCrypterScrypt = (KeyCrypterScrypt) keyCrypt;
				walletBuilder.setEncryptionParameters(keyCrypterScrypt.getScryptParameters());
			} else {

				throw new RuntimeException(
						"The wallet has encryption of type '" + keyCrypt.getUnderstoodEncryptionType()
								+ "' but this WalletProtoUtil does not know how to persist this.");
			}
		}

		if (wallet.getKeyRotationTime() != null) {
			long timeSecs = wallet.getKeyRotationTime().getTime() / 1000;
			walletBuilder.setKeyRotationTime(timeSecs);
		}

		populateExtensions(wallet, walletBuilder);

		for (Map.Entry<String, ByteString> entry : wallet.getTags().entrySet()) {
			Protos.Tag.Builder tag = Protos.Tag.newBuilder().setTag(entry.getKey()).setData(entry.getValue());
			walletBuilder.addTags(tag);
		}

		for (TransactionSignature signer : wallet.getTransactionSigners()) {

			if (signer instanceof LocalTransactionSignature)
				continue;
			Protos.TransactionSigner.Builder protoSigner = Protos.TransactionSigner.newBuilder();
			protoSigner.setClassName(signer.getClass().getName());
			protoSigner.setData(ByteString.copyFrom(signer.serialize()));
			walletBuilder.addTransactionSigners(protoSigner);
		}

		walletBuilder.setVersion(wallet.getVersion());

		return walletBuilder.build();
	}

	private static void populateExtensions(Wallet wallet, Protos.Wallet.Builder walletBuilder) {
		for (WalletExtension extension : wallet.getExtensions().values()) {
			Protos.Extension.Builder proto = Protos.Extension.newBuilder();
			proto.setId(extension.getWalletExtensionID());
			proto.setMandatory(extension.isWalletExtensionMandatory());
			proto.setData(ByteString.copyFrom(extension.serializeWalletExtension()));
			walletBuilder.addExtension(proto);
		}
	}

	private static Protos.Transaction makeTxProto(CrosWalletTransaction wtx) {
		Transaction tx = wtx.getTransaction();
		Protos.Transaction.Builder txBuilder = Protos.Transaction.newBuilder();

		txBuilder.setPool(getProtoPool(wtx)).setHash(hashToByteString(tx.getHash())).setVersion((int) tx.getVersion());

		if (tx.getUpdateTime() != null) {
			txBuilder.setUpdatedAt(tx.getUpdateTime().getTime());
		}

		if (tx.getLockTime() > 0) {
			txBuilder.setLockTime((int) tx.getLockTime());
		}

		for (TxInput input : tx.getInputs()) {
			Protos.TransactionInput.Builder inputBuilder = Protos.TransactionInput.newBuilder()
					.setScriptBytes(ByteString.copyFrom(input.getScriptBytes()))
					.setTransactionOutPointHash(hashToByteString(input.getOutpoint().getHash()))
					.setTransactionOutPointIndex((int) input.getOutpoint().getIndex());
			if (input.hasSequence())
				inputBuilder.setSequence((int) input.getSequenceNumber());
			if (input.getValue() != null)
				inputBuilder.setValue(input.getValue().value);
			txBuilder.addTransactionInput(inputBuilder);
		}

		for (TxOutput output : tx.getOutputs()) {
			Protos.TransactionOutput.Builder outputBuilder = Protos.TransactionOutput.newBuilder()
					.setScriptBytes(ByteString.copyFrom(output.getScriptBytes())).setValue(output.getValue().value);
			final TxInput spentBy = output.getSpentBy();
			if (spentBy != null) {
				Sha256Hash spendingHash = spentBy.getParentTransaction().getHash();
				int spentByTransactionIndex = spentBy.getParentTransaction().getInputs().indexOf(spentBy);
				outputBuilder.setSpentByTransactionHash(hashToByteString(spendingHash))
						.setSpentByTransactionIndex(spentByTransactionIndex);
			}
			txBuilder.addTransactionOutput(outputBuilder);
		}

		final Map<Sha256Hash, Integer> appearsInHashes = tx.getAppearsInHashes();
		if (appearsInHashes != null) {
			for (Map.Entry<Sha256Hash, Integer> entry : appearsInHashes.entrySet()) {
				txBuilder.addBlockHash(hashToByteString(entry.getKey()));
				txBuilder.addBlockRelativityOffsets(entry.getValue());
			}
		}

		if (tx.hasConfidence()) {
			TransactionDegree confidence = tx.getConfidence();
			Protos.TransactionConfidence.Builder confidenceBuilder = Protos.TransactionConfidence.newBuilder();
			writeConfidence(txBuilder, confidence, confidenceBuilder);
		}

		Protos.Transaction.Purpose purpose;
		switch (tx.getPurpose()) {
		case UNKNOWN:
			purpose = Protos.Transaction.Purpose.UNKNOWN;
			break;
		case USER_PAYMENT:
			purpose = Protos.Transaction.Purpose.USER_PAYMENT;
			break;
		case KEY_ROTATION:
			purpose = Protos.Transaction.Purpose.KEY_ROTATION;
			break;
		case ASSURANCE_CONTRACT_CLAIM:
			purpose = Protos.Transaction.Purpose.ASSURANCE_CONTRACT_CLAIM;
			break;
		case ASSURANCE_CONTRACT_PLEDGE:
			purpose = Protos.Transaction.Purpose.ASSURANCE_CONTRACT_PLEDGE;
			break;
		case ASSURANCE_CONTRACT_STUB:
			purpose = Protos.Transaction.Purpose.ASSURANCE_CONTRACT_STUB;
			break;
		case RAISE_FEE:
			purpose = Protos.Transaction.Purpose.RAISE_FEE;
			break;
		default:
			throw new RuntimeException("New tx purpose serialization not implemented.");
		}
		txBuilder.setPurpose(purpose);

		InterchangeRate interchangeRate = tx.getExchangeRate();
		if (interchangeRate != null) {
			Protos.ExchangeRate.Builder exchangeRateBuilder = Protos.ExchangeRate.newBuilder()
					.setCoinValue(interchangeRate.coin.value).setFiatValue(interchangeRate.fiatMoney.value)
					.setFiatCurrencyCode(interchangeRate.fiatMoney.currencyCode);
			txBuilder.setExchangeRate(exchangeRateBuilder);
		}

		if (tx.getMemo() != null)
			txBuilder.setMemo(tx.getMemo());

		return txBuilder.build();
	}

	private static Protos.Transaction.Pool getProtoPool(CrosWalletTransaction wtx) {
		switch (wtx.getPool()) {
		case UNSPENT:
			return Protos.Transaction.Pool.UNSPENT;
		case SPENT:
			return Protos.Transaction.Pool.SPENT;
		case DEAD:
			return Protos.Transaction.Pool.DEAD;
		case PENDING:
			return Protos.Transaction.Pool.PENDING;
		default:
			throw new RuntimeException("Unreachable");
		}
	}

	private static void writeConfidence(Protos.Transaction.Builder txBuilder, TransactionDegree confidence,
			Protos.TransactionConfidence.Builder confidenceBuilder) {
		synchronized (confidence) {
			confidenceBuilder
					.setType(Protos.TransactionConfidence.Type.valueOf(confidence.getConfidenceType().getValue()));
			if (confidence.getConfidenceType() == ConfidenceType.BUILDING) {
				confidenceBuilder.setAppearedAtHeight(confidence.getAppearedAtChainHeight());
				confidenceBuilder.setDepth(confidence.getDepthInBlocks());
			}
			if (confidence.getConfidenceType() == ConfidenceType.DEAD) {

				if (confidence.getOverridingTransaction() != null) {
					Sha256Hash overridingHash = confidence.getOverridingTransaction().getHash();
					confidenceBuilder.setOverridingTransaction(hashToByteString(overridingHash));
				}
			}
			TransactionDegree.Source source = confidence.getSource();
			switch (source) {
			case SELF:
				confidenceBuilder.setSource(Protos.TransactionConfidence.Source.SOURCE_SELF);
				break;
			case NETWORK:
				confidenceBuilder.setSource(Protos.TransactionConfidence.Source.SOURCE_NETWORK);
				break;
			case UNKNOWN:

			default:
				confidenceBuilder.setSource(Protos.TransactionConfidence.Source.SOURCE_UNKNOWN);
				break;
			}
		}

		for (PeerAddress address : confidence.getBroadcastBy()) {
			Protos.PeerAddress proto = Protos.PeerAddress.newBuilder()
					.setIpAddress(ByteString.copyFrom(address.getAddr().getAddress())).setPort(address.getPort())
					.setServices(address.getServices().longValue()).build();
			confidenceBuilder.addBroadcastBy(proto);
		}
		txBuilder.setConfidence(confidenceBuilder);
	}

	public static ByteString hashToByteString(Sha256Hash hash) {
		return ByteString.copyFrom(hash.getBytes());
	}

	public static Sha256Hash byteStringToHash(ByteString bs) {
		return Sha256Hash.wrap(bs.toByteArray());
	}

	public Wallet readWallet(InputStream input, @Nullable WalletExtension... walletExtensions)
			throws InvalidWalletException {
		try {
			Protos.Wallet walletProto = parseToProto(input);
			final String paramsID = walletProto.getNetworkIdentifier();
			NetworkParams params = NetworkParams.fromID(paramsID);
			if (params == null)
				throw new InvalidWalletException("Unknown network parameters ID " + paramsID);
			return readWallet(params, walletExtensions, walletProto);
		} catch (IOException e) {
			throw new InvalidWalletException("Could not parse input stream to protobuf", e);
		} catch (IllegalStateException e) {
			throw new InvalidWalletException("Could not parse input stream to protobuf", e);
		} catch (IllegalArgumentException e) {
			throw new InvalidWalletException("Could not parse input stream to protobuf", e);
		}
	}

	public Wallet readWallet(NetworkParams params, @Nullable WalletExtension[] extensions, Protos.Wallet walletProto)
			throws InvalidWalletException {
		if (walletProto.getVersion() > CURRENT_WALLET_VERSION)
			throw new InvalidWalletException.FutureVersion();
		if (!walletProto.getNetworkIdentifier().equals(params.getId()))
			throw new InvalidWalletException.WrongNetwork();

		CrosKeyChainGroup chain;
		if (walletProto.hasEncryptionParameters()) {
			Protos.ScryptParameters encryptionParameters = walletProto.getEncryptionParameters();
			final KeyCrypterScrypt keyCrypter = new KeyCrypterScrypt(encryptionParameters);
			chain = CrosKeyChainGroup.fromProtobufEncrypted(params, walletProto.getKeyList(), keyCrypter, crosKeyChainFactory);
		} else {
			chain = CrosKeyChainGroup.fromProtobufUnencrypted(params, walletProto.getKeyList(), crosKeyChainFactory);
		}
		Wallet wallet = factory.create(params, chain);

		List<ChainScript> chainScripts = Lists.newArrayList();
		for (Protos.Script protoScript : walletProto.getWatchedScriptList()) {
			try {
				ChainScript chainScript = new ChainScript(protoScript.getProgram().toByteArray(),
						protoScript.getCreationTimestamp() / 1000);
				chainScripts.add(chainScript);
			} catch (ScriptException e) {
				throw new InvalidWalletException("Unparseable script in wallet");
			}
		}

		wallet.addWatchedScripts(chainScripts);

		if (walletProto.hasDescription()) {
			wallet.setDescription(walletProto.getDescription());
		}

		for (Protos.Transaction txProto : walletProto.getTransactionList()) {
			readTransaction(txProto, wallet.getParams());
		}

		for (Protos.Transaction txProto : walletProto.getTransactionList()) {
			CrosWalletTransaction wtx = connectTransactionOutputs(txProto);
			wallet.addWalletTransaction(wtx);
		}

		if (!walletProto.hasLastSeenBlockHash()) {
			wallet.setLastBlockSeenHash(null);
		} else {
			wallet.setLastBlockSeenHash(byteStringToHash(walletProto.getLastSeenBlockHash()));
		}
		if (!walletProto.hasLastSeenBlockHeight()) {
			wallet.setLastBlockSeenHeight(-1);
		} else {
			wallet.setLastBlockSeenHeight(walletProto.getLastSeenBlockHeight());
		}

		wallet.setLastBlockSeenTimeSecs(walletProto.getLastSeenBlockTimeSecs());

		if (walletProto.hasKeyRotationTime()) {
			wallet.setKeyRotationTime(new Date(walletProto.getKeyRotationTime() * 1000));
		}

		loadExtensions(wallet, extensions != null ? extensions : new WalletExtension[0], walletProto);

		for (Protos.Tag tag : walletProto.getTagsList()) {
			wallet.setTag(tag.getTag(), tag.getData());
		}

		for (Protos.TransactionSigner signerProto : walletProto.getTransactionSignersList()) {
			try {
				Class signerClass = Class.forName(signerProto.getClassName());
				TransactionSignature signer = (TransactionSignature) signerClass.newInstance();
				signer.deserialize(signerProto.getData().toByteArray());
				wallet.addTransactionSigner(signer);
			} catch (Exception e) {
				throw new InvalidWalletException(
						"Unable to deserialize TransactionSignature instance: " + signerProto.getClassName(), e);
			}
		}

		if (walletProto.hasVersion()) {
			wallet.setVersion(walletProto.getVersion());
		}

		txMap.clear();

		return wallet;
	}

	private void loadExtensions(Wallet wallet, WalletExtension[] extensionsList, Protos.Wallet walletProto)
			throws InvalidWalletException {
		final Map<String, WalletExtension> extensions = new HashMap<String, WalletExtension>();
		for (WalletExtension e : extensionsList)
			extensions.put(e.getWalletExtensionID(), e);

		extensions.putAll(wallet.getExtensions());
		for (Protos.Extension extProto : walletProto.getExtensionList()) {
			String id = extProto.getId();
			WalletExtension extension = extensions.get(id);
			if (extension == null) {
				if (extProto.getMandatory()) {
					if (requireMandatoryExtensions)
						throw new InvalidWalletException("Unknown mandatory extension in wallet: " + id);
					else
						log.error("Unknown extension in wallet {}, ignoring", id);
				}
			} else {
				log.info("Loading wallet extension {}", id);
				try {
					wallet.deserializeExtension(extension, extProto.getData().toByteArray());
				} catch (Exception e) {
					if (extProto.getMandatory() && requireMandatoryExtensions) {
						log.error("Error whilst reading mandatory extension {}, failing to read wallet", id);
						throw new InvalidWalletException("Could not parse mandatory extension in wallet: " + id);
					}
				}
			}
		}
	}

	public static Protos.Wallet parseToProto(InputStream input) throws IOException {
		CodedInputStream codedInput = CodedInputStream.newInstance(input);
		codedInput.setSizeLimit(WALLET_SIZE_LIMIT);
		return Protos.Wallet.parseFrom(codedInput);
	}

	private void readTransaction(Protos.Transaction txProto, NetworkParams params) throws InvalidWalletException {
		Transaction tx = new Transaction(params);
		if (txProto.hasUpdatedAt()) {
			tx.setUpdateTime(new Date(txProto.getUpdatedAt()));
		}

		for (Protos.TransactionOutput outputProto : txProto.getTransactionOutputList()) {
			Coin value = Coin.valueOf(outputProto.getValue());
			byte[] scriptBytes = outputProto.getScriptBytes().toByteArray();
			TxOutput output = new TxOutput(params, tx, value, scriptBytes);
			tx.addOutput(output);
		}

		for (Protos.TransactionInput inputProto : txProto.getTransactionInputList()) {
			byte[] scriptBytes = inputProto.getScriptBytes().toByteArray();
			TxOutPoint outpoint = new TxOutPoint(params, inputProto.getTransactionOutPointIndex() & 0xFFFFFFFFL,
					byteStringToHash(inputProto.getTransactionOutPointHash()));
			Coin value = inputProto.hasValue() ? Coin.valueOf(inputProto.getValue()) : null;
			TxInput input = new TxInput(params, tx, scriptBytes, outpoint, value);
			if (inputProto.hasSequence())
				input.setSequenceNumber(0xffffffffL & inputProto.getSequence());
			tx.addInput(input);
		}

		for (int i = 0; i < txProto.getBlockHashCount(); i++) {
			ByteString blockHash = txProto.getBlockHash(i);
			int relativityOffset = 0;
			if (txProto.getBlockRelativityOffsetsCount() > 0)
				relativityOffset = txProto.getBlockRelativityOffsets(i);
			tx.addBlockAppearance(byteStringToHash(blockHash), relativityOffset);
		}

		if (txProto.hasLockTime()) {
			tx.setLockTime(0xffffffffL & txProto.getLockTime());
		}

		if (txProto.hasPurpose()) {
			switch (txProto.getPurpose()) {
			case UNKNOWN:
				tx.setPurpose(Transaction.Purpose.UNKNOWN);
				break;
			case USER_PAYMENT:
				tx.setPurpose(Transaction.Purpose.USER_PAYMENT);
				break;
			case KEY_ROTATION:
				tx.setPurpose(Transaction.Purpose.KEY_ROTATION);
				break;
			case ASSURANCE_CONTRACT_CLAIM:
				tx.setPurpose(Transaction.Purpose.ASSURANCE_CONTRACT_CLAIM);
				break;
			case ASSURANCE_CONTRACT_PLEDGE:
				tx.setPurpose(Transaction.Purpose.ASSURANCE_CONTRACT_PLEDGE);
				break;
			case ASSURANCE_CONTRACT_STUB:
				tx.setPurpose(Transaction.Purpose.ASSURANCE_CONTRACT_STUB);
				break;
			case RAISE_FEE:
				tx.setPurpose(Transaction.Purpose.RAISE_FEE);
				break;
			default:
				throw new RuntimeException("New purpose serialization not implemented");
			}
		} else {

			tx.setPurpose(Transaction.Purpose.USER_PAYMENT);
		}

		if (txProto.hasExchangeRate()) {
			Protos.ExchangeRate exchangeRateProto = txProto.getExchangeRate();
			tx.setExchangeRate(new InterchangeRate(Coin.valueOf(exchangeRateProto.getCoinValue()),
					FiatMoney.valueOf(exchangeRateProto.getFiatCurrencyCode(), exchangeRateProto.getFiatValue())));
		}

		if (txProto.hasMemo())
			tx.setMemo(txProto.getMemo());

		Sha256Hash protoHash = byteStringToHash(txProto.getHash());
		if (!tx.getHash().equals(protoHash))
			throw new InvalidWalletException(String.format(Locale.US,
					"Transaction did not deserialize completely: %s vs %s", tx.getHash(), protoHash));
		if (txMap.containsKey(txProto.getHash()))
			throw new InvalidWalletException(
					"Wallet contained duplicate transaction " + byteStringToHash(txProto.getHash()));
		txMap.put(txProto.getHash(), tx);
	}

	private CrosWalletTransaction connectTransactionOutputs(cros.mail.chain.wallet.Protos.Transaction txProto)
			throws InvalidWalletException {
		Transaction tx = txMap.get(txProto.getHash());
		final CrosWalletTransaction.Pool pool;
		switch (txProto.getPool()) {
		case DEAD:
			pool = CrosWalletTransaction.Pool.DEAD;
			break;
		case PENDING:
			pool = CrosWalletTransaction.Pool.PENDING;
			break;
		case SPENT:
			pool = CrosWalletTransaction.Pool.SPENT;
			break;
		case UNSPENT:
			pool = CrosWalletTransaction.Pool.UNSPENT;
			break;

		case INACTIVE:
		case PENDING_INACTIVE:
			pool = CrosWalletTransaction.Pool.PENDING;
			break;
		default:
			throw new InvalidWalletException("Unknown transaction pool: " + txProto.getPool());
		}
		for (int i = 0; i < tx.getOutputs().size(); i++) {
			TxOutput output = tx.getOutputs().get(i);
			final Protos.TransactionOutput transactionOutput = txProto.getTransactionOutput(i);
			if (transactionOutput.hasSpentByTransactionHash()) {
				final ByteString spentByTransactionHash = transactionOutput.getSpentByTransactionHash();
				Transaction spendingTx = txMap.get(spentByTransactionHash);
				if (spendingTx == null) {
					throw new InvalidWalletException(String.format(Locale.US, "Could not connect %s to %s",
							tx.getHashAsString(), byteStringToHash(spentByTransactionHash)));
				}
				final int spendingIndex = transactionOutput.getSpentByTransactionIndex();
				TxInput input = checkNotNull(spendingTx.getInput(spendingIndex));
				input.connect(output);
			}
		}

		if (txProto.hasConfidence()) {
			Protos.TransactionConfidence confidenceProto = txProto.getConfidence();
			TransactionDegree confidence = tx.getConfidence();
			readConfidence(tx, confidenceProto, confidence);
		}

		return new CrosWalletTransaction(pool, tx);
	}

	private void readConfidence(Transaction tx, Protos.TransactionConfidence confidenceProto,
			TransactionDegree confidence) throws InvalidWalletException {

		if (!confidenceProto.hasType()) {
			log.warn("Unknown confidence type for tx {}", tx.getHashAsString());
			return;
		}
		ConfidenceType confidenceType;
		switch (confidenceProto.getType()) {
		case BUILDING:
			confidenceType = ConfidenceType.BUILDING;
			break;
		case DEAD:
			confidenceType = ConfidenceType.DEAD;
			break;

		case NOT_IN_BEST_CHAIN:
			confidenceType = ConfidenceType.PENDING;
			break;
		case PENDING:
			confidenceType = ConfidenceType.PENDING;
			break;
		case UNKNOWN:

		default:
			confidenceType = ConfidenceType.UNKNOWN;
			break;
		}
		confidence.setConfidenceType(confidenceType);
		if (confidenceProto.hasAppearedAtHeight()) {
			if (confidence.getConfidenceType() != ConfidenceType.BUILDING) {
				log.warn("Have appearedAtHeight but not BUILDING for tx {}", tx.getHashAsString());
				return;
			}
			confidence.setAppearedAtChainHeight(confidenceProto.getAppearedAtHeight());
		}
		if (confidenceProto.hasDepth()) {
			if (confidence.getConfidenceType() != ConfidenceType.BUILDING) {
				log.warn("Have depth but not BUILDING for tx {}", tx.getHashAsString());
				return;
			}
			confidence.setDepthInBlocks(confidenceProto.getDepth());
		}
		if (confidenceProto.hasOverridingTransaction()) {
			if (confidence.getConfidenceType() != ConfidenceType.DEAD) {
				log.warn("Have overridingTransaction but not OVERRIDDEN for tx {}", tx.getHashAsString());
				return;
			}
			Transaction overridingTransaction = txMap.get(confidenceProto.getOverridingTransaction());
			if (overridingTransaction == null) {
				log.warn("Have overridingTransaction that is not in wallet for tx {}", tx.getHashAsString());
				return;
			}
			confidence.setOverridingTransaction(overridingTransaction);
		}
		for (Protos.PeerAddress proto : confidenceProto.getBroadcastByList()) {
			InetAddress ip;
			try {
				ip = InetAddress.getByAddress(proto.getIpAddress().toByteArray());
			} catch (UnknownHostException e) {
				throw new InvalidWalletException("Peer IP address does not have the right length", e);
			}
			int port = proto.getPort();
			PeerAddress address = new PeerAddress(ip, port);
			address.setServices(BigInteger.valueOf(proto.getServices()));
			confidence.markBroadcastBy(address);
		}
		switch (confidenceProto.getSource()) {
		case SOURCE_SELF:
			confidence.setSource(TransactionDegree.Source.SELF);
			break;
		case SOURCE_NETWORK:
			confidence.setSource(TransactionDegree.Source.NETWORK);
			break;
		case SOURCE_UNKNOWN:

		default:
			confidence.setSource(TransactionDegree.Source.UNKNOWN);
			break;
		}
	}

	public static boolean isWallet(InputStream is) {
		try {
			final CodedInputStream cis = CodedInputStream.newInstance(is);
			final int tag = cis.readTag();
			final int field = WireFormat.getTagFieldNumber(tag);
			if (field != 1)
				return false;
			final String network = cis.readString();
			return NetworkParams.fromID(network) != null;
		} catch (IOException x) {
			return false;
		}
	}
}
