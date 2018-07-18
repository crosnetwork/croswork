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

import javax.annotation.*;

import cros.mail.chain.misc.*;
import cros.mail.chain.misc.Threading;

import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.locks.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class TxDegreeTable {
	protected ReentrantLock lock = Threading.lock("txconfidencetable");

	private static class WeakConfidenceReference extends WeakReference<TransactionDegree> {
		public Sha256Hash hash;

		public WeakConfidenceReference(TransactionDegree confidence, ReferenceQueue<TransactionDegree> queue) {
			super(confidence, queue);
			hash = confidence.getTransactionHash();
		}
	}

	private LinkedHashMap<Sha256Hash, WeakConfidenceReference> table;

	private ReferenceQueue<TransactionDegree> referenceQueue;

	public static final int MAX_SIZE = 1000;

	public TxDegreeTable(final int size) {
		table = new LinkedHashMap<Sha256Hash, WeakConfidenceReference>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Sha256Hash, WeakConfidenceReference> entry) {

				return size() > size;
			}
		};
		referenceQueue = new ReferenceQueue<TransactionDegree>();
	}

	public TxDegreeTable() {
		this(MAX_SIZE);
	}

	private void cleanTable() {
		lock.lock();
		try {
			Reference<? extends TransactionDegree> ref;
			while ((ref = referenceQueue.poll()) != null) {

				WeakConfidenceReference txRef = (WeakConfidenceReference) ref;

				table.remove(txRef.hash);
			}
		} finally {
			lock.unlock();
		}
	}

	public int numBroadcastPeers(Sha256Hash txHash) {
		lock.lock();
		try {
			cleanTable();
			WeakConfidenceReference entry = table.get(txHash);
			if (entry == null) {
				return 0;
			} else {
				TransactionDegree confidence = entry.get();
				if (confidence == null) {

					table.remove(txHash);
					return 0;
				} else {
					return confidence.numBroadcastPeers();
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public TransactionDegree seen(Sha256Hash hash, PeerAddress byPeer) {
		TransactionDegree confidence;
		boolean fresh = false;
		lock.lock();
		{
			cleanTable();
			confidence = getOrCreate(hash);
			fresh = confidence.markBroadcastBy(byPeer);
		}
		lock.unlock();
		if (fresh)
			confidence.queueListeners(TransactionDegree.Listener.ChangeReason.SEEN_PEERS);
		return confidence;
	}

	public TransactionDegree getOrCreate(Sha256Hash hash) {
		checkNotNull(hash);
		lock.lock();
		try {
			WeakConfidenceReference reference = table.get(hash);
			if (reference != null) {
				TransactionDegree confidence = reference.get();
				if (confidence != null)
					return confidence;
			}
			TransactionDegree newConfidence = new TransactionDegree(hash);
			table.put(hash, new WeakConfidenceReference(newConfidence, referenceQueue));
			return newConfidence;
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	public TransactionDegree get(Sha256Hash hash) {
		lock.lock();
		try {
			WeakConfidenceReference ref = table.get(hash);
			if (ref == null)
				return null;
			TransactionDegree confidence = ref.get();
			if (confidence != null)
				return confidence;
			else
				return null;
		} finally {
			lock.unlock();
		}
	}
}
