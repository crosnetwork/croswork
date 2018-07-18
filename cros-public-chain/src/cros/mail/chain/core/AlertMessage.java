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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AlertMessage extends Message {
	private byte[] content;
	private byte[] signature;

	private long version = 1;
	private Date relayUntil;
	private Date expiration;
	private long id;
	private long cancel;
	private long minVer, maxVer;
	private long priority;
	private String comment, statusBar, reserved;

	private static final long MAX_SET_SIZE = 100;

	public AlertMessage(NetworkParams params, byte[] payloadBytes) throws ProtocolException {
		super(params, payloadBytes, 0);
	}

	@Override
	public String toString() {
		return "ALERT: " + getStatusBar();
	}

	@Override
	void parse() throws ProtocolException {

		int startPos = cursor;
		content = readByteArray();
		signature = readByteArray();

		cursor = startPos;
		readVarInt();

		version = readUint32();

		relayUntil = new Date(readUint64().longValue() * 1000);
		expiration = new Date(readUint64().longValue() * 1000);
		id = readUint32();
		cancel = readUint32();

		long cancelSetSize = readVarInt();
		if (cancelSetSize < 0 || cancelSetSize > MAX_SET_SIZE) {
			throw new ProtocolException("Bad cancel set size: " + cancelSetSize);
		}

		Set<Long> cancelSet = new HashSet<Long>((int) cancelSetSize);
		for (long i = 0; i < cancelSetSize; i++) {
			cancelSet.add(readUint32());
		}
		minVer = readUint32();
		maxVer = readUint32();

		long subverSetSize = readVarInt();
		if (subverSetSize < 0 || subverSetSize > MAX_SET_SIZE) {
			throw new ProtocolException("Bad subver set size: " + subverSetSize);
		}
		Set<String> matchingSubVers = new HashSet<String>((int) subverSetSize);
		for (long i = 0; i < subverSetSize; i++) {
			matchingSubVers.add(readStr());
		}
		priority = readUint32();
		comment = readStr();
		statusBar = readStr();
		reserved = readStr();

		length = cursor - offset;
	}

	public boolean isSignatureValid() {
		return ECKey.verify(Sha256Hash.hashTwice(content), signature, params.getAlertSigningKey());
	}

	@Override
	protected void parseLite() throws ProtocolException {

	}

	public Date getRelayUntil() {
		return relayUntil;
	}

	public void setRelayUntil(Date relayUntil) {
		this.relayUntil = relayUntil;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCancel() {
		return cancel;
	}

	public void setCancel(long cancel) {
		this.cancel = cancel;
	}

	public long getMinVer() {
		return minVer;
	}

	public void setMinVer(long minVer) {
		this.minVer = minVer;
	}

	public long getMaxVer() {
		return maxVer;
	}

	public void setMaxVer(long maxVer) {
		this.maxVer = maxVer;
	}

	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getStatusBar() {
		return statusBar;
	}

	public void setStatusBar(String statusBar) {
		this.statusBar = statusBar;
	}

	public String getReserved() {
		return reserved;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

	public long getVersion() {
		return version;
	}
}
