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

import cros.mail.chain.blockdata.BlockData;
import cros.mail.chain.blockdata.BlockDataException;
import cros.mail.chain.blockdata.FullPrunedBlockData;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.*;
/**
 * 
 * @author CROS
 *
 */
public class CheckpointManager {
	private static final Logger log = LoggerFactory.getLogger(CheckpointManager.class);

	private static final String BINARY_MAGIC = "CHECKPOINTS 1";
	private static final String TEXTUAL_MAGIC = "TXT CHECKPOINTS 1";
	private static final int MAX_SIGNATURES = 256;

	protected final TreeMap<Long, StoredDataBlock> checkpoints = new TreeMap<Long, StoredDataBlock>();

	protected final NetworkParams params;
	protected final Sha256Hash dataHash;

	public static final BaseEncoding BASE64 = BaseEncoding.base64().omitPadding();

	public CheckpointManager(NetworkParams params, InputStream inputStream) throws IOException {
		this.params = checkNotNull(params);
		checkNotNull(inputStream);
		inputStream = new BufferedInputStream(inputStream);
		inputStream.mark(1);
		int first = inputStream.read();
		inputStream.reset();
		if (first == BINARY_MAGIC.charAt(0))
			dataHash = readBinary(inputStream);
		else if (first == TEXTUAL_MAGIC.charAt(0))
			dataHash = readTextual(inputStream);
		else
			throw new IOException("Unsupported format.");
	}

	private Sha256Hash readBinary(InputStream inputStream) throws IOException {
		DataInputStream dis = null;
		try {
			MessageDigest digest = Sha256Hash.newDigest();
			DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);
			dis = new DataInputStream(digestInputStream);
			digestInputStream.on(false);
			byte[] header = new byte[BINARY_MAGIC.length()];
			dis.readFully(header);
			if (!Arrays.equals(header, BINARY_MAGIC.getBytes("US-ASCII")))
				throw new IOException("Header bytes did not match expected version");
			int numSignatures = checkPositionIndex(dis.readInt(), MAX_SIGNATURES, "Num signatures out of range");
			for (int i = 0; i < numSignatures; i++) {
				byte[] sig = new byte[65];
				dis.readFully(sig);

			}
			digestInputStream.on(true);
			int numCheckpoints = dis.readInt();
			checkState(numCheckpoints > 0);
			final int size = StoredDataBlock.COMPACT_SERIALIZED_SIZE;
			ByteBuffer buffer = ByteBuffer.allocate(size);
			for (int i = 0; i < numCheckpoints; i++) {
				if (dis.read(buffer.array(), 0, size) < size)
					throw new IOException("Incomplete read whilst loading checkpoints.");
				StoredDataBlock block = StoredDataBlock.deserializeCompact(params, buffer);
				buffer.position(0);
				checkpoints.put(block.getHeader().getTimeSeconds(), block);
			}
			Sha256Hash dataHash = Sha256Hash.wrap(digest.digest());
			log.info("Read {} checkpoints, hash is {}", checkpoints.size(), dataHash);
			return dataHash;
		} catch (ProtocolException e) {
			throw new IOException(e);
		} finally {
			if (dis != null)
				dis.close();
			inputStream.close();
		}
	}

	private Sha256Hash readTextual(InputStream inputStream) throws IOException {
		Hasher hasher = Hashing.sha256().newHasher();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream, Charsets.US_ASCII));
			String magic = reader.readLine();
			if (!TEXTUAL_MAGIC.equals(magic))
				throw new IOException("unexpected magic: " + magic);
			int numSigs = Integer.parseInt(reader.readLine());
			for (int i = 0; i < numSigs; i++)
				reader.readLine();
			int numCheckpoints = Integer.parseInt(reader.readLine());
			checkState(numCheckpoints > 0);

			hasher.putBytes(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(numCheckpoints).array());
			final int size = StoredDataBlock.COMPACT_SERIALIZED_SIZE;
			ByteBuffer buffer = ByteBuffer.allocate(size);
			for (int i = 0; i < numCheckpoints; i++) {
				byte[] bytes = BASE64.decode(reader.readLine());
				hasher.putBytes(bytes);
				buffer.position(0);
				buffer.put(bytes);
				buffer.position(0);
				StoredDataBlock block = StoredDataBlock.deserializeCompact(params, buffer);
				checkpoints.put(block.getHeader().getTimeSeconds(), block);
			}
			HashCode hash = hasher.hash();
			log.info("Read {} checkpoints, hash is {}", checkpoints.size(), hash);
			return Sha256Hash.wrap(hash.asBytes());
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public StoredDataBlock getCheckpointBefore(long time) {
		try {
			checkArgument(time > params.getGenesisBlock().getTimeSeconds());

			Map.Entry<Long, StoredDataBlock> entry = checkpoints.floorEntry(time);
			if (entry != null)
				return entry.getValue();
			Block genesis = params.getGenesisBlock().cloneAsHeader();
			return new StoredDataBlock(genesis, genesis.getWork(), 0);
		} catch (VeriException e) {
			throw new RuntimeException(e);
		}
	}

	public int numCheckpoints() {
		return checkpoints.size();
	}

	public Sha256Hash getDataHash() {
		return dataHash;
	}

	public static void checkpoint(NetworkParams params, InputStream checkpoints, BlockData store, long time)
			throws IOException, BlockDataException {
		checkNotNull(params);
		checkNotNull(store);
		checkArgument(!(store instanceof FullPrunedBlockData), "You cannot use checkpointing with a full store.");

		time -= 86400 * 7;

		checkArgument(time > 0);
		log.info("Attempting to initialize a new block store with a checkpoint for time {} ({})", time,
				Utils.dateTimeFormat(time * 1000));

		BufferedInputStream stream = new BufferedInputStream(checkpoints);
		CheckpointManager manager = new CheckpointManager(params, stream);
		StoredDataBlock checkpoint = manager.getCheckpointBefore(time);
		store.put(checkpoint);
		store.setChainHead(checkpoint);
	}
}
