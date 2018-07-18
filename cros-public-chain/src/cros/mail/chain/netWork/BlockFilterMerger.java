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
package cros.mail.chain.netWork;

import com.google.common.collect.Lists;
import cros.mail.chain.core.BloomFilter;
import cros.mail.chain.core.PeerProvider;
import com.google.common.collect.ImmutableList;

import java.util.LinkedList;

public class BlockFilterMerger {

	private final long bloomFilterTweak = (long) (Math.random() * Long.MAX_VALUE);

	private volatile double vBloomFilterFPRate;
	private int lastBloomFilterElementCount;
	private BloomFilter lastFilter;

	public BlockFilterMerger(double bloomFilterFPRate) {
		this.vBloomFilterFPRate = bloomFilterFPRate;
	}

	public static class Result {
		public BloomFilter filter;
		public long earliestKeyTimeSecs;
		public boolean changed;
	}

	public Result calculate(ImmutableList<PeerProvider> providers) {
		LinkedList<PeerProvider> begunProviders = Lists.newLinkedList();
		try {

			for (PeerProvider provider : providers) {
				provider.beginBloomFilterCalculation();
				begunProviders.add(provider);
			}
			Result result = new Result();
			result.earliestKeyTimeSecs = Long.MAX_VALUE;
			int elements = 0;
			boolean requiresUpdateAll = false;
			for (PeerProvider p : providers) {
				result.earliestKeyTimeSecs = Math.min(result.earliestKeyTimeSecs, p.getEarliestKeyCreationTime());
				elements += p.getBloomFilterElementCount();
				requiresUpdateAll = requiresUpdateAll || p.isRequiringUpdateAllBloomFilter();
			}

			if (elements > 0) {

				lastBloomFilterElementCount = elements > lastBloomFilterElementCount ? elements + 100
						: lastBloomFilterElementCount;
				BloomFilter.BloomUpdate bloomFlags = requiresUpdateAll ? BloomFilter.BloomUpdate.UPDATE_ALL
						: BloomFilter.BloomUpdate.UPDATE_P2PUBKEY_ONLY;
				double fpRate = vBloomFilterFPRate;
				BloomFilter filter = new BloomFilter(lastBloomFilterElementCount, fpRate, bloomFilterTweak, bloomFlags);
				for (PeerProvider p : providers)
					filter.merge(p.getBloomFilter(lastBloomFilterElementCount, fpRate, bloomFilterTweak));

				result.changed = !filter.equals(lastFilter);
				result.filter = lastFilter = filter;
			}

			result.earliestKeyTimeSecs -= 86400 * 7;
			return result;
		} finally {
			for (PeerProvider provider : begunProviders) {
				provider.endBloomFilterCalculation();
			}
		}
	}

	public void setBloomFilterFPRate(double bloomFilterFPRate) {
		this.vBloomFilterFPRate = bloomFilterFPRate;
	}

	public double getBloomFilterFPRate() {
		return vBloomFilterFPRate;
	}

	public BloomFilter getLastFilter() {
		return lastFilter;
	}
}
