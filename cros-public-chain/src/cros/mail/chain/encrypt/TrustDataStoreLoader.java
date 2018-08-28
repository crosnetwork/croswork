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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;

public interface TrustDataStoreLoader {
	KeyStore getKeyStore() throws FileNotFoundException, KeyStoreException;

	String DEFAULT_KEYSTORE_TYPE = KeyStore.getDefaultType();
	String DEFAULT_KEYSTORE_PASSWORD = "changeit";

	class DefaultTrustStoreLoader implements TrustDataStoreLoader {
		@Override
		public KeyStore getKeyStore() throws FileNotFoundException, KeyStoreException {

			String keystorePath = null;
			String keystoreType = DEFAULT_KEYSTORE_TYPE;
			try {

				Class<?> version = Class.forName("android.os.Build$VERSION");

				if (version.getDeclaredField("SDK_INT").getInt(version) >= 14) {
					return loadIcsKeyStore();
				} else {
					keystoreType = "BKS";
					keystorePath = System.getProperty("java.home")
							+ "/etc/security/cacerts.bks".replace('/', File.separatorChar);
				}
			} catch (ClassNotFoundException e) {

			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			if (keystorePath == null) {
				keystorePath = System.getProperty("javax.net.ssl.trustStore");
			}
			if (keystorePath == null) {
				return loadFallbackStore();
			}
			try {
				return X509Utils.loadKeyStore(keystoreType, DEFAULT_KEYSTORE_PASSWORD,
						new FileInputStream(keystorePath));
			} catch (FileNotFoundException e) {

				return loadFallbackStore();
			}
		}

		private KeyStore loadIcsKeyStore() throws KeyStoreException {
			try {

				KeyStore keystore = KeyStore.getInstance("AndroidCAStore");
				keystore.load(null, null);
				return keystore;
			} catch (IOException x) {
				throw new KeyStoreException(x);
			} catch (GeneralSecurityException x) {
				throw new KeyStoreException(x);
			}
		}

		private KeyStore loadFallbackStore() throws FileNotFoundException, KeyStoreException {
			return X509Utils.loadKeyStore("JKS", DEFAULT_KEYSTORE_PASSWORD, getClass().getResourceAsStream("cacerts"));
		}
	}

	class FileTrustStoreLoader implements TrustDataStoreLoader {
		private final File path;

		public FileTrustStoreLoader(@Nonnull File path) throws FileNotFoundException {
			if (!path.exists())
				throw new FileNotFoundException(path.toString());
			this.path = path;
		}

		@Override
		public KeyStore getKeyStore() throws FileNotFoundException, KeyStoreException {
			return X509Utils.loadKeyStore(DEFAULT_KEYSTORE_TYPE, DEFAULT_KEYSTORE_PASSWORD, new FileInputStream(path));
		}
	}
}
