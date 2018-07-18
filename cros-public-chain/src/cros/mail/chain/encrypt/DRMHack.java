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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cros.mail.chain.core.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DRMHack {
	private static Logger log = LoggerFactory.getLogger(DRMHack.class);

	private static boolean done = false;

	public static void maybeDisableExportControls() {

		if (done)
			return;
		done = true;

		if (Utils.isAndroidRuntime())
			return;
		try {
			Field gate = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
			gate.setAccessible(true);
			gate.setBoolean(null, false);
			final Field allPerm = Class.forName("javax.crypto.CryptoAllPermission").getDeclaredField("INSTANCE");
			allPerm.setAccessible(true);
			Object accessAllAreasCard = allPerm.get(null);
			final Constructor<?> constructor = Class.forName("javax.crypto.CryptoPermissions").getDeclaredConstructor();
			constructor.setAccessible(true);
			Object coll = constructor.newInstance();
			Method addPerm = Class.forName("javax.crypto.CryptoPermissions").getDeclaredMethod("add",
					java.security.Permission.class);
			addPerm.setAccessible(true);
			addPerm.invoke(coll, accessAllAreasCard);
			Field defaultPolicy = Class.forName("javax.crypto.JceSecurity").getDeclaredField("defaultPolicy");
			defaultPolicy.setAccessible(true);
			defaultPolicy.set(null, coll);
		} catch (Exception e) {
			log.warn(
					"Failed to deactivate AES-256 barrier logic, Tor mode/BIP38 decryption may crash if this JVM requires it: "
							+ e.getMessage());
		}
	}
}
