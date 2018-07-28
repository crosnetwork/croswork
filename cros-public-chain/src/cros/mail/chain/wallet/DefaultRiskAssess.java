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
package cros.mail.chain.wallet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cros.mail.chain.core.Coin;
import cros.mail.chain.core.ECKey;
import cros.mail.chain.core.ECKey.ECDSASignature;
import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.core.Transaction;
import cros.mail.chain.core.TransactionDegree;
import cros.mail.chain.core.TxInput;
import cros.mail.chain.core.TxOutput;
import cros.mail.chain.core.Wallet;
import cros.mail.chain.encrypt.Signature;
import cros.mail.chain.script.ChainScriptChunk;

import javax.annotation.Nullable;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public class DefaultRiskAssess implements RiskAssess {
	private static final Logger log = LoggerFactory.getLogger(DefaultRiskAssess.class);

	public static final Coin MIN_ANALYSIS_NONDUST_OUTPUT = Transaction.MIN_NONDUST_OUTPUT;

	protected final Transaction tx;
	protected final List<Transaction> dependencies;
	@Nullable
	protected final Wallet wallet;

	private Transaction nonStandard;
	protected Transaction nonFinal;
	protected boolean analyzed;

	private DefaultRiskAssess(Wallet wallet, Transaction tx, List<Transaction> dependencies) {
		this.tx = tx;
		this.dependencies = dependencies;
		this.wallet = wallet;
	}

	@Override
	public Result analyze() {
		checkState(!analyzed);
		analyzed = true;

		Result result = analyzeIsFinal();
		if (result != null && result != Result.OK)
			return result;

		return analyzeIsStandard();
	}

	@Nullable
	private Result analyzeIsFinal() {

		if (tx.getConfidence().getSource() == TransactionDegree.Source.SELF)
			return Result.OK;

		if (tx.isOptInFullRBF()) {
			nonFinal = tx;
			return Result.NON_FINAL;
		}

		if (wallet == null)
			return null;

		final int height = wallet.getLastBlockSeenHeight();
		final long time = wallet.getLastBlockSeenTimeSecs();

		final int adjustedHeight = height + 1;

		if (!tx.isFinal(adjustedHeight, time)) {
			nonFinal = tx;
			return Result.NON_FINAL;
		}
		for (Transaction dep : dependencies) {
			if (!dep.isFinal(adjustedHeight, time)) {
				nonFinal = dep;
				return Result.NON_FINAL;
			}
		}

		return Result.OK;
	}

	public enum RuleViolation {
		NONE, VERSION, DUST, SHORTEST_POSSIBLE_PUSHDATA, NONEMPTY_STACK, SIGNATURE_CANONICAL_ENCODING
	}

	public static RuleViolation isStandard(Transaction tx) {

		if (tx.getVersion() > 1 || tx.getVersion() < 1) {
			log.warn("TX considered non-standard due to unknown version number {}", tx.getVersion());
			return RuleViolation.VERSION;
		}

		final List<TxOutput> outputs = tx.getOutputs();
		for (int i = 0; i < outputs.size(); i++) {
			TxOutput output = outputs.get(i);
			RuleViolation violation = isOutputStandard(output);
			if (violation != RuleViolation.NONE) {
				log.warn("TX considered non-standard due to output {} violating rule {}", i, violation);
				return violation;
			}
		}

		final List<TxInput> inputs = tx.getInputs();
		for (int i = 0; i < inputs.size(); i++) {
			TxInput input = inputs.get(i);
			RuleViolation violation = isInputStandard(input);
			if (violation != RuleViolation.NONE) {
				log.warn("TX considered non-standard due to input {} violating rule {}", i, violation);
				return violation;
			}
		}

		return RuleViolation.NONE;
	}

	public static RuleViolation isOutputStandard(TxOutput output) {
		if (output.getValue().compareTo(MIN_ANALYSIS_NONDUST_OUTPUT) < 0)
			return RuleViolation.DUST;
		for (ChainScriptChunk chunk : output.getScriptPubKey().getChunks()) {
			if (chunk.isPushData() && !chunk.isShortestPossiblePushData())
				return RuleViolation.SHORTEST_POSSIBLE_PUSHDATA;
		}
		return RuleViolation.NONE;
	}

	public static RuleViolation isInputStandard(TxInput input) {
		for (ChainScriptChunk chunk : input.getScriptSig().getChunks()) {
			if (chunk.data != null && !chunk.isShortestPossiblePushData())
				return RuleViolation.SHORTEST_POSSIBLE_PUSHDATA;
			if (chunk.isPushData()) {
				ECDSASignature signature;
				try {
					signature = ECKey.ECDSASignature.decodeFromDER(chunk.data);
				} catch (RuntimeException x) {

					signature = null;
				}
				if (signature != null) {
					if (!Signature.isEncodingCanonical(chunk.data))
						return RuleViolation.SIGNATURE_CANONICAL_ENCODING;
					if (!signature.isCanonical())
						return RuleViolation.SIGNATURE_CANONICAL_ENCODING;
				}
			}
		}
		return RuleViolation.NONE;
	}

	private Result analyzeIsStandard() {

		if (wallet != null && !wallet.getNetworkParameters().getId().equals(NetworkParams.ID_MAINNET))
			return Result.OK;

		RuleViolation ruleViolation = isStandard(tx);
		if (ruleViolation != RuleViolation.NONE) {
			nonStandard = tx;
			return Result.NON_STANDARD;
		}

		for (Transaction dep : dependencies) {
			ruleViolation = isStandard(dep);
			if (ruleViolation != RuleViolation.NONE) {
				nonStandard = dep;
				return Result.NON_STANDARD;
			}
		}

		return Result.OK;
	}

	@Nullable
	public Transaction getNonStandard() {
		return nonStandard;
	}

	@Nullable
	public Transaction getNonFinal() {
		return nonFinal;
	}

	@Override
	public String toString() {
		if (!analyzed)
			return "Pending risk analysis for " + tx.getHashAsString();
		else if (nonFinal != null)
			return "Risky due to non-finality of " + nonFinal.getHashAsString();
		else if (nonStandard != null)
			return "Risky due to non-standard tx " + nonStandard.getHashAsString();
		else
			return "Non-risky";
	}

	public static class Analyzer implements RiskAssess.Analyzer {
		@Override
		public DefaultRiskAssess create(Wallet wallet, Transaction tx, List<Transaction> dependencies) {
			return new DefaultRiskAssess(wallet, tx, dependencies);
		}
	}

	public static Analyzer FACTORY = new Analyzer();
}
