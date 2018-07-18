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
package cros.mail.chain.encrypt;

import org.slf4j.*;

import java.io.*;
import java.security.*;

public class LinuxSecureRandom extends SecureRandomSpi {
	private static final FileInputStream urandom;

	private static class LinuxSecureRandomProvider extends Provider {
		public LinuxSecureRandomProvider() {
			super("LinuxSecureRandom", 1.0, "A Linux specific random number provider that uses /dev/urandom");
			put("LinuxSecureRandom.LinuxSecureRandom", LinuxSecureRandom.class.getName());
		}
	}

	private static final Logger log = LoggerFactory.getLogger(LinuxSecureRandom.class);

	static {
		try {
			File file = new File("/dev/urandom");

			urandom = new FileInputStream(file);
			if (urandom.read() == -1)
				throw new RuntimeException("/dev/urandom not readable?");

			int position = Security.insertProviderAt(new LinuxSecureRandomProvider(), 1);

			if (position != -1)
				log.info("Secure randomness will be read from {} only.", file);
			else
				log.info("Randomness is already secure.");
		} catch (FileNotFoundException e) {

			log.error("/dev/urandom does not appear to exist or is not openable");
			throw new RuntimeException(e);
		} catch (IOException e) {
			log.error("/dev/urandom does not appear to be readable");
			throw new RuntimeException(e);
		}
	}

	private final DataInputStream dis;

	public LinuxSecureRandom() {

		dis = new DataInputStream(urandom);
	}

	@Override
	protected void engineSetSeed(byte[] bytes) {

	}

	@Override
	protected void engineNextBytes(byte[] bytes) {
		try {
			dis.readFully(bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected byte[] engineGenerateSeed(int i) {
		byte[] bits = new byte[i];
		engineNextBytes(bits);
		return bits;
	}
}
