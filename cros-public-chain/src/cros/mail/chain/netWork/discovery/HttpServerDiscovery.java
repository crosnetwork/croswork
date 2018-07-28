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
package cros.mail.chain.netWork.discovery;

import com.google.common.annotations.*;
import com.google.protobuf.*;
import com.squareup.okhttp.*;

import cros.mail.chain.core.ECKey;
import cros.mail.chain.core.NetworkParams;
import cros.mail.chain.core.Sha256Hash;
import cros.mail.chain.core.Utils;

import org.bitcoin.crawler.PeerSeedProtos;
import org.slf4j.*;

import javax.annotation.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;

import static com.google.common.base.Preconditions.*;

public class HttpServerDiscovery implements NetworkPeerDiscovery {
	private static final Logger log = LoggerFactory.getLogger(HttpServerDiscovery.class);

	public static class Details {
		@Nullable
		public final ECKey pubkey;
		public final URI uri;

		public Details(@Nullable ECKey pubkey, URI uri) {
			this.pubkey = pubkey;
			this.uri = uri;
		}
	}

	private final Details details;
	private final NetworkParams params;
	private final OkHttpClient client;

	public HttpServerDiscovery(NetworkParams params, URI uri, @Nullable ECKey pubkey) {
		this(params, new Details(pubkey, uri));
	}

	public HttpServerDiscovery(NetworkParams params, Details details) {
		this(params, details, new OkHttpClient());
	}

	public HttpServerDiscovery(NetworkParams params, Details details, OkHttpClient client) {
		checkArgument(details.uri.getScheme().startsWith("http"));
		this.details = details;
		this.params = params;
		this.client = client;
	}

	@Override
	public InetSocketAddress[] getPeers(long timeoutValue, TimeUnit timeoutUnit) throws NetworkDiscoveryException {
		try {
			log.info("Requesting seeds from {}", details.uri);
			Response response = client.newCall(new Request.Builder().url(details.uri.toURL()).build()).execute();
			if (!response.isSuccessful())
				throw new NetworkDiscoveryException(
						"HTTP request failed: " + response.code() + " " + response.message());
			InputStream stream = response.body().byteStream();
			GZIPInputStream zip = new GZIPInputStream(stream);
			PeerSeedProtos.SignedPeerSeeds proto = PeerSeedProtos.SignedPeerSeeds.parseDelimitedFrom(zip);
			stream.close();
			return protoToAddrs(proto);
		} catch (NetworkDiscoveryException e1) {
			throw e1;
		} catch (Exception e) {
			throw new NetworkDiscoveryException(e);
		}
	}

	@VisibleForTesting
	public InetSocketAddress[] protoToAddrs(PeerSeedProtos.SignedPeerSeeds proto)
			throws NetworkDiscoveryException, InvalidProtocolBufferException, SignatureException {
		if (details.pubkey != null) {
			if (!Arrays.equals(proto.getPubkey().toByteArray(), details.pubkey.getPubKey()))
				throw new NetworkDiscoveryException("Public key mismatch");
			byte[] hash = Sha256Hash.hash(proto.getPeerSeeds().toByteArray());
			details.pubkey.verifyOrThrow(hash, proto.getSignature().toByteArray());
		}
		PeerSeedProtos.PeerSeeds seeds = PeerSeedProtos.PeerSeeds.parseFrom(proto.getPeerSeeds());
		if (seeds.getTimestamp() < Utils.currentTimeSeconds() - (60 * 60 * 24))
			throw new NetworkDiscoveryException("Seed data is more than one day old: replay attack?");
		if (!seeds.getNet().equals(params.getPaymentProtocolId()))
			throw new NetworkDiscoveryException("Network mismatch");
		InetSocketAddress[] results = new InetSocketAddress[seeds.getSeedCount()];
		int i = 0;
		for (PeerSeedProtos.PeerSeedData data : seeds.getSeedList())
			results[i++] = new InetSocketAddress(data.getIpAddress(), data.getPort());
		return results;
	}

	@Override
	public void shutdown() {
	}
}
