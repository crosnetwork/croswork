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
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
/**
 * 
 * @author CROS
 *
 */
class StoredTransactionOutPoint implements Serializable {
	private static final long serialVersionUID = -4064230006297064377L;

	Sha256Hash hash;

	long index;

	StoredTransactionOutPoint(Sha256Hash hash, long index) {
		this.hash = hash;
		this.index = index;
	}

	StoredTransactionOutPoint(Unspent out) {
		this.hash = out.getHash();
		this.index = out.getIndex();
	}

	Sha256Hash getHash() {
		return hash;
	}

	long getIndex() {
		return index;
	}

	@Override
	public int hashCode() {
		return this.hash.hashCode() + (int) index;
	}

	@Override
	public String toString() {
		return "Stored transaction out point: " + hash + ":" + index;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		StoredTransactionOutPoint other = (StoredTransactionOutPoint) o;
		return getIndex() == other.getIndex() && Objects.equal(getHash(), other.getHash());
	}
}

class TransactionalHashMap<KeyType, ValueType> {
	ThreadLocal<HashMap<KeyType, ValueType>> tempMap;
	ThreadLocal<HashSet<KeyType>> tempSetRemoved;
	private ThreadLocal<Boolean> inTransaction;

	HashMap<KeyType, ValueType> map;

	public TransactionalHashMap() {
		tempMap = new ThreadLocal<HashMap<KeyType, ValueType>>();
		tempSetRemoved = new ThreadLocal<HashSet<KeyType>>();
		inTransaction = new ThreadLocal<Boolean>();
		map = new HashMap<KeyType, ValueType>();
	}

	public void beginDatabaseBatchWrite() {
		inTransaction.set(true);
	}

	public void commitDatabaseBatchWrite() {
		if (tempSetRemoved.get() != null)
			for (KeyType key : tempSetRemoved.get())
				map.remove(key);
		if (tempMap.get() != null)
			for (Map.Entry<KeyType, ValueType> entry : tempMap.get().entrySet())
				map.put(entry.getKey(), entry.getValue());
		abortDatabaseBatchWrite();
	}

	public void abortDatabaseBatchWrite() {
		inTransaction.set(false);
		tempSetRemoved.remove();
		tempMap.remove();
	}

	@Nullable
	public ValueType get(KeyType key) {
		if (Boolean.TRUE.equals(inTransaction.get())) {
			if (tempMap.get() != null) {
				ValueType value = tempMap.get().get(key);
				if (value != null)
					return value;
			}
			if (tempSetRemoved.get() != null && tempSetRemoved.get().contains(key))
				return null;
		}
		return map.get(key);
	}

	public List<ValueType> values() {
		List<ValueType> valueTypes = new ArrayList<ValueType>();
		for (KeyType keyType : map.keySet()) {
			valueTypes.add(get(keyType));
		}
		return valueTypes;
	}

	public void put(KeyType key, ValueType value) {
		if (Boolean.TRUE.equals(inTransaction.get())) {
			if (tempSetRemoved.get() != null)
				tempSetRemoved.get().remove(key);
			if (tempMap.get() == null)
				tempMap.set(new HashMap<KeyType, ValueType>());
			tempMap.get().put(key, value);
		} else {
			map.put(key, value);
		}
	}

	@Nullable
	public ValueType remove(KeyType key) {
		if (Boolean.TRUE.equals(inTransaction.get())) {
			ValueType retVal = map.get(key);
			if (retVal != null) {
				if (tempSetRemoved.get() == null)
					tempSetRemoved.set(new HashSet<KeyType>());
				tempSetRemoved.get().add(key);
			}
			if (tempMap.get() != null) {
				ValueType tempVal = tempMap.get().remove(key);
				if (tempVal != null)
					return tempVal;
			}
			return retVal;
		} else {
			return map.remove(key);
		}
	}
}

class TransactionalMultiKeyHashMap<UniqueKeyType, MultiKeyType, ValueType> {
	TransactionalHashMap<UniqueKeyType, ValueType> mapValues;
	HashMap<MultiKeyType, Set<UniqueKeyType>> mapKeys;

	public TransactionalMultiKeyHashMap() {
		mapValues = new TransactionalHashMap<UniqueKeyType, ValueType>();
		mapKeys = new HashMap<MultiKeyType, Set<UniqueKeyType>>();
	}

	public void BeginTransaction() {
		mapValues.beginDatabaseBatchWrite();
	}

	public void CommitTransaction() {
		mapValues.commitDatabaseBatchWrite();
	}

	public void AbortTransaction() {
		mapValues.abortDatabaseBatchWrite();
	}

