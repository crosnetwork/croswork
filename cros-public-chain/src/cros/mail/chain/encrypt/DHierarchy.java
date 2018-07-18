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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class DHierarchy implements Serializable {
	private final Map<ImmutableList<Child>, DeterKey> keys = Maps.newHashMap();
	private final ImmutableList<Child> rootPath;

	private final Map<ImmutableList<Child>, Child> lastChildNumbers = Maps.newHashMap();

	public static final int BIP32_STANDARDISATION_TIME_SECS = 1369267200;

	public DHierarchy(DeterKey rootKey) {
		putKey(rootKey);
		rootPath = rootKey.getPath();
	}

	public void putKey(DeterKey key) {
		ImmutableList<Child> path = key.getPath();

		final DeterKey parent = key.getParent();
		if (parent != null)
			lastChildNumbers.put(parent.getPath(), key.getChildNumber());
		keys.put(path, key);
	}

	public DeterKey get(List<Child> path, boolean relativePath, boolean create) {
		ImmutableList<Child> absolutePath = relativePath
				? ImmutableList.<Child>builder().addAll(rootPath).addAll(path).build()
				: ImmutableList.copyOf(path);
		if (!keys.containsKey(absolutePath)) {
			if (!create)
				throw new IllegalArgumentException(String.format(Locale.US, "No key found for %s path %s.",
						relativePath ? "relative" : "absolute", HDUtils.formatPath(path)));
			checkArgument(absolutePath.size() > 0, "Can't derive the master key: nothing to derive from.");
			DeterKey parent = get(absolutePath.subList(0, absolutePath.size() - 1), false, true);
			putKey(HDKeyD.deriveChildKey(parent, absolutePath.get(absolutePath.size() - 1)));
		}
		return keys.get(absolutePath);
	}

	public DeterKey deriveNextChild(ImmutableList<Child> parentPath, boolean relative, boolean createParent,
			boolean privateDerivation) {
		DeterKey parent = get(parentPath, relative, createParent);
		int nAttempts = 0;
		while (nAttempts++ < HDKeyD.MAX_CHILD_DERIVATION_ATTEMPTS) {
			try {
				Child createChildNumber = getNextChildNumberToDerive(parent.getPath(), privateDerivation);
				return deriveChild(parent, createChildNumber);
			} catch (HDException ignore) {
			}
		}
		throw new HDException(
				"Maximum number of child derivation attempts reached, this is probably an indication of a bug.");
	}

	private Child getNextChildNumberToDerive(ImmutableList<Child> path, boolean privateDerivation) {
		Child lastChildNumber = lastChildNumbers.get(path);
		Child nextChildNumber = new Child(lastChildNumber != null ? lastChildNumber.num() + 1 : 0, privateDerivation);
		lastChildNumbers.put(path, nextChildNumber);
		return nextChildNumber;
	}

	public int getNumChildren(ImmutableList<Child> path) {
		final Child cn = lastChildNumbers.get(path);
		if (cn == null)
			return 0;
		else
			return cn.num() + 1;
	}

	public DeterKey deriveChild(List<Child> parentPath, boolean relative, boolean createParent,
			Child createChildNumber) {
		return deriveChild(get(parentPath, relative, createParent), createChildNumber);
	}

	private DeterKey deriveChild(DeterKey parent, Child createChildNumber) {
		DeterKey childKey = HDKeyD.deriveChildKey(parent, createChildNumber);
		putKey(childKey);
		return childKey;
	}

	public DeterKey getRootKey() {
		return get(rootPath, false, false);
	}
}
