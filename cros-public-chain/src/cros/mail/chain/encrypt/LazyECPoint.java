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

import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECFieldElement;
import org.spongycastle.math.ec.ECPoint;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public class LazyECPoint {

	private final ECCurve curve;
	private final byte[] bits;

	@Nullable
	private ECPoint point;

	public LazyECPoint(ECCurve curve, byte[] bits) {
		this.curve = curve;
		this.bits = bits;
	}

	public LazyECPoint(ECPoint point) {
		this.point = checkNotNull(point);
		this.curve = null;
		this.bits = null;
	}

	public ECPoint get() {
		if (point == null)
			point = curve.decodePoint(bits);
		return point;
	}

	public ECPoint getDetachedPoint() {
		return get().getDetachedPoint();
	}

	public byte[] getEncoded() {
		if (bits != null)
			return Arrays.copyOf(bits, bits.length);
		else
			return get().getEncoded();
	}

	public boolean isInfinity() {
		return get().isInfinity();
	}

	public ECPoint timesPow2(int e) {
		return get().timesPow2(e);
	}

	public ECFieldElement getYCoord() {
		return get().getYCoord();
	}

	public ECFieldElement[] getZCoords() {
		return get().getZCoords();
	}

	public boolean isNormalized() {
		return get().isNormalized();
	}

	public boolean isCompressed() {
		if (bits != null)
			return bits[0] == 2 || bits[0] == 3;
		else
			return get().isCompressed();
	}

	public ECPoint multiply(BigInteger k) {
		return get().multiply(k);
	}

	public ECPoint subtract(ECPoint b) {
		return get().subtract(b);
	}

	public boolean isValid() {
		return get().isValid();
	}

	public ECPoint scaleY(ECFieldElement scale) {
		return get().scaleY(scale);
	}

	public ECFieldElement getXCoord() {
		return get().getXCoord();
	}

	public ECPoint scaleX(ECFieldElement scale) {
		return get().scaleX(scale);
	}

	public boolean equals(LazyECPoint other) {
		return get().equals(other);
	}

	public ECPoint negate() {
		return get().negate();
	}

	public ECPoint threeTimes() {
		return get().threeTimes();
	}

	public ECFieldElement getZCoord(int index) {
		return get().getZCoord(index);
	}

	public byte[] getEncoded(boolean compressed) {
		if (compressed == isCompressed() && bits != null)
			return Arrays.copyOf(bits, bits.length);
		else
			return get().getEncoded(compressed);
	}

	public ECPoint add(ECPoint b) {
		return get().add(b);
	}

	public ECPoint twicePlus(ECPoint b) {
		return get().twicePlus(b);
	}

	public ECCurve getCurve() {
		return get().getCurve();
	}

	public ECPoint normalize() {
		return get().normalize();
	}

	public ECFieldElement getY() {
		return this.normalize().getYCoord();
	}

	public ECPoint twice() {
		return get().twice();
	}

	public ECFieldElement getAffineYCoord() {
		return get().getAffineYCoord();
	}

	public ECFieldElement getAffineXCoord() {
		return get().getAffineXCoord();
	}

	public ECFieldElement getX() {
		return this.normalize().getXCoord();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		LazyECPoint point1 = (LazyECPoint) o;
		if (bits != null && point1.bits != null)
			return Arrays.equals(bits, point1.bits);
		else
			return get().equals(point1.get());
	}

	@Override
	public int hashCode() {
		if (bits != null)
			return Arrays.hashCode(bits);
		else
			return get().hashCode();
	}
}