	@Nullable
	public ValueType get(UniqueKeyType key) {
		return mapValues.get(key);
	}

	public void put(UniqueKeyType uniqueKey, MultiKeyType multiKey, ValueType value) {
		mapValues.put(uniqueKey, value);
		Set<UniqueKeyType> set = mapKeys.get(multiKey);
		if (set == null) {
			set = new HashSet<UniqueKeyType>();
			set.add(uniqueKey);
			mapKeys.put(multiKey, set);
		} else {
			set.add(uniqueKey);
		}
	}

	@Nullable
	public ValueType removeByUniqueKey(UniqueKeyType key) {
		return mapValues.remove(key);
	}

	public void removeByMultiKey(MultiKeyType key) {
		Set<UniqueKeyType> set = mapKeys.remove(key);
		if (set != null)
			for (UniqueKeyType uniqueKey : set)
				removeByUniqueKey(uniqueKey);
	}
}

public class MemoryFullPrunedBlockData implements FullPrunedBlockData {
	protected static class StoredBlockAndWasUndoableFlag {
		public StoredDataBlock block;
		public boolean wasUndoable;

		public StoredBlockAndWasUndoableFlag(StoredDataBlock block, boolean wasUndoable) {
			this.block = block;
			this.wasUndoable = wasUndoable;
		}
	}

	private TransactionalHashMap<Sha256Hash, StoredBlockAndWasUndoableFlag> blockMap;
	private TransactionalMultiKeyHashMap<Sha256Hash, Integer, StoredInvalidDataBlock> fullBlockMap;

	private TransactionalHashMap<StoredTransactionOutPoint, Unspent> transactionOutputMap;
	private StoredDataBlock chainHead;
	private StoredDataBlock verifiedChainHead;
	private int fullStoreDepth;
	private NetworkParams params;

