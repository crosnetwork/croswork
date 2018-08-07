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

import org.slf4j.*;

import cros.mail.chain.core.*;
import cros.mail.chain.misc.*;

import javax.annotation.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.locks.*;

import static com.google.common.base.Preconditions.*;

public class SPVBlockData implements BlockData {
	private static final Logger log = LoggerFactory.getLogger(SPVBlockData.class);

	public static final int DEFAULT_NUM_HEADERS = 5000;
	public static final String HEADER_MAGIC = "SPVB";

	protected volatile MappedByteBuffer buffer;
	protected int numHeaders;
	protected NetworkParams params;

	protected ReentrantLock lock = Threading.lock("SPVBlockData");

	protected LinkedHashMap<Sha256Hash, StoredDataBlock> blockCache = new LinkedHashMap<Sha256Hash, StoredDataBlock>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Sha256Hash, StoredDataBlock> entry) {
			return size() > 2050;
		}
	};

	protected static final Object notFoundMarker = new Object();
	protected LinkedHashMap<Sha256Hash, Object> notFoundCache = new LinkedHashMap<Sha256Hash, Object>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Sha256Hash, Object> entry) {
			return size() > 100;
		}
	};

	protected FileLock fileLock = null;
	protected RandomAccessFile randomAccessFile = null;

	public SPVBlockData(NetworkParams params, File file) throws BlockDataException {
		checkNotNull(file);
		this.params = checkNotNull(params);
		try {
			this.numHeaders = DEFAULT_NUM_HEADERS;
			boolean exists = file.exists();

			randomAccessFile = new RandomAccessFile(file, "rw");
			long fileSize = getFileSize();
			if (!exists) {
				log.info("Creating new SPV block chain file " + file);
				randomAccessFile.setLength(fileSize);
			} else if (randomAccessFile.length() != fileSize) {
				throw new BlockDataException("File size on disk does not match expected size: "
						+ randomAccessFile.length() + " vs " + fileSize);
			}

			FileChannel channel = randomAccessFile.getChannel();
			fileLock = channel.tryLock();
			if (fileLock == null)
				throw new FileLockedException("Store file is already locked by another process");

			buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);

			byte[] header;
			if (exists) {
				header = new byte[4];
				buffer.get(header);
				if (!new String(header, "US-ASCII").equals(HEADER_MAGIC))
					throw new BlockDataException("Header bytes do not equal " + HEADER_MAGIC);
			} else {
				initNewStore(params);
			}
		} catch (Exception e) {
			try {
				if (randomAccessFile != null)
					randomAccessFile.close();
			} catch (IOException e2) {
				throw new BlockDataException(e2);
			}
			throw new BlockDataException(e);
		}
	}

	private void initNewStore(NetworkParams params) throws Exception {
		byte[] header;
		header = HEADER_MAGIC.getBytes("US-ASCII");
		buffer.put(header);

		lock.lock();
		try {
			setRingCursor(buffer, FILE_PROLOGUE_BYTES);
		} finally {
			lock.unlock();
		}
		Block genesis = params.getGenesisBlock().cloneAsHeader();
		StoredDataBlock storedGenesis = new StoredDataBlock(genesis, genesis.getWork(), 0);
		put(storedGenesis);
		setChainHead(storedGenesis);
	}

	public int getFileSize() {
		return RECORD_SIZE * numHeaders + FILE_PROLOGUE_BYTES;
	}

	@Override
	public void put(StoredDataBlock block) throws BlockDataException {
		final MappedByteBuffer buffer = this.buffer;
		if (buffer == null)
			throw new BlockDataException("Store closed");

		lock.lock();
		try {
			int cursor = getRingCursor(buffer);
			if (cursor == getFileSize()) {

				cursor = FILE_PROLOGUE_BYTES;
			}
			buffer.position(cursor);
			Sha256Hash hash = block.getHeader().getHash();
			notFoundCache.remove(hash);
			buffer.put(hash.getBytes());
			block.serializeCompact(buffer);
			setRingCursor(buffer, buffer.position());
			blockCache.put(hash, block);
		} finally {
			lock.unlock();
		}
	}

	@Override
	@Nullable
	public StoredDataBlock get(Sha256Hash hash) throws BlockDataException {
		final MappedByteBuffer buffer = this.buffer;
		if (buffer == null)
			throw new BlockDataException("Store closed");

		lock.lock();
		try {
			StoredDataBlock cacheHit = blockCache.get(hash);
			if (cacheHit != null)
				return cacheHit;
			if (notFoundCache.get(hash) != null)
				return null;

			int cursor = getRingCursor(buffer);
			final int startingPoint = cursor;
			final int fileSize = getFileSize();
			final byte[] targetHashBytes = hash.getBytes();
			byte[] scratch = new byte[32];
			do {
				cursor -= RECORD_SIZE;
				if (cursor < FILE_PROLOGUE_BYTES) {

					cursor = fileSize - RECORD_SIZE;
				}

				buffer.position(cursor);
				buffer.get(scratch);
				if (Arrays.equals(scratch, targetHashBytes)) {

					StoredDataBlock storedDataBlock = StoredDataBlock.deserializeCompact(params, buffer);
					blockCache.put(hash, storedDataBlock);
					return storedDataBlock;
				}
			} while (cursor != startingPoint);

			notFoundCache.put(hash, notFoundMarker);
			return null;
		} catch (ProtocolException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

	protected StoredDataBlock lastChainHead = null;

	@Override
	public StoredDataBlock getChainHead() throws BlockDataException {
		final MappedByteBuffer buffer = this.buffer;
		if (buffer == null)
			throw new BlockDataException("Store closed");

		lock.lock();
		try {
			if (lastChainHead == null) {
				byte[] headHash = new byte[32];
				buffer.position(8);
				buffer.get(headHash);
				Sha256Hash hash = Sha256Hash.wrap(headHash);
				StoredDataBlock block = get(hash);
				if (block == null)
					throw new BlockDataException("Corrupted block store: could not find chain head: " + hash);
				lastChainHead = block;
			}
			return lastChainHead;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void setChainHead(StoredDataBlock chainHead) throws BlockDataException {
		final MappedByteBuffer buffer = this.buffer;
		if (buffer == null)
			throw new BlockDataException("Store closed");

		lock.lock();
		try {
			lastChainHead = chainHead;
			byte[] headHash = chainHead.getHeader().getHash().getBytes();
			buffer.position(8);
			buffer.put(headHash);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() throws BlockDataException {
		try {
			buffer.force();
			if (System.getProperty("os.name").toLowerCase().contains("win")) {
				log.info("Windows mmap hack: Forcing buffer cleaning");
				MapUtil.forceRelease(buffer);
			}
			buffer = null;
			randomAccessFile.close();
		} catch (IOException e) {
			throw new BlockDataException(e);
		}
	}

	@Override
	public NetworkParams getParams() {
		return params;
	}

	protected static final int RECORD_SIZE = 32 + StoredDataBlock.COMPACT_SERIALIZED_SIZE;

	protected static final int FILE_PROLOGUE_BYTES = 1024;

	private int getRingCursor(ByteBuffer buffer) {
		int c = buffer.getInt(4);
		checkState(c >= FILE_PROLOGUE_BYTES, "Integer overflow");
		return c;
	}

	private void setRingCursor(ByteBuffer buffer, int newCursor) {
		checkArgument(newCursor >= 0);
		buffer.putInt(4, newCursor);
	}
} 
