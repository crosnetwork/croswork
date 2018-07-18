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
package cros.mail.chain.param;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cros.mail.chain.blockdata.BlockData;
import cros.mail.chain.blockdata.BlockDataException;
import cros.mail.chain.core.Block;
import cros.mail.chain.core.Coin;
import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.core.StoredDataBlock;
import cros.mail.chain.core.Transaction;
import cros.mail.chain.core.Utils;
import cros.mail.chain.core.VeriException;
import cros.mail.chain.misc.MoneyFormat;


public abstract class AbstractCrosNetParam extends NetworkParams {

	public static final String BITCOIN_SCHEME = "bitcoin";

	private static final Logger log = LoggerFactory.getLogger(AbstractCrosNetParam.class);

	public AbstractCrosNetParam() {
		super();
	}

	protected boolean isDifficultyTransitionPoint(StoredDataBlock storedPrev) {
		return ((storedPrev.getHeight() + 1) % this.getInterval()) == 0;
	}

	@Override
	public void checkDifficultyTransitions(final StoredDataBlock storedPrev, final Block nextBlock,
			final BlockData blockData) throws VeriException, BlockDataException {
		Block prev = storedPrev.getHeader();

		if (!isDifficultyTransitionPoint(storedPrev)) {

			if (nextBlock.getDifficultyTarget() != prev.getDifficultyTarget())
				throw new VeriException("Unexpected change in difficulty at height " + storedPrev.getHeight() + ": "
						+ Long.toHexString(nextBlock.getDifficultyTarget()) + " vs "
						+ Long.toHexString(prev.getDifficultyTarget()));
			return;
		}

		long now = System.currentTimeMillis();
		StoredDataBlock cursor = blockData.get(prev.getHash());
		for (int i = 0; i < this.getInterval() - 1; i++) {
			if (cursor == null) {

				throw new VeriException(
						"Difficulty transition point but we did not find a way back to the genesis block.");
			}
			cursor = blockData.get(cursor.getHeader().getPrevBlockHash());
		}
		long elapsed = System.currentTimeMillis() - now;
		if (elapsed > 50)
			log.info("Difficulty transition traversal took {}msec", elapsed);

		Block blockIntervalAgo = cursor.getHeader();
		int timespan = (int) (prev.getTimeSeconds() - blockIntervalAgo.getTimeSeconds());

		final int targetTimespan = this.getTargetTimespan();
		if (timespan < targetTimespan / 4)
			timespan = targetTimespan / 4;
		if (timespan > targetTimespan * 4)
			timespan = targetTimespan * 4;

		BigInteger newTarget = Utils.decodeCompactBits(prev.getDifficultyTarget());
		newTarget = newTarget.multiply(BigInteger.valueOf(timespan));
		newTarget = newTarget.divide(BigInteger.valueOf(targetTimespan));

		if (newTarget.compareTo(this.getMaxTarget()) > 0) {
			log.info("Difficulty hit proof of work limit: {}", newTarget.toString(16));
			newTarget = this.getMaxTarget();
		}

		int accuracyBytes = (int) (nextBlock.getDifficultyTarget() >>> 24) - 3;
		long receivedTargetCompact = nextBlock.getDifficultyTarget();

		BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
		newTarget = newTarget.and(mask);
		long newTargetCompact = Utils.encodeCompactBits(newTarget);

		if (newTargetCompact != receivedTargetCompact)
			throw new VeriException("Network provided difficulty bits do not match what was calculated: "
					+ newTargetCompact + " vs " + receivedTargetCompact);
	}

	@Override
	public Coin getMaxMoney() {
		return MAX_MONEY;
	}

	@Override
	public Coin getMinNonDustOutput() {
		return Transaction.MIN_NONDUST_OUTPUT;
	}

	@Override
	public MoneyFormat getMonetaryFormat() {
		return new MoneyFormat();
	}

	@Override
	public String getUriScheme() {
		return BITCOIN_SCHEME;
	}

	@Override
	public boolean hasMaxMoney() {
		return true;
	}
}