	public MemoryFullPrunedBlockData(NetworkParams params, int fullStoreDepth) {
		blockMap = new TransactionalHashMap<Sha256Hash, StoredBlockAndWasUndoableFlag>();
		fullBlockMap = new TransactionalMultiKeyHashMap<Sha256Hash, Integer, StoredInvalidDataBlock>();
		transactionOutputMap = new TransactionalHashMap<StoredTransactionOutPoint, Unspent>();
		this.fullStoreDepth = fullStoreDepth > 0 ? fullStoreDepth : 1;

		try {
			StoredDataBlock storedGenesisHeader = new StoredDataBlock(params.getGenesisBlock().cloneAsHeader(),
					params.getGenesisBlock().getWork(), 0);

			List<Transaction> genesisTransactions = Lists.newLinkedList();
			StoredInvalidDataBlock storedGenesis = new StoredInvalidDataBlock(params.getGenesisBlock().getHash(),
					genesisTransactions);
			put(storedGenesisHeader, storedGenesis);
			setChainHead(storedGenesisHeader);
			setVerifiedChainHead(storedGenesisHeader);
			this.params = params;
		} catch (BlockDataException e) {
			throw new RuntimeException(e);
		} catch (VeriException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void put(StoredDataBlock block) throws BlockDataException {
		Preconditions.checkNotNull(blockMap, "MemoryFullPrunedBlockData is closed");
		Sha256Hash hash = block.getHeader().getHash();
		blockMap.put(hash, new StoredBlockAndWasUndoableFlag(block, false));
	}

	@Override
	public synchronized void put(StoredDataBlock storedDataBlock, StoredInvalidDataBlock undoableBlock)
			throws BlockDataException {
		Preconditions.checkNotNull(blockMap, "MemoryFullPrunedBlockData is closed");
		Sha256Hash hash = storedDataBlock.getHeader().getHash();
		fullBlockMap.put(hash, storedDataBlock.getHeight(), undoableBlock);
		blockMap.put(hash, new StoredBlockAndWasUndoableFlag(storedDataBlock, true));
	}

	@Override
	@Nullable
	public synchronized StoredDataBlock get(Sha256Hash hash) throws BlockDataException {
		Preconditions.checkNotNull(blockMap, "MemoryFullPrunedBlockData is closed");
		StoredBlockAndWasUndoableFlag storedBlock = blockMap.get(hash);
		return storedBlock == null ? null : storedBlock.block;
	}

	@Override
	@Nullable
	public synchronized StoredDataBlock getOnceUndoableStoredBlock(Sha256Hash hash) throws BlockDataException {
		Preconditions.checkNotNull(blockMap, "MemoryFullPrunedBlockData is closed");
		StoredBlockAndWasUndoableFlag storedBlock = blockMap.get(hash);
		return (storedBlock != null && storedBlock.wasUndoable) ? storedBlock.block : null;
	}

	@Override
	@Nullable
	public synchronized StoredInvalidDataBlock getUndoBlock(Sha256Hash hash) throws BlockDataException {
		Preconditions.checkNotNull(fullBlockMap, "MemoryFullPrunedBlockData is closed");
		return fullBlockMap.get(hash);
	}

	@Override
	public synchronized StoredDataBlock getChainHead() throws BlockDataException {
		Preconditions.checkNotNull(blockMap, "MemoryFullPrunedBlockData is closed");
		return chainHead;
	}

	@Override
	public synchronized void setChainHead(StoredDataBlock chainHead) throws BlockDataException {
		Preconditions.checkNotNull(blockMap, "MemoryFullPrunedBlockData is closed");
		this.chainHead = chainHead;
	}

	@Override
	public synchronized StoredDataBlock getVerifiedChainHead() throws BlockDataException {
		Preconditions.checkNotNull(blockMap, "MemoryFullPrunedBlockData is closed");
		return verifiedChainHead;
	}

	@Override
	public synchronized void setVerifiedChainHead(StoredDataBlock chainHead) throws BlockDataException {
		Preconditions.checkNotNull(blockMap, "MemoryFullPrunedBlockData is closed");
		this.verifiedChainHead = chainHead;
		if (this.chainHead.getHeight() < chainHead.getHeight())
			setChainHead(chainHead);

		fullBlockMap.removeByMultiKey(chainHead.getHeight() - fullStoreDepth);
	}

	@Override
	public void close() {
		blockMap = null;
		fullBlockMap = null;
		transactionOutputMap = null;
	}

	@Override
	@Nullable
	public synchronized Unspent getTransactionOutput(Sha256Hash hash, long index) throws BlockDataException {
		Preconditions.checkNotNull(transactionOutputMap, "MemoryFullPrunedBlockData is closed");
		return transactionOutputMap.get(new StoredTransactionOutPoint(hash, index));
	}

	@Override
	public synchronized void addUnspentTransactionOutput(Unspent out) throws BlockDataException {
		Preconditions.checkNotNull(transactionOutputMap, "MemoryFullPrunedBlockData is closed");
		transactionOutputMap.put(new StoredTransactionOutPoint(out), out);
	}

	@Override
	public synchronized void removeUnspentTransactionOutput(Unspent out) throws BlockDataException {
		Preconditions.checkNotNull(transactionOutputMap, "MemoryFullPrunedBlockData is closed");
		if (transactionOutputMap.remove(new StoredTransactionOutPoint(out)) == null)
			throw new BlockDataException(
					"Tried to remove a Unspent from MemoryFullPrunedBlockData that it didn't have!");
	}

	@Override
	public synchronized void beginDatabaseBatchWrite() throws BlockDataException {
		blockMap.beginDatabaseBatchWrite();
		fullBlockMap.BeginTransaction();
		transactionOutputMap.beginDatabaseBatchWrite();
	}

	@Override
	public synchronized void commitDatabaseBatchWrite() throws BlockDataException {
		blockMap.commitDatabaseBatchWrite();
		fullBlockMap.CommitTransaction();
		transactionOutputMap.commitDatabaseBatchWrite();
	}

	@Override
	public synchronized void abortDatabaseBatchWrite() throws BlockDataException {
		blockMap.abortDatabaseBatchWrite();
		fullBlockMap.AbortTransaction();
		transactionOutputMap.abortDatabaseBatchWrite();
	}

	@Override
	public synchronized boolean hasUnspentOutputs(Sha256Hash hash, int numOutputs) throws BlockDataException {
		for (int i = 0; i < numOutputs; i++)
			if (getTransactionOutput(hash, i) != null)
				return true;
		return false;
	}

	@Override
	public NetworkParams getParams() {
		return params;
	}

	@Override
	public int getChainHeadHeight() throws UnspentException {
		try {
			return getVerifiedChainHead().getHeight();
		} catch (BlockDataException e) {
			throw new UnspentException(e);
		}
	}

	@Override
	public List<Unspent> getOpenTransactionOutputs(List<Address> addresses) throws UnspentException {

		List<Unspent> foundOutputs = new ArrayList<Unspent>();
		List<Unspent> outputsList = transactionOutputMap.values();
		for (Unspent output : outputsList) {
			for (Address address : addresses) {
				if (output.getAddress().equals(address.toString())) {
					foundOutputs.add(output);
				}
			}
		}
		return foundOutputs;
	}
}
