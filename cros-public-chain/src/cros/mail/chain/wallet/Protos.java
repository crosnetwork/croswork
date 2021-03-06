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

public final class Protos {
	private Protos() {
	}

	public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
	}

	public interface PeerAddressOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasIpAddress();

		com.google.protobuf.ByteString getIpAddress();

		boolean hasPort();

		int getPort();

		boolean hasServices();

		long getServices();
	}

	public static final class PeerAddress extends com.google.protobuf.GeneratedMessage implements

			PeerAddressOrBuilder {

		private PeerAddress(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private PeerAddress(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final PeerAddress defaultInstance;

		public static PeerAddress getDefaultInstance() {
			return defaultInstance;
		}

		public PeerAddress getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private PeerAddress(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 10: {
						bitField0_ |= 0x00000001;
						ipAddress_ = input.readBytes();
						break;
					}
					case 16: {
						bitField0_ |= 0x00000002;
						port_ = input.readUInt32();
						break;
					}
					case 24: {
						bitField0_ |= 0x00000004;
						services_ = input.readUInt64();
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_PeerAddress_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_PeerAddress_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.PeerAddress.class,
							cros.mail.chain.wallet.Protos.PeerAddress.Builder.class);
		}

		public static com.google.protobuf.Parser<PeerAddress> PARSER = new com.google.protobuf.AbstractParser<PeerAddress>() {
			public PeerAddress parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new PeerAddress(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<PeerAddress> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int IP_ADDRESS_FIELD_NUMBER = 1;
		private com.google.protobuf.ByteString ipAddress_;

		public boolean hasIpAddress() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public com.google.protobuf.ByteString getIpAddress() {
			return ipAddress_;
		}

		public static final int PORT_FIELD_NUMBER = 2;
		private int port_;

		public boolean hasPort() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public int getPort() {
			return port_;
		}

		public static final int SERVICES_FIELD_NUMBER = 3;
		private long services_;

		public boolean hasServices() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public long getServices() {
			return services_;
		}

		private void initFields() {
			ipAddress_ = com.google.protobuf.ByteString.EMPTY;
			port_ = 0;
			services_ = 0L;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasIpAddress()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasPort()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasServices()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeBytes(1, ipAddress_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeUInt32(2, port_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeUInt64(3, services_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, ipAddress_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeUInt32Size(2, port_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeUInt64Size(3, services_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.PeerAddress parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.PeerAddress parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.PeerAddress parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.PeerAddress parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.PeerAddress parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.PeerAddress parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.PeerAddress parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.PeerAddress parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.PeerAddress parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.PeerAddress parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.PeerAddress prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

				cros.mail.chain.wallet.Protos.PeerAddressOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_PeerAddress_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_PeerAddress_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.PeerAddress.class,
								cros.mail.chain.wallet.Protos.PeerAddress.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				ipAddress_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000001);
				port_ = 0;
				bitField0_ = (bitField0_ & ~0x00000002);
				services_ = 0L;
				bitField0_ = (bitField0_ & ~0x00000004);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_PeerAddress_descriptor;
			}

			public cros.mail.chain.wallet.Protos.PeerAddress getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.PeerAddress.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.PeerAddress build() {
				cros.mail.chain.wallet.Protos.PeerAddress result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.PeerAddress buildPartial() {
				cros.mail.chain.wallet.Protos.PeerAddress result = new cros.mail.chain.wallet.Protos.PeerAddress(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.ipAddress_ = ipAddress_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.port_ = port_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000004;
				}
				result.services_ = services_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.PeerAddress) {
					return mergeFrom((cros.mail.chain.wallet.Protos.PeerAddress) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.PeerAddress other) {
				if (other == cros.mail.chain.wallet.Protos.PeerAddress.getDefaultInstance())
					return this;
				if (other.hasIpAddress()) {
					setIpAddress(other.getIpAddress());
				}
				if (other.hasPort()) {
					setPort(other.getPort());
				}
				if (other.hasServices()) {
					setServices(other.getServices());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasIpAddress()) {

					return false;
				}
				if (!hasPort()) {

					return false;
				}
				if (!hasServices()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.PeerAddress parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.PeerAddress) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private com.google.protobuf.ByteString ipAddress_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasIpAddress() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public com.google.protobuf.ByteString getIpAddress() {
				return ipAddress_;
			}

			public Builder setIpAddress(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				ipAddress_ = value;
				onChanged();
				return this;
			}

			public Builder clearIpAddress() {
				bitField0_ = (bitField0_ & ~0x00000001);
				ipAddress_ = getDefaultInstance().getIpAddress();
				onChanged();
				return this;
			}

			private int port_;

			public boolean hasPort() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public int getPort() {
				return port_;
			}

			public Builder setPort(int value) {
				bitField0_ |= 0x00000002;
				port_ = value;
				onChanged();
				return this;
			}

			public Builder clearPort() {
				bitField0_ = (bitField0_ & ~0x00000002);
				port_ = 0;
				onChanged();
				return this;
			}

			private long services_;

			public boolean hasServices() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public long getServices() {
				return services_;
			}

			public Builder setServices(long value) {
				bitField0_ |= 0x00000004;
				services_ = value;
				onChanged();
				return this;
			}

			public Builder clearServices() {
				bitField0_ = (bitField0_ & ~0x00000004);
				services_ = 0L;
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new PeerAddress(true);
			defaultInstance.initFields();
		}

	}

	public interface EncryptedDataOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasInitialisationVector();

		com.google.protobuf.ByteString getInitialisationVector();

		boolean hasEncryptedPrivateKey();

		com.google.protobuf.ByteString getEncryptedPrivateKey();
	}

	public static final class EncryptedData extends com.google.protobuf.GeneratedMessage implements

			EncryptedDataOrBuilder {

		private EncryptedData(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private EncryptedData(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final EncryptedData defaultInstance;

		public static EncryptedData getDefaultInstance() {
			return defaultInstance;
		}

		public EncryptedData getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private EncryptedData(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 10: {
						bitField0_ |= 0x00000001;
						initialisationVector_ = input.readBytes();
						break;
					}
					case 18: {
						bitField0_ |= 0x00000002;
						encryptedPrivateKey_ = input.readBytes();
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_EncryptedData_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_EncryptedData_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.EncryptedData.class,
							cros.mail.chain.wallet.Protos.EncryptedData.Builder.class);
		}

		public static com.google.protobuf.Parser<EncryptedData> PARSER = new com.google.protobuf.AbstractParser<EncryptedData>() {
			public EncryptedData parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new EncryptedData(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<EncryptedData> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int INITIALISATION_VECTOR_FIELD_NUMBER = 1;
		private com.google.protobuf.ByteString initialisationVector_;

		public boolean hasInitialisationVector() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public com.google.protobuf.ByteString getInitialisationVector() {
			return initialisationVector_;
		}

		public static final int ENCRYPTED_PRIVATE_KEY_FIELD_NUMBER = 2;
		private com.google.protobuf.ByteString encryptedPrivateKey_;

		public boolean hasEncryptedPrivateKey() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public com.google.protobuf.ByteString getEncryptedPrivateKey() {
			return encryptedPrivateKey_;
		}

		private void initFields() {
			initialisationVector_ = com.google.protobuf.ByteString.EMPTY;
			encryptedPrivateKey_ = com.google.protobuf.ByteString.EMPTY;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasInitialisationVector()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasEncryptedPrivateKey()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeBytes(1, initialisationVector_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeBytes(2, encryptedPrivateKey_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, initialisationVector_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, encryptedPrivateKey_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.EncryptedData parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.EncryptedData parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.EncryptedData parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.EncryptedData parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.EncryptedData parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.EncryptedData parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.EncryptedData parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.EncryptedData parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.EncryptedData parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.EncryptedData parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.EncryptedData prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

			cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_EncryptedData_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_EncryptedData_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.EncryptedData.class,
								cros.mail.chain.wallet.Protos.EncryptedData.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				initialisationVector_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000001);
				encryptedPrivateKey_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000002);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_EncryptedData_descriptor;
			}

			public cros.mail.chain.wallet.Protos.EncryptedData getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.EncryptedData.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.EncryptedData build() {
				cros.mail.chain.wallet.Protos.EncryptedData result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.EncryptedData buildPartial() {
				cros.mail.chain.wallet.Protos.EncryptedData result = new cros.mail.chain.wallet.Protos.EncryptedData(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.initialisationVector_ = initialisationVector_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.encryptedPrivateKey_ = encryptedPrivateKey_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.EncryptedData) {
					return mergeFrom((cros.mail.chain.wallet.Protos.EncryptedData) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.EncryptedData other) {
				if (other == cros.mail.chain.wallet.Protos.EncryptedData.getDefaultInstance())
					return this;
				if (other.hasInitialisationVector()) {
					setInitialisationVector(other.getInitialisationVector());
				}
				if (other.hasEncryptedPrivateKey()) {
					setEncryptedPrivateKey(other.getEncryptedPrivateKey());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasInitialisationVector()) {

					return false;
				}
				if (!hasEncryptedPrivateKey()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.EncryptedData parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.EncryptedData) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private com.google.protobuf.ByteString initialisationVector_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasInitialisationVector() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public com.google.protobuf.ByteString getInitialisationVector() {
				return initialisationVector_;
			}

			public Builder setInitialisationVector(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				initialisationVector_ = value;
				onChanged();
				return this;
			}

			public Builder clearInitialisationVector() {
				bitField0_ = (bitField0_ & ~0x00000001);
				initialisationVector_ = getDefaultInstance().getInitialisationVector();
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString encryptedPrivateKey_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasEncryptedPrivateKey() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public com.google.protobuf.ByteString getEncryptedPrivateKey() {
				return encryptedPrivateKey_;
			}

			public Builder setEncryptedPrivateKey(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000002;
				encryptedPrivateKey_ = value;
				onChanged();
				return this;
			}

			public Builder clearEncryptedPrivateKey() {
				bitField0_ = (bitField0_ & ~0x00000002);
				encryptedPrivateKey_ = getDefaultInstance().getEncryptedPrivateKey();
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new EncryptedData(true);
			defaultInstance.initFields();
		}

	}

	public interface DeterministicKeyOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasChainCode();

		com.google.protobuf.ByteString getChainCode();

		java.util.List<java.lang.Integer> getPathList();

		int getPathCount();

		int getPath(int index);

		boolean hasIssuedSubkeys();

		int getIssuedSubkeys();

		boolean hasLookaheadSize();

		int getLookaheadSize();

		boolean hasIsFollowing();

		boolean getIsFollowing();

		boolean hasSigsRequiredToSpend();

		int getSigsRequiredToSpend();
	}

	public static final class DeterministicKey extends com.google.protobuf.GeneratedMessage implements

			DeterministicKeyOrBuilder {

		private DeterministicKey(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private DeterministicKey(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final DeterministicKey defaultInstance;

		public static DeterministicKey getDefaultInstance() {
			return defaultInstance;
		}

		public DeterministicKey getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private DeterministicKey(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 10: {
						bitField0_ |= 0x00000001;
						chainCode_ = input.readBytes();
						break;
					}
					case 16: {
						if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
							path_ = new java.util.ArrayList<java.lang.Integer>();
							mutable_bitField0_ |= 0x00000002;
						}
						path_.add(input.readUInt32());
						break;
					}
					case 18: {
						int length = input.readRawVarint32();
						int limit = input.pushLimit(length);
						if (!((mutable_bitField0_ & 0x00000002) == 0x00000002) && input.getBytesUntilLimit() > 0) {
							path_ = new java.util.ArrayList<java.lang.Integer>();
							mutable_bitField0_ |= 0x00000002;
						}
						while (input.getBytesUntilLimit() > 0) {
							path_.add(input.readUInt32());
						}
						input.popLimit(limit);
						break;
					}
					case 24: {
						bitField0_ |= 0x00000002;
						issuedSubkeys_ = input.readUInt32();
						break;
					}
					case 32: {
						bitField0_ |= 0x00000004;
						lookaheadSize_ = input.readUInt32();
						break;
					}
					case 40: {
						bitField0_ |= 0x00000008;
						isFollowing_ = input.readBool();
						break;
					}
					case 48: {
						bitField0_ |= 0x00000010;
						sigsRequiredToSpend_ = input.readUInt32();
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				if (((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
					path_ = java.util.Collections.unmodifiableList(path_);
				}
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_DeterministicKey_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_DeterministicKey_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.DeterministicKey.class,
							cros.mail.chain.wallet.Protos.DeterministicKey.Builder.class);
		}

		public static com.google.protobuf.Parser<DeterministicKey> PARSER = new com.google.protobuf.AbstractParser<DeterministicKey>() {
			public DeterministicKey parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new DeterministicKey(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<DeterministicKey> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int CHAIN_CODE_FIELD_NUMBER = 1;
		private com.google.protobuf.ByteString chainCode_;

		public boolean hasChainCode() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public com.google.protobuf.ByteString getChainCode() {
			return chainCode_;
		}

		public static final int PATH_FIELD_NUMBER = 2;
		private java.util.List<java.lang.Integer> path_;

		public java.util.List<java.lang.Integer> getPathList() {
			return path_;
		}

		public int getPathCount() {
			return path_.size();
		}

		public int getPath(int index) {
			return path_.get(index);
		}

		public static final int ISSUED_SUBKEYS_FIELD_NUMBER = 3;
		private int issuedSubkeys_;

		public boolean hasIssuedSubkeys() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public int getIssuedSubkeys() {
			return issuedSubkeys_;
		}

		public static final int LOOKAHEAD_SIZE_FIELD_NUMBER = 4;
		private int lookaheadSize_;

		public boolean hasLookaheadSize() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public int getLookaheadSize() {
			return lookaheadSize_;
		}

		public static final int ISFOLLOWING_FIELD_NUMBER = 5;
		private boolean isFollowing_;

		public boolean hasIsFollowing() {
			return ((bitField0_ & 0x00000008) == 0x00000008);
		}

		public boolean getIsFollowing() {
			return isFollowing_;
		}

		public static final int SIGSREQUIREDTOSPEND_FIELD_NUMBER = 6;
		private int sigsRequiredToSpend_;

		public boolean hasSigsRequiredToSpend() {
			return ((bitField0_ & 0x00000010) == 0x00000010);
		}

		public int getSigsRequiredToSpend() {
			return sigsRequiredToSpend_;
		}

		private void initFields() {
			chainCode_ = com.google.protobuf.ByteString.EMPTY;
			path_ = java.util.Collections.emptyList();
			issuedSubkeys_ = 0;
			lookaheadSize_ = 0;
			isFollowing_ = false;
			sigsRequiredToSpend_ = 1;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasChainCode()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeBytes(1, chainCode_);
			}
			for (int i = 0; i < path_.size(); i++) {
				output.writeUInt32(2, path_.get(i));
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeUInt32(3, issuedSubkeys_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeUInt32(4, lookaheadSize_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				output.writeBool(5, isFollowing_);
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				output.writeUInt32(6, sigsRequiredToSpend_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, chainCode_);
			}
			{
				int dataSize = 0;
				for (int i = 0; i < path_.size(); i++) {
					dataSize += com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(path_.get(i));
				}
				size += dataSize;
				size += 1 * getPathList().size();
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeUInt32Size(3, issuedSubkeys_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeUInt32Size(4, lookaheadSize_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				size += com.google.protobuf.CodedOutputStream.computeBoolSize(5, isFollowing_);
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				size += com.google.protobuf.CodedOutputStream.computeUInt32Size(6, sigsRequiredToSpend_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.DeterministicKey parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.DeterministicKey parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.DeterministicKey parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.DeterministicKey parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.DeterministicKey parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.DeterministicKey parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.DeterministicKey parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.DeterministicKey parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.DeterministicKey parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.DeterministicKey parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.DeterministicKey prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

			cros.mail.chain.wallet.Protos.DeterministicKeyOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_DeterministicKey_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_DeterministicKey_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.DeterministicKey.class,
								cros.mail.chain.wallet.Protos.DeterministicKey.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				chainCode_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000001);
				path_ = java.util.Collections.emptyList();
				bitField0_ = (bitField0_ & ~0x00000002);
				issuedSubkeys_ = 0;
				bitField0_ = (bitField0_ & ~0x00000004);
				lookaheadSize_ = 0;
				bitField0_ = (bitField0_ & ~0x00000008);
				isFollowing_ = false;
				bitField0_ = (bitField0_ & ~0x00000010);
				sigsRequiredToSpend_ = 1;
				bitField0_ = (bitField0_ & ~0x00000020);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_DeterministicKey_descriptor;
			}

			public cros.mail.chain.wallet.Protos.DeterministicKey getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.DeterministicKey.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.DeterministicKey build() {
				cros.mail.chain.wallet.Protos.DeterministicKey result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.DeterministicKey buildPartial() {
				cros.mail.chain.wallet.Protos.DeterministicKey result = new cros.mail.chain.wallet.Protos.DeterministicKey(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.chainCode_ = chainCode_;
				if (((bitField0_ & 0x00000002) == 0x00000002)) {
					path_ = java.util.Collections.unmodifiableList(path_);
					bitField0_ = (bitField0_ & ~0x00000002);
				}
				result.path_ = path_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000002;
				}
				result.issuedSubkeys_ = issuedSubkeys_;
				if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
					to_bitField0_ |= 0x00000004;
				}
				result.lookaheadSize_ = lookaheadSize_;
				if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
					to_bitField0_ |= 0x00000008;
				}
				result.isFollowing_ = isFollowing_;
				if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
					to_bitField0_ |= 0x00000010;
				}
				result.sigsRequiredToSpend_ = sigsRequiredToSpend_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.DeterministicKey) {
					return mergeFrom((cros.mail.chain.wallet.Protos.DeterministicKey) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.DeterministicKey other) {
				if (other == cros.mail.chain.wallet.Protos.DeterministicKey.getDefaultInstance())
					return this;
				if (other.hasChainCode()) {
					setChainCode(other.getChainCode());
				}
				if (!other.path_.isEmpty()) {
					if (path_.isEmpty()) {
						path_ = other.path_;
						bitField0_ = (bitField0_ & ~0x00000002);
					} else {
						ensurePathIsMutable();
						path_.addAll(other.path_);
					}
					onChanged();
				}
				if (other.hasIssuedSubkeys()) {
					setIssuedSubkeys(other.getIssuedSubkeys());
				}
				if (other.hasLookaheadSize()) {
					setLookaheadSize(other.getLookaheadSize());
				}
				if (other.hasIsFollowing()) {
					setIsFollowing(other.getIsFollowing());
				}
				if (other.hasSigsRequiredToSpend()) {
					setSigsRequiredToSpend(other.getSigsRequiredToSpend());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasChainCode()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.DeterministicKey parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.DeterministicKey) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private com.google.protobuf.ByteString chainCode_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasChainCode() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public com.google.protobuf.ByteString getChainCode() {
				return chainCode_;
			}

			public Builder setChainCode(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				chainCode_ = value;
				onChanged();
				return this;
			}

			public Builder clearChainCode() {
				bitField0_ = (bitField0_ & ~0x00000001);
				chainCode_ = getDefaultInstance().getChainCode();
				onChanged();
				return this;
			}

			private java.util.List<java.lang.Integer> path_ = java.util.Collections.emptyList();

			private void ensurePathIsMutable() {
				if (!((bitField0_ & 0x00000002) == 0x00000002)) {
					path_ = new java.util.ArrayList<java.lang.Integer>(path_);
					bitField0_ |= 0x00000002;
				}
			}

			public java.util.List<java.lang.Integer> getPathList() {
				return java.util.Collections.unmodifiableList(path_);
			}

			public int getPathCount() {
				return path_.size();
			}

			public int getPath(int index) {
				return path_.get(index);
			}

			public Builder setPath(int index, int value) {
				ensurePathIsMutable();
				path_.set(index, value);
				onChanged();
				return this;
			}

			public Builder addPath(int value) {
				ensurePathIsMutable();
				path_.add(value);
				onChanged();
				return this;
			}

			public Builder addAllPath(java.lang.Iterable<? extends java.lang.Integer> values) {
				ensurePathIsMutable();
				com.google.protobuf.AbstractMessageLite.Builder.addAll(values, path_);
				onChanged();
				return this;
			}

			public Builder clearPath() {
				path_ = java.util.Collections.emptyList();
				bitField0_ = (bitField0_ & ~0x00000002);
				onChanged();
				return this;
			}

			private int issuedSubkeys_;

			public boolean hasIssuedSubkeys() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public int getIssuedSubkeys() {
				return issuedSubkeys_;
			}

			public Builder setIssuedSubkeys(int value) {
				bitField0_ |= 0x00000004;
				issuedSubkeys_ = value;
				onChanged();
				return this;
			}

			public Builder clearIssuedSubkeys() {
				bitField0_ = (bitField0_ & ~0x00000004);
				issuedSubkeys_ = 0;
				onChanged();
				return this;
			}

			private int lookaheadSize_;

			public boolean hasLookaheadSize() {
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			public int getLookaheadSize() {
				return lookaheadSize_;
			}

			public Builder setLookaheadSize(int value) {
				bitField0_ |= 0x00000008;
				lookaheadSize_ = value;
				onChanged();
				return this;
			}

			public Builder clearLookaheadSize() {
				bitField0_ = (bitField0_ & ~0x00000008);
				lookaheadSize_ = 0;
				onChanged();
				return this;
			}

			private boolean isFollowing_;

			public boolean hasIsFollowing() {
				return ((bitField0_ & 0x00000010) == 0x00000010);
			}

			public boolean getIsFollowing() {
				return isFollowing_;
			}

			public Builder setIsFollowing(boolean value) {
				bitField0_ |= 0x00000010;
				isFollowing_ = value;
				onChanged();
				return this;
			}

			public Builder clearIsFollowing() {
				bitField0_ = (bitField0_ & ~0x00000010);
				isFollowing_ = false;
				onChanged();
				return this;
			}

			private int sigsRequiredToSpend_ = 1;

			public boolean hasSigsRequiredToSpend() {
				return ((bitField0_ & 0x00000020) == 0x00000020);
			}

			public int getSigsRequiredToSpend() {
				return sigsRequiredToSpend_;
			}

			public Builder setSigsRequiredToSpend(int value) {
				bitField0_ |= 0x00000020;
				sigsRequiredToSpend_ = value;
				onChanged();
				return this;
			}

			public Builder clearSigsRequiredToSpend() {
				bitField0_ = (bitField0_ & ~0x00000020);
				sigsRequiredToSpend_ = 1;
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new DeterministicKey(true);
			defaultInstance.initFields();
		}

	}

	public interface KeyOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasType();

		cros.mail.chain.wallet.Protos.Key.Type getType();

		boolean hasSecretBytes();

		com.google.protobuf.ByteString getSecretBytes();

		boolean hasEncryptedData();

		cros.mail.chain.wallet.Protos.EncryptedData getEncryptedData();

		cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder getEncryptedDataOrBuilder();

		boolean hasPublicKey();

		com.google.protobuf.ByteString getPublicKey();

		boolean hasLabel();

		java.lang.String getLabel();

		com.google.protobuf.ByteString getLabelBytes();

		boolean hasCreationTimestamp();

		long getCreationTimestamp();

		boolean hasDeterministicKey();

		cros.mail.chain.wallet.Protos.DeterministicKey getDeterministicKey();

		cros.mail.chain.wallet.Protos.DeterministicKeyOrBuilder getDeterministicKeyOrBuilder();

		boolean hasDeterministicSeed();

		com.google.protobuf.ByteString getDeterministicSeed();

		boolean hasEncryptedDeterministicSeed();

		cros.mail.chain.wallet.Protos.EncryptedData getEncryptedDeterministicSeed();

		cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder getEncryptedDeterministicSeedOrBuilder();
	}

	public static final class Key extends com.google.protobuf.GeneratedMessage implements

			KeyOrBuilder {

		private Key(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private Key(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final Key defaultInstance;

		public static Key getDefaultInstance() {
			return defaultInstance;
		}

		public Key getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private Key(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 8: {
						int rawValue = input.readEnum();
						cros.mail.chain.wallet.Protos.Key.Type value = cros.mail.chain.wallet.Protos.Key.Type.valueOf(rawValue);
						if (value == null) {
							unknownFields.mergeVarintField(1, rawValue);
						} else {
							bitField0_ |= 0x00000001;
							type_ = value;
						}
						break;
					}
					case 18: {
						bitField0_ |= 0x00000002;
						secretBytes_ = input.readBytes();
						break;
					}
					case 26: {
						bitField0_ |= 0x00000008;
						publicKey_ = input.readBytes();
						break;
					}
					case 34: {
						com.google.protobuf.ByteString bs = input.readBytes();
						bitField0_ |= 0x00000010;
						label_ = bs;
						break;
					}
					case 40: {
						bitField0_ |= 0x00000020;
						creationTimestamp_ = input.readInt64();
						break;
					}
					case 50: {
						cros.mail.chain.wallet.Protos.EncryptedData.Builder subBuilder = null;
						if (((bitField0_ & 0x00000004) == 0x00000004)) {
							subBuilder = encryptedData_.toBuilder();
						}
						encryptedData_ = input.readMessage(cros.mail.chain.wallet.Protos.EncryptedData.PARSER,
								extensionRegistry);
						if (subBuilder != null) {
							subBuilder.mergeFrom(encryptedData_);
							encryptedData_ = subBuilder.buildPartial();
						}
						bitField0_ |= 0x00000004;
						break;
					}
					case 58: {
						cros.mail.chain.wallet.Protos.DeterministicKey.Builder subBuilder = null;
						if (((bitField0_ & 0x00000040) == 0x00000040)) {
							subBuilder = deterministicKey_.toBuilder();
						}
						deterministicKey_ = input.readMessage(cros.mail.chain.wallet.Protos.DeterministicKey.PARSER,
								extensionRegistry);
						if (subBuilder != null) {
							subBuilder.mergeFrom(deterministicKey_);
							deterministicKey_ = subBuilder.buildPartial();
						}
						bitField0_ |= 0x00000040;
						break;
					}
					case 66: {
						bitField0_ |= 0x00000080;
						deterministicSeed_ = input.readBytes();
						break;
					}
					case 74: {
						cros.mail.chain.wallet.Protos.EncryptedData.Builder subBuilder = null;
						if (((bitField0_ & 0x00000100) == 0x00000100)) {
							subBuilder = encryptedDeterministicSeed_.toBuilder();
						}
						encryptedDeterministicSeed_ = input.readMessage(cros.mail.chain.wallet.Protos.EncryptedData.PARSER,
								extensionRegistry);
						if (subBuilder != null) {
							subBuilder.mergeFrom(encryptedDeterministicSeed_);
							encryptedDeterministicSeed_ = subBuilder.buildPartial();
						}
						bitField0_ |= 0x00000100;
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Key_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Key_fieldAccessorTable.ensureFieldAccessorsInitialized(
					cros.mail.chain.wallet.Protos.Key.class, cros.mail.chain.wallet.Protos.Key.Builder.class);
		}

		public static com.google.protobuf.Parser<Key> PARSER = new com.google.protobuf.AbstractParser<Key>() {
			public Key parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new Key(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<Key> getParserForType() {
			return PARSER;
		}

		public enum Type implements com.google.protobuf.ProtocolMessageEnum {

			ORIGINAL(0, 1),

			ENCRYPTED_SCRYPT_AES(1, 2),

			DETERMINISTIC_MNEMONIC(2, 3),

			DETERMINISTIC_KEY(3, 4),;

			public static final int ORIGINAL_VALUE = 1;

			public static final int ENCRYPTED_SCRYPT_AES_VALUE = 2;

			public static final int DETERMINISTIC_MNEMONIC_VALUE = 3;

			public static final int DETERMINISTIC_KEY_VALUE = 4;

			public final int getNumber() {
				return value;
			}

			public static Type valueOf(int value) {
				switch (value) {
				case 1:
					return ORIGINAL;
				case 2:
					return ENCRYPTED_SCRYPT_AES;
				case 3:
					return DETERMINISTIC_MNEMONIC;
				case 4:
					return DETERMINISTIC_KEY;
				default:
					return null;
				}
			}

			public static com.google.protobuf.Internal.EnumLiteMap<Type> internalGetValueMap() {
				return internalValueMap;
			}

			private static com.google.protobuf.Internal.EnumLiteMap<Type> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<Type>() {
				public Type findValueByNumber(int number) {
					return Type.valueOf(number);
				}
			};

			public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
				return getDescriptor().getValues().get(index);
			}

			public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
				return getDescriptor();
			}

			public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.Key.getDescriptor().getEnumTypes().get(0);
			}

			private static final Type[] VALUES = values();

			public static Type valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
				if (desc.getType() != getDescriptor()) {
					throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
				}
				return VALUES[desc.getIndex()];
			}

			private final int index;
			private final int value;

			private Type(int index, int value) {
				this.index = index;
				this.value = value;
			}

		}

		private int bitField0_;
		public static final int TYPE_FIELD_NUMBER = 1;
		private cros.mail.chain.wallet.Protos.Key.Type type_;

		public boolean hasType() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public cros.mail.chain.wallet.Protos.Key.Type getType() {
			return type_;
		}

		public static final int SECRET_BYTES_FIELD_NUMBER = 2;
		private com.google.protobuf.ByteString secretBytes_;

		public boolean hasSecretBytes() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public com.google.protobuf.ByteString getSecretBytes() {
			return secretBytes_;
		}

		public static final int ENCRYPTED_DATA_FIELD_NUMBER = 6;
		private cros.mail.chain.wallet.Protos.EncryptedData encryptedData_;

		public boolean hasEncryptedData() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public cros.mail.chain.wallet.Protos.EncryptedData getEncryptedData() {
			return encryptedData_;
		}

		public cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder getEncryptedDataOrBuilder() {
			return encryptedData_;
		}

		public static final int PUBLIC_KEY_FIELD_NUMBER = 3;
		private com.google.protobuf.ByteString publicKey_;

		public boolean hasPublicKey() {
			return ((bitField0_ & 0x00000008) == 0x00000008);
		}

		public com.google.protobuf.ByteString getPublicKey() {
			return publicKey_;
		}

		public static final int LABEL_FIELD_NUMBER = 4;
		private java.lang.Object label_;

		public boolean hasLabel() {
			return ((bitField0_ & 0x00000010) == 0x00000010);
		}

		public java.lang.String getLabel() {
			java.lang.Object ref = label_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8()) {
					label_ = s;
				}
				return s;
			}
		}

		public com.google.protobuf.ByteString getLabelBytes() {
			java.lang.Object ref = label_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				label_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}

		public static final int CREATION_TIMESTAMP_FIELD_NUMBER = 5;
		private long creationTimestamp_;

		public boolean hasCreationTimestamp() {
			return ((bitField0_ & 0x00000020) == 0x00000020);
		}

		public long getCreationTimestamp() {
			return creationTimestamp_;
		}

		public static final int DETERMINISTIC_KEY_FIELD_NUMBER = 7;
		private cros.mail.chain.wallet.Protos.DeterministicKey deterministicKey_;

		public boolean hasDeterministicKey() {
			return ((bitField0_ & 0x00000040) == 0x00000040);
		}

		public cros.mail.chain.wallet.Protos.DeterministicKey getDeterministicKey() {
			return deterministicKey_;
		}

		public cros.mail.chain.wallet.Protos.DeterministicKeyOrBuilder getDeterministicKeyOrBuilder() {
			return deterministicKey_;
		}

		public static final int DETERMINISTIC_SEED_FIELD_NUMBER = 8;
		private com.google.protobuf.ByteString deterministicSeed_;

		public boolean hasDeterministicSeed() {
			return ((bitField0_ & 0x00000080) == 0x00000080);
		}

		public com.google.protobuf.ByteString getDeterministicSeed() {
			return deterministicSeed_;
		}

		public static final int ENCRYPTED_DETERMINISTIC_SEED_FIELD_NUMBER = 9;
		private cros.mail.chain.wallet.Protos.EncryptedData encryptedDeterministicSeed_;

		public boolean hasEncryptedDeterministicSeed() {
			return ((bitField0_ & 0x00000100) == 0x00000100);
		}

		public cros.mail.chain.wallet.Protos.EncryptedData getEncryptedDeterministicSeed() {
			return encryptedDeterministicSeed_;
		}

		public cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder getEncryptedDeterministicSeedOrBuilder() {
			return encryptedDeterministicSeed_;
		}

		private void initFields() {
			type_ = cros.mail.chain.wallet.Protos.Key.Type.ORIGINAL;
			secretBytes_ = com.google.protobuf.ByteString.EMPTY;
			encryptedData_ = cros.mail.chain.wallet.Protos.EncryptedData.getDefaultInstance();
			publicKey_ = com.google.protobuf.ByteString.EMPTY;
			label_ = "";
			creationTimestamp_ = 0L;
			deterministicKey_ = cros.mail.chain.wallet.Protos.DeterministicKey.getDefaultInstance();
			deterministicSeed_ = com.google.protobuf.ByteString.EMPTY;
			encryptedDeterministicSeed_ = cros.mail.chain.wallet.Protos.EncryptedData.getDefaultInstance();
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasType()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (hasEncryptedData()) {
				if (!getEncryptedData().isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			if (hasDeterministicKey()) {
				if (!getDeterministicKey().isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			if (hasEncryptedDeterministicSeed()) {
				if (!getEncryptedDeterministicSeed().isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeEnum(1, type_.getNumber());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeBytes(2, secretBytes_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				output.writeBytes(3, publicKey_);
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				output.writeBytes(4, getLabelBytes());
			}
			if (((bitField0_ & 0x00000020) == 0x00000020)) {
				output.writeInt64(5, creationTimestamp_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeMessage(6, encryptedData_);
			}
			if (((bitField0_ & 0x00000040) == 0x00000040)) {
				output.writeMessage(7, deterministicKey_);
			}
			if (((bitField0_ & 0x00000080) == 0x00000080)) {
				output.writeBytes(8, deterministicSeed_);
			}
			if (((bitField0_ & 0x00000100) == 0x00000100)) {
				output.writeMessage(9, encryptedDeterministicSeed_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeEnumSize(1, type_.getNumber());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, secretBytes_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(3, publicKey_);
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(4, getLabelBytes());
			}
			if (((bitField0_ & 0x00000020) == 0x00000020)) {
				size += com.google.protobuf.CodedOutputStream.computeInt64Size(5, creationTimestamp_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(6, encryptedData_);
			}
			if (((bitField0_ & 0x00000040) == 0x00000040)) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(7, deterministicKey_);
			}
			if (((bitField0_ & 0x00000080) == 0x00000080)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(8, deterministicSeed_);
			}
			if (((bitField0_ & 0x00000100) == 0x00000100)) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(9, encryptedDeterministicSeed_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.Key parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Key parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Key parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Key parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Key parseFrom(java.io.InputStream input) throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Key parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Key parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Key parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Key parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Key parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.Key prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

		cros.mail.chain.wallet.Protos.KeyOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Key_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Key_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.Key.class,
								cros.mail.chain.wallet.Protos.Key.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
					getEncryptedDataFieldBuilder();
					getDeterministicKeyFieldBuilder();
					getEncryptedDeterministicSeedFieldBuilder();
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				type_ = cros.mail.chain.wallet.Protos.Key.Type.ORIGINAL;
				bitField0_ = (bitField0_ & ~0x00000001);
				secretBytes_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000002);
				if (encryptedDataBuilder_ == null) {
					encryptedData_ = cros.mail.chain.wallet.Protos.EncryptedData.getDefaultInstance();
				} else {
					encryptedDataBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000004);
				publicKey_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000008);
				label_ = "";
				bitField0_ = (bitField0_ & ~0x00000010);
				creationTimestamp_ = 0L;
				bitField0_ = (bitField0_ & ~0x00000020);
				if (deterministicKeyBuilder_ == null) {
					deterministicKey_ = cros.mail.chain.wallet.Protos.DeterministicKey.getDefaultInstance();
				} else {
					deterministicKeyBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000040);
				deterministicSeed_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000080);
				if (encryptedDeterministicSeedBuilder_ == null) {
					encryptedDeterministicSeed_ = cros.mail.chain.wallet.Protos.EncryptedData.getDefaultInstance();
				} else {
					encryptedDeterministicSeedBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000100);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Key_descriptor;
			}

			public cros.mail.chain.wallet.Protos.Key getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.Key.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.Key build() {
				cros.mail.chain.wallet.Protos.Key result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.Key buildPartial() {
				cros.mail.chain.wallet.Protos.Key result = new cros.mail.chain.wallet.Protos.Key(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.type_ = type_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.secretBytes_ = secretBytes_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000004;
				}
				if (encryptedDataBuilder_ == null) {
					result.encryptedData_ = encryptedData_;
				} else {
					result.encryptedData_ = encryptedDataBuilder_.build();
				}
				if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
					to_bitField0_ |= 0x00000008;
				}
				result.publicKey_ = publicKey_;
				if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
					to_bitField0_ |= 0x00000010;
				}
				result.label_ = label_;
				if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
					to_bitField0_ |= 0x00000020;
				}
				result.creationTimestamp_ = creationTimestamp_;
				if (((from_bitField0_ & 0x00000040) == 0x00000040)) {
					to_bitField0_ |= 0x00000040;
				}
				if (deterministicKeyBuilder_ == null) {
					result.deterministicKey_ = deterministicKey_;
				} else {
					result.deterministicKey_ = deterministicKeyBuilder_.build();
				}
				if (((from_bitField0_ & 0x00000080) == 0x00000080)) {
					to_bitField0_ |= 0x00000080;
				}
				result.deterministicSeed_ = deterministicSeed_;
				if (((from_bitField0_ & 0x00000100) == 0x00000100)) {
					to_bitField0_ |= 0x00000100;
				}
				if (encryptedDeterministicSeedBuilder_ == null) {
					result.encryptedDeterministicSeed_ = encryptedDeterministicSeed_;
				} else {
					result.encryptedDeterministicSeed_ = encryptedDeterministicSeedBuilder_.build();
				}
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.Key) {
					return mergeFrom((cros.mail.chain.wallet.Protos.Key) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.Key other) {
				if (other == cros.mail.chain.wallet.Protos.Key.getDefaultInstance())
					return this;
				if (other.hasType()) {
					setType(other.getType());
				}
				if (other.hasSecretBytes()) {
					setSecretBytes(other.getSecretBytes());
				}
				if (other.hasEncryptedData()) {
					mergeEncryptedData(other.getEncryptedData());
				}
				if (other.hasPublicKey()) {
					setPublicKey(other.getPublicKey());
				}
				if (other.hasLabel()) {
					bitField0_ |= 0x00000010;
					label_ = other.label_;
					onChanged();
				}
				if (other.hasCreationTimestamp()) {
					setCreationTimestamp(other.getCreationTimestamp());
				}
				if (other.hasDeterministicKey()) {
					mergeDeterministicKey(other.getDeterministicKey());
				}
				if (other.hasDeterministicSeed()) {
					setDeterministicSeed(other.getDeterministicSeed());
				}
				if (other.hasEncryptedDeterministicSeed()) {
					mergeEncryptedDeterministicSeed(other.getEncryptedDeterministicSeed());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasType()) {

					return false;
				}
				if (hasEncryptedData()) {
					if (!getEncryptedData().isInitialized()) {

						return false;
					}
				}
				if (hasDeterministicKey()) {
					if (!getDeterministicKey().isInitialized()) {

						return false;
					}
				}
				if (hasEncryptedDeterministicSeed()) {
					if (!getEncryptedDeterministicSeed().isInitialized()) {

						return false;
					}
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.Key parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.Key) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private cros.mail.chain.wallet.Protos.Key.Type type_ = cros.mail.chain.wallet.Protos.Key.Type.ORIGINAL;

			public boolean hasType() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public cros.mail.chain.wallet.Protos.Key.Type getType() {
				return type_;
			}

			public Builder setType(cros.mail.chain.wallet.Protos.Key.Type value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				type_ = value;
				onChanged();
				return this;
			}

			public Builder clearType() {
				bitField0_ = (bitField0_ & ~0x00000001);
				type_ = cros.mail.chain.wallet.Protos.Key.Type.ORIGINAL;
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString secretBytes_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasSecretBytes() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public com.google.protobuf.ByteString getSecretBytes() {
				return secretBytes_;
			}

			public Builder setSecretBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000002;
				secretBytes_ = value;
				onChanged();
				return this;
			}

			public Builder clearSecretBytes() {
				bitField0_ = (bitField0_ & ~0x00000002);
				secretBytes_ = getDefaultInstance().getSecretBytes();
				onChanged();
				return this;
			}

			private cros.mail.chain.wallet.Protos.EncryptedData encryptedData_ = cros.mail.chain.wallet.Protos.EncryptedData
					.getDefaultInstance();
			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.EncryptedData, cros.mail.chain.wallet.Protos.EncryptedData.Builder, cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder> encryptedDataBuilder_;

			public boolean hasEncryptedData() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public cros.mail.chain.wallet.Protos.EncryptedData getEncryptedData() {
				if (encryptedDataBuilder_ == null) {
					return encryptedData_;
				} else {
					return encryptedDataBuilder_.getMessage();
				}
			}

			public Builder setEncryptedData(cros.mail.chain.wallet.Protos.EncryptedData value) {
				if (encryptedDataBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					encryptedData_ = value;
					onChanged();
				} else {
					encryptedDataBuilder_.setMessage(value);
				}
				bitField0_ |= 0x00000004;
				return this;
			}

			public Builder setEncryptedData(cros.mail.chain.wallet.Protos.EncryptedData.Builder builderForValue) {
				if (encryptedDataBuilder_ == null) {
					encryptedData_ = builderForValue.build();
					onChanged();
				} else {
					encryptedDataBuilder_.setMessage(builderForValue.build());
				}
				bitField0_ |= 0x00000004;
				return this;
			}

			public Builder mergeEncryptedData(cros.mail.chain.wallet.Protos.EncryptedData value) {
				if (encryptedDataBuilder_ == null) {
					if (((bitField0_ & 0x00000004) == 0x00000004)
							&& encryptedData_ != cros.mail.chain.wallet.Protos.EncryptedData.getDefaultInstance()) {
						encryptedData_ = cros.mail.chain.wallet.Protos.EncryptedData.newBuilder(encryptedData_)
								.mergeFrom(value).buildPartial();
					} else {
						encryptedData_ = value;
					}
					onChanged();
				} else {
					encryptedDataBuilder_.mergeFrom(value);
				}
				bitField0_ |= 0x00000004;
				return this;
			}

			public Builder clearEncryptedData() {
				if (encryptedDataBuilder_ == null) {
					encryptedData_ = cros.mail.chain.wallet.Protos.EncryptedData.getDefaultInstance();
					onChanged();
				} else {
					encryptedDataBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000004);
				return this;
			}

			public cros.mail.chain.wallet.Protos.EncryptedData.Builder getEncryptedDataBuilder() {
				bitField0_ |= 0x00000004;
				onChanged();
				return getEncryptedDataFieldBuilder().getBuilder();
			}

			public cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder getEncryptedDataOrBuilder() {
				if (encryptedDataBuilder_ != null) {
					return encryptedDataBuilder_.getMessageOrBuilder();
				} else {
					return encryptedData_;
				}
			}

			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.EncryptedData, cros.mail.chain.wallet.Protos.EncryptedData.Builder, cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder> getEncryptedDataFieldBuilder() {
				if (encryptedDataBuilder_ == null) {
					encryptedDataBuilder_ = new com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.EncryptedData, cros.mail.chain.wallet.Protos.EncryptedData.Builder, cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder>(
							getEncryptedData(), getParentForChildren(), isClean());
					encryptedData_ = null;
				}
				return encryptedDataBuilder_;
			}

			private com.google.protobuf.ByteString publicKey_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasPublicKey() {
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			public com.google.protobuf.ByteString getPublicKey() {
				return publicKey_;
			}

			public Builder setPublicKey(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000008;
				publicKey_ = value;
				onChanged();
				return this;
			}

			public Builder clearPublicKey() {
				bitField0_ = (bitField0_ & ~0x00000008);
				publicKey_ = getDefaultInstance().getPublicKey();
				onChanged();
				return this;
			}

			private java.lang.Object label_ = "";

			public boolean hasLabel() {
				return ((bitField0_ & 0x00000010) == 0x00000010);
			}

			public java.lang.String getLabel() {
				java.lang.Object ref = label_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						label_ = s;
					}
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}

			public com.google.protobuf.ByteString getLabelBytes() {
				java.lang.Object ref = label_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					label_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			public Builder setLabel(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000010;
				label_ = value;
				onChanged();
				return this;
			}

			public Builder clearLabel() {
				bitField0_ = (bitField0_ & ~0x00000010);
				label_ = getDefaultInstance().getLabel();
				onChanged();
				return this;
			}

			public Builder setLabelBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000010;
				label_ = value;
				onChanged();
				return this;
			}

			private long creationTimestamp_;

			public boolean hasCreationTimestamp() {
				return ((bitField0_ & 0x00000020) == 0x00000020);
			}

			public long getCreationTimestamp() {
				return creationTimestamp_;
			}

			public Builder setCreationTimestamp(long value) {
				bitField0_ |= 0x00000020;
				creationTimestamp_ = value;
				onChanged();
				return this;
			}

			public Builder clearCreationTimestamp() {
				bitField0_ = (bitField0_ & ~0x00000020);
				creationTimestamp_ = 0L;
				onChanged();
				return this;
			}

			private cros.mail.chain.wallet.Protos.DeterministicKey deterministicKey_ = cros.mail.chain.wallet.Protos.DeterministicKey
					.getDefaultInstance();
			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.DeterministicKey, cros.mail.chain.wallet.Protos.DeterministicKey.Builder, cros.mail.chain.wallet.Protos.DeterministicKeyOrBuilder> deterministicKeyBuilder_;

			public boolean hasDeterministicKey() {
				return ((bitField0_ & 0x00000040) == 0x00000040);
			}

			public cros.mail.chain.wallet.Protos.DeterministicKey getDeterministicKey() {
				if (deterministicKeyBuilder_ == null) {
					return deterministicKey_;
				} else {
					return deterministicKeyBuilder_.getMessage();
				}
			}

			public Builder setDeterministicKey(cros.mail.chain.wallet.Protos.DeterministicKey value) {
				if (deterministicKeyBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					deterministicKey_ = value;
					onChanged();
				} else {
					deterministicKeyBuilder_.setMessage(value);
				}
				bitField0_ |= 0x00000040;
				return this;
			}

			public Builder setDeterministicKey(cros.mail.chain.wallet.Protos.DeterministicKey.Builder builderForValue) {
				if (deterministicKeyBuilder_ == null) {
					deterministicKey_ = builderForValue.build();
					onChanged();
				} else {
					deterministicKeyBuilder_.setMessage(builderForValue.build());
				}
				bitField0_ |= 0x00000040;
				return this;
			}

			public Builder mergeDeterministicKey(cros.mail.chain.wallet.Protos.DeterministicKey value) {
				if (deterministicKeyBuilder_ == null) {
					if (((bitField0_ & 0x00000040) == 0x00000040)
							&& deterministicKey_ != cros.mail.chain.wallet.Protos.DeterministicKey.getDefaultInstance()) {
						deterministicKey_ = cros.mail.chain.wallet.Protos.DeterministicKey.newBuilder(deterministicKey_)
								.mergeFrom(value).buildPartial();
					} else {
						deterministicKey_ = value;
					}
					onChanged();
				} else {
					deterministicKeyBuilder_.mergeFrom(value);
				}
				bitField0_ |= 0x00000040;
				return this;
			}

			public Builder clearDeterministicKey() {
				if (deterministicKeyBuilder_ == null) {
					deterministicKey_ = cros.mail.chain.wallet.Protos.DeterministicKey.getDefaultInstance();
					onChanged();
				} else {
					deterministicKeyBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000040);
				return this;
			}

			public cros.mail.chain.wallet.Protos.DeterministicKey.Builder getDeterministicKeyBuilder() {
				bitField0_ |= 0x00000040;
				onChanged();
				return getDeterministicKeyFieldBuilder().getBuilder();
			}

			public cros.mail.chain.wallet.Protos.DeterministicKeyOrBuilder getDeterministicKeyOrBuilder() {
				if (deterministicKeyBuilder_ != null) {
					return deterministicKeyBuilder_.getMessageOrBuilder();
				} else {
					return deterministicKey_;
				}
			}

			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.DeterministicKey, cros.mail.chain.wallet.Protos.DeterministicKey.Builder, cros.mail.chain.wallet.Protos.DeterministicKeyOrBuilder> getDeterministicKeyFieldBuilder() {
				if (deterministicKeyBuilder_ == null) {
					deterministicKeyBuilder_ = new com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.DeterministicKey, cros.mail.chain.wallet.Protos.DeterministicKey.Builder, cros.mail.chain.wallet.Protos.DeterministicKeyOrBuilder>(
							getDeterministicKey(), getParentForChildren(), isClean());
					deterministicKey_ = null;
				}
				return deterministicKeyBuilder_;
			}

			private com.google.protobuf.ByteString deterministicSeed_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasDeterministicSeed() {
				return ((bitField0_ & 0x00000080) == 0x00000080);
			}

			public com.google.protobuf.ByteString getDeterministicSeed() {
				return deterministicSeed_;
			}

			public Builder setDeterministicSeed(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000080;
				deterministicSeed_ = value;
				onChanged();
				return this;
			}

			public Builder clearDeterministicSeed() {
				bitField0_ = (bitField0_ & ~0x00000080);
				deterministicSeed_ = getDefaultInstance().getDeterministicSeed();
				onChanged();
				return this;
			}

			private cros.mail.chain.wallet.Protos.EncryptedData encryptedDeterministicSeed_ = cros.mail.chain.wallet.Protos.EncryptedData
					.getDefaultInstance();
			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.EncryptedData, cros.mail.chain.wallet.Protos.EncryptedData.Builder, cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder> encryptedDeterministicSeedBuilder_;

			public boolean hasEncryptedDeterministicSeed() {
				return ((bitField0_ & 0x00000100) == 0x00000100);
			}

			public cros.mail.chain.wallet.Protos.EncryptedData getEncryptedDeterministicSeed() {
				if (encryptedDeterministicSeedBuilder_ == null) {
					return encryptedDeterministicSeed_;
				} else {
					return encryptedDeterministicSeedBuilder_.getMessage();
				}
			}

			public Builder setEncryptedDeterministicSeed(cros.mail.chain.wallet.Protos.EncryptedData value) {
				if (encryptedDeterministicSeedBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					encryptedDeterministicSeed_ = value;
					onChanged();
				} else {
					encryptedDeterministicSeedBuilder_.setMessage(value);
				}
				bitField0_ |= 0x00000100;
				return this;
			}

			public Builder setEncryptedDeterministicSeed(cros.mail.chain.wallet.Protos.EncryptedData.Builder builderForValue) {
				if (encryptedDeterministicSeedBuilder_ == null) {
					encryptedDeterministicSeed_ = builderForValue.build();
					onChanged();
				} else {
					encryptedDeterministicSeedBuilder_.setMessage(builderForValue.build());
				}
				bitField0_ |= 0x00000100;
				return this;
			}

			public Builder mergeEncryptedDeterministicSeed(cros.mail.chain.wallet.Protos.EncryptedData value) {
				if (encryptedDeterministicSeedBuilder_ == null) {
					if (((bitField0_ & 0x00000100) == 0x00000100)
							&& encryptedDeterministicSeed_ != cros.mail.chain.wallet.Protos.EncryptedData
									.getDefaultInstance()) {
						encryptedDeterministicSeed_ = cros.mail.chain.wallet.Protos.EncryptedData
								.newBuilder(encryptedDeterministicSeed_).mergeFrom(value).buildPartial();
					} else {
						encryptedDeterministicSeed_ = value;
					}
					onChanged();
				} else {
					encryptedDeterministicSeedBuilder_.mergeFrom(value);
				}
				bitField0_ |= 0x00000100;
				return this;
			}

			public Builder clearEncryptedDeterministicSeed() {
				if (encryptedDeterministicSeedBuilder_ == null) {
					encryptedDeterministicSeed_ = cros.mail.chain.wallet.Protos.EncryptedData.getDefaultInstance();
					onChanged();
				} else {
					encryptedDeterministicSeedBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000100);
				return this;
			}

			public cros.mail.chain.wallet.Protos.EncryptedData.Builder getEncryptedDeterministicSeedBuilder() {
				bitField0_ |= 0x00000100;
				onChanged();
				return getEncryptedDeterministicSeedFieldBuilder().getBuilder();
			}

			public cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder getEncryptedDeterministicSeedOrBuilder() {
				if (encryptedDeterministicSeedBuilder_ != null) {
					return encryptedDeterministicSeedBuilder_.getMessageOrBuilder();
				} else {
					return encryptedDeterministicSeed_;
				}
			}

			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.EncryptedData, cros.mail.chain.wallet.Protos.EncryptedData.Builder, cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder> getEncryptedDeterministicSeedFieldBuilder() {
				if (encryptedDeterministicSeedBuilder_ == null) {
					encryptedDeterministicSeedBuilder_ = new com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.EncryptedData, cros.mail.chain.wallet.Protos.EncryptedData.Builder, cros.mail.chain.wallet.Protos.EncryptedDataOrBuilder>(
							getEncryptedDeterministicSeed(), getParentForChildren(), isClean());
					encryptedDeterministicSeed_ = null;
				}
				return encryptedDeterministicSeedBuilder_;
			}

		}

		static {
			defaultInstance = new Key(true);
			defaultInstance.initFields();
		}

	}

	public interface ScriptOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasProgram();

		com.google.protobuf.ByteString getProgram();

		boolean hasCreationTimestamp();

		long getCreationTimestamp();
	}

	public static final class Script extends com.google.protobuf.GeneratedMessage implements

			ScriptOrBuilder {

		private Script(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private Script(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final Script defaultInstance;

		public static Script getDefaultInstance() {
			return defaultInstance;
		}

		public Script getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private Script(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 10: {
						bitField0_ |= 0x00000001;
						program_ = input.readBytes();
						break;
					}
					case 16: {
						bitField0_ |= 0x00000002;
						creationTimestamp_ = input.readInt64();
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Script_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Script_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.Script.class,
							cros.mail.chain.wallet.Protos.Script.Builder.class);
		}

		public static com.google.protobuf.Parser<Script> PARSER = new com.google.protobuf.AbstractParser<Script>() {
			public Script parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new Script(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<Script> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int PROGRAM_FIELD_NUMBER = 1;
		private com.google.protobuf.ByteString program_;

		public boolean hasProgram() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public com.google.protobuf.ByteString getProgram() {
			return program_;
		}

		public static final int CREATION_TIMESTAMP_FIELD_NUMBER = 2;
		private long creationTimestamp_;

		public boolean hasCreationTimestamp() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public long getCreationTimestamp() {
			return creationTimestamp_;
		}

		private void initFields() {
			program_ = com.google.protobuf.ByteString.EMPTY;
			creationTimestamp_ = 0L;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasProgram()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasCreationTimestamp()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeBytes(1, program_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeInt64(2, creationTimestamp_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, program_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeInt64Size(2, creationTimestamp_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.Script parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Script parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Script parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Script parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Script parseFrom(java.io.InputStream input) throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Script parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Script parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Script parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Script parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Script parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.Script prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

		cros.mail.chain.wallet.Protos.ScriptOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Script_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Script_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.Script.class,
								cros.mail.chain.wallet.Protos.Script.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				program_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000001);
				creationTimestamp_ = 0L;
				bitField0_ = (bitField0_ & ~0x00000002);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Script_descriptor;
			}

			public cros.mail.chain.wallet.Protos.Script getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.Script.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.Script build() {
				cros.mail.chain.wallet.Protos.Script result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.Script buildPartial() {
				cros.mail.chain.wallet.Protos.Script result = new cros.mail.chain.wallet.Protos.Script(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.program_ = program_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.creationTimestamp_ = creationTimestamp_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.Script) {
					return mergeFrom((cros.mail.chain.wallet.Protos.Script) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.Script other) {
				if (other == cros.mail.chain.wallet.Protos.Script.getDefaultInstance())
					return this;
				if (other.hasProgram()) {
					setProgram(other.getProgram());
				}
				if (other.hasCreationTimestamp()) {
					setCreationTimestamp(other.getCreationTimestamp());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasProgram()) {

					return false;
				}
				if (!hasCreationTimestamp()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.Script parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.Script) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private com.google.protobuf.ByteString program_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasProgram() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public com.google.protobuf.ByteString getProgram() {
				return program_;
			}

			public Builder setProgram(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				program_ = value;
				onChanged();
				return this;
			}

			public Builder clearProgram() {
				bitField0_ = (bitField0_ & ~0x00000001);
				program_ = getDefaultInstance().getProgram();
				onChanged();
				return this;
			}

			private long creationTimestamp_;

			public boolean hasCreationTimestamp() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public long getCreationTimestamp() {
				return creationTimestamp_;
			}

			public Builder setCreationTimestamp(long value) {
				bitField0_ |= 0x00000002;
				creationTimestamp_ = value;
				onChanged();
				return this;
			}

			public Builder clearCreationTimestamp() {
				bitField0_ = (bitField0_ & ~0x00000002);
				creationTimestamp_ = 0L;
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new Script(true);
			defaultInstance.initFields();
		}

	}

	public interface TransactionInputOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasTransactionOutPointHash();

		com.google.protobuf.ByteString getTransactionOutPointHash();

		boolean hasTransactionOutPointIndex();

		int getTransactionOutPointIndex();

		boolean hasScriptBytes();

		com.google.protobuf.ByteString getScriptBytes();

		boolean hasSequence();

		int getSequence();

		boolean hasValue();

		long getValue();
	}

	public static final class TransactionInput extends com.google.protobuf.GeneratedMessage implements

			TransactionInputOrBuilder {

		private TransactionInput(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private TransactionInput(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final TransactionInput defaultInstance;

		public static TransactionInput getDefaultInstance() {
			return defaultInstance;
		}

		public TransactionInput getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private TransactionInput(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 10: {
						bitField0_ |= 0x00000001;
						transactionOutPointHash_ = input.readBytes();
						break;
					}
					case 16: {
						bitField0_ |= 0x00000002;
						transactionOutPointIndex_ = input.readUInt32();
						break;
					}
					case 26: {
						bitField0_ |= 0x00000004;
						scriptBytes_ = input.readBytes();
						break;
					}
					case 32: {
						bitField0_ |= 0x00000008;
						sequence_ = input.readUInt32();
						break;
					}
					case 40: {
						bitField0_ |= 0x00000010;
						value_ = input.readInt64();
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionInput_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionInput_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.TransactionInput.class,
							cros.mail.chain.wallet.Protos.TransactionInput.Builder.class);
		}

		public static com.google.protobuf.Parser<TransactionInput> PARSER = new com.google.protobuf.AbstractParser<TransactionInput>() {
			public TransactionInput parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new TransactionInput(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<TransactionInput> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int TRANSACTION_OUT_POINT_HASH_FIELD_NUMBER = 1;
		private com.google.protobuf.ByteString transactionOutPointHash_;

		public boolean hasTransactionOutPointHash() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public com.google.protobuf.ByteString getTransactionOutPointHash() {
			return transactionOutPointHash_;
		}

		public static final int TRANSACTION_OUT_POINT_INDEX_FIELD_NUMBER = 2;
		private int transactionOutPointIndex_;

		public boolean hasTransactionOutPointIndex() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public int getTransactionOutPointIndex() {
			return transactionOutPointIndex_;
		}

		public static final int SCRIPT_BYTES_FIELD_NUMBER = 3;
		private com.google.protobuf.ByteString scriptBytes_;

		public boolean hasScriptBytes() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public com.google.protobuf.ByteString getScriptBytes() {
			return scriptBytes_;
		}

		public static final int SEQUENCE_FIELD_NUMBER = 4;
		private int sequence_;

		public boolean hasSequence() {
			return ((bitField0_ & 0x00000008) == 0x00000008);
		}

		public int getSequence() {
			return sequence_;
		}

		public static final int VALUE_FIELD_NUMBER = 5;
		private long value_;

		public boolean hasValue() {
			return ((bitField0_ & 0x00000010) == 0x00000010);
		}

		public long getValue() {
			return value_;
		}

		private void initFields() {
			transactionOutPointHash_ = com.google.protobuf.ByteString.EMPTY;
			transactionOutPointIndex_ = 0;
			scriptBytes_ = com.google.protobuf.ByteString.EMPTY;
			sequence_ = 0;
			value_ = 0L;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasTransactionOutPointHash()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasTransactionOutPointIndex()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasScriptBytes()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeBytes(1, transactionOutPointHash_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeUInt32(2, transactionOutPointIndex_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeBytes(3, scriptBytes_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				output.writeUInt32(4, sequence_);
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				output.writeInt64(5, value_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, transactionOutPointHash_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeUInt32Size(2, transactionOutPointIndex_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(3, scriptBytes_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				size += com.google.protobuf.CodedOutputStream.computeUInt32Size(4, sequence_);
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				size += com.google.protobuf.CodedOutputStream.computeInt64Size(5, value_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.TransactionInput parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.TransactionInput parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionInput parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.TransactionInput parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionInput parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionInput parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionInput parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionInput parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionInput parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionInput parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.TransactionInput prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

		cros.mail.chain.wallet.Protos.TransactionInputOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionInput_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionInput_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.TransactionInput.class,
								cros.mail.chain.wallet.Protos.TransactionInput.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				transactionOutPointHash_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000001);
				transactionOutPointIndex_ = 0;
				bitField0_ = (bitField0_ & ~0x00000002);
				scriptBytes_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000004);
				sequence_ = 0;
				bitField0_ = (bitField0_ & ~0x00000008);
				value_ = 0L;
				bitField0_ = (bitField0_ & ~0x00000010);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionInput_descriptor;
			}

			public cros.mail.chain.wallet.Protos.TransactionInput getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.TransactionInput.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.TransactionInput build() {
				cros.mail.chain.wallet.Protos.TransactionInput result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.TransactionInput buildPartial() {
				cros.mail.chain.wallet.Protos.TransactionInput result = new cros.mail.chain.wallet.Protos.TransactionInput(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.transactionOutPointHash_ = transactionOutPointHash_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.transactionOutPointIndex_ = transactionOutPointIndex_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000004;
				}
				result.scriptBytes_ = scriptBytes_;
				if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
					to_bitField0_ |= 0x00000008;
				}
				result.sequence_ = sequence_;
				if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
					to_bitField0_ |= 0x00000010;
				}
				result.value_ = value_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.TransactionInput) {
					return mergeFrom((cros.mail.chain.wallet.Protos.TransactionInput) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.TransactionInput other) {
				if (other == cros.mail.chain.wallet.Protos.TransactionInput.getDefaultInstance())
					return this;
				if (other.hasTransactionOutPointHash()) {
					setTransactionOutPointHash(other.getTransactionOutPointHash());
				}
				if (other.hasTransactionOutPointIndex()) {
					setTransactionOutPointIndex(other.getTransactionOutPointIndex());
				}
				if (other.hasScriptBytes()) {
					setScriptBytes(other.getScriptBytes());
				}
				if (other.hasSequence()) {
					setSequence(other.getSequence());
				}
				if (other.hasValue()) {
					setValue(other.getValue());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasTransactionOutPointHash()) {

					return false;
				}
				if (!hasTransactionOutPointIndex()) {

					return false;
				}
				if (!hasScriptBytes()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.TransactionInput parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.TransactionInput) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private com.google.protobuf.ByteString transactionOutPointHash_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasTransactionOutPointHash() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public com.google.protobuf.ByteString getTransactionOutPointHash() {
				return transactionOutPointHash_;
			}

			public Builder setTransactionOutPointHash(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				transactionOutPointHash_ = value;
				onChanged();
				return this;
			}

			public Builder clearTransactionOutPointHash() {
				bitField0_ = (bitField0_ & ~0x00000001);
				transactionOutPointHash_ = getDefaultInstance().getTransactionOutPointHash();
				onChanged();
				return this;
			}

			private int transactionOutPointIndex_;

			public boolean hasTransactionOutPointIndex() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public int getTransactionOutPointIndex() {
				return transactionOutPointIndex_;
			}

			public Builder setTransactionOutPointIndex(int value) {
				bitField0_ |= 0x00000002;
				transactionOutPointIndex_ = value;
				onChanged();
				return this;
			}

			public Builder clearTransactionOutPointIndex() {
				bitField0_ = (bitField0_ & ~0x00000002);
				transactionOutPointIndex_ = 0;
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString scriptBytes_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasScriptBytes() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public com.google.protobuf.ByteString getScriptBytes() {
				return scriptBytes_;
			}

			public Builder setScriptBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000004;
				scriptBytes_ = value;
				onChanged();
				return this;
			}

			public Builder clearScriptBytes() {
				bitField0_ = (bitField0_ & ~0x00000004);
				scriptBytes_ = getDefaultInstance().getScriptBytes();
				onChanged();
				return this;
			}

			private int sequence_;

			public boolean hasSequence() {
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			public int getSequence() {
				return sequence_;
			}

			public Builder setSequence(int value) {
				bitField0_ |= 0x00000008;
				sequence_ = value;
				onChanged();
				return this;
			}

			public Builder clearSequence() {
				bitField0_ = (bitField0_ & ~0x00000008);
				sequence_ = 0;
				onChanged();
				return this;
			}

			private long value_;

			public boolean hasValue() {
				return ((bitField0_ & 0x00000010) == 0x00000010);
			}

			public long getValue() {
				return value_;
			}

			public Builder setValue(long value) {
				bitField0_ |= 0x00000010;
				value_ = value;
				onChanged();
				return this;
			}

			public Builder clearValue() {
				bitField0_ = (bitField0_ & ~0x00000010);
				value_ = 0L;
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new TransactionInput(true);
			defaultInstance.initFields();
		}

	}

	public interface TransactionOutputOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasValue();

		long getValue();

		boolean hasScriptBytes();

		com.google.protobuf.ByteString getScriptBytes();

		boolean hasSpentByTransactionHash();

		com.google.protobuf.ByteString getSpentByTransactionHash();

		boolean hasSpentByTransactionIndex();

		int getSpentByTransactionIndex();
	}

	public static final class TransactionOutput extends com.google.protobuf.GeneratedMessage implements

			TransactionOutputOrBuilder {

		private TransactionOutput(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private TransactionOutput(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final TransactionOutput defaultInstance;

		public static TransactionOutput getDefaultInstance() {
			return defaultInstance;
		}

		public TransactionOutput getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private TransactionOutput(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 8: {
						bitField0_ |= 0x00000001;
						value_ = input.readInt64();
						break;
					}
					case 18: {
						bitField0_ |= 0x00000002;
						scriptBytes_ = input.readBytes();
						break;
					}
					case 26: {
						bitField0_ |= 0x00000004;
						spentByTransactionHash_ = input.readBytes();
						break;
					}
					case 32: {
						bitField0_ |= 0x00000008;
						spentByTransactionIndex_ = input.readInt32();
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionOutput_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionOutput_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.TransactionOutput.class,
							cros.mail.chain.wallet.Protos.TransactionOutput.Builder.class);
		}

		public static com.google.protobuf.Parser<TransactionOutput> PARSER = new com.google.protobuf.AbstractParser<TransactionOutput>() {
			public TransactionOutput parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new TransactionOutput(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<TransactionOutput> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int VALUE_FIELD_NUMBER = 1;
		private long value_;

		public boolean hasValue() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public long getValue() {
			return value_;
		}

		public static final int SCRIPT_BYTES_FIELD_NUMBER = 2;
		private com.google.protobuf.ByteString scriptBytes_;

		public boolean hasScriptBytes() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public com.google.protobuf.ByteString getScriptBytes() {
			return scriptBytes_;
		}

		public static final int SPENT_BY_TRANSACTION_HASH_FIELD_NUMBER = 3;
		private com.google.protobuf.ByteString spentByTransactionHash_;

		public boolean hasSpentByTransactionHash() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public com.google.protobuf.ByteString getSpentByTransactionHash() {
			return spentByTransactionHash_;
		}

		public static final int SPENT_BY_TRANSACTION_INDEX_FIELD_NUMBER = 4;
		private int spentByTransactionIndex_;

		public boolean hasSpentByTransactionIndex() {
			return ((bitField0_ & 0x00000008) == 0x00000008);
		}

		public int getSpentByTransactionIndex() {
			return spentByTransactionIndex_;
		}

		private void initFields() {
			value_ = 0L;
			scriptBytes_ = com.google.protobuf.ByteString.EMPTY;
			spentByTransactionHash_ = com.google.protobuf.ByteString.EMPTY;
			spentByTransactionIndex_ = 0;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasValue()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasScriptBytes()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeInt64(1, value_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeBytes(2, scriptBytes_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeBytes(3, spentByTransactionHash_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				output.writeInt32(4, spentByTransactionIndex_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeInt64Size(1, value_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, scriptBytes_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(3, spentByTransactionHash_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				size += com.google.protobuf.CodedOutputStream.computeInt32Size(4, spentByTransactionIndex_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.TransactionOutput parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.TransactionOutput parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionOutput parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.TransactionOutput parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionOutput parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionOutput parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionOutput parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionOutput parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionOutput parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionOutput parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.TransactionOutput prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

		cros.mail.chain.wallet.Protos.TransactionOutputOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionOutput_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionOutput_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.TransactionOutput.class,
								cros.mail.chain.wallet.Protos.TransactionOutput.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				value_ = 0L;
				bitField0_ = (bitField0_ & ~0x00000001);
				scriptBytes_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000002);
				spentByTransactionHash_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000004);
				spentByTransactionIndex_ = 0;
				bitField0_ = (bitField0_ & ~0x00000008);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionOutput_descriptor;
			}

			public cros.mail.chain.wallet.Protos.TransactionOutput getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.TransactionOutput.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.TransactionOutput build() {
				cros.mail.chain.wallet.Protos.TransactionOutput result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.TransactionOutput buildPartial() {
				cros.mail.chain.wallet.Protos.TransactionOutput result = new cros.mail.chain.wallet.Protos.TransactionOutput(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.value_ = value_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.scriptBytes_ = scriptBytes_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000004;
				}
				result.spentByTransactionHash_ = spentByTransactionHash_;
				if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
					to_bitField0_ |= 0x00000008;
				}
				result.spentByTransactionIndex_ = spentByTransactionIndex_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.TransactionOutput) {
					return mergeFrom((cros.mail.chain.wallet.Protos.TransactionOutput) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.TransactionOutput other) {
				if (other == cros.mail.chain.wallet.Protos.TransactionOutput.getDefaultInstance())
					return this;
				if (other.hasValue()) {
					setValue(other.getValue());
				}
				if (other.hasScriptBytes()) {
					setScriptBytes(other.getScriptBytes());
				}
				if (other.hasSpentByTransactionHash()) {
					setSpentByTransactionHash(other.getSpentByTransactionHash());
				}
				if (other.hasSpentByTransactionIndex()) {
					setSpentByTransactionIndex(other.getSpentByTransactionIndex());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasValue()) {

					return false;
				}
				if (!hasScriptBytes()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.TransactionOutput parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.TransactionOutput) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private long value_;

			public boolean hasValue() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public long getValue() {
				return value_;
			}

			public Builder setValue(long value) {
				bitField0_ |= 0x00000001;
				value_ = value;
				onChanged();
				return this;
			}

			public Builder clearValue() {
				bitField0_ = (bitField0_ & ~0x00000001);
				value_ = 0L;
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString scriptBytes_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasScriptBytes() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public com.google.protobuf.ByteString getScriptBytes() {
				return scriptBytes_;
			}

			public Builder setScriptBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000002;
				scriptBytes_ = value;
				onChanged();
				return this;
			}

			public Builder clearScriptBytes() {
				bitField0_ = (bitField0_ & ~0x00000002);
				scriptBytes_ = getDefaultInstance().getScriptBytes();
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString spentByTransactionHash_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasSpentByTransactionHash() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public com.google.protobuf.ByteString getSpentByTransactionHash() {
				return spentByTransactionHash_;
			}

			public Builder setSpentByTransactionHash(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000004;
				spentByTransactionHash_ = value;
				onChanged();
				return this;
			}

			public Builder clearSpentByTransactionHash() {
				bitField0_ = (bitField0_ & ~0x00000004);
				spentByTransactionHash_ = getDefaultInstance().getSpentByTransactionHash();
				onChanged();
				return this;
			}

			private int spentByTransactionIndex_;

			public boolean hasSpentByTransactionIndex() {
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			public int getSpentByTransactionIndex() {
				return spentByTransactionIndex_;
			}

			public Builder setSpentByTransactionIndex(int value) {
				bitField0_ |= 0x00000008;
				spentByTransactionIndex_ = value;
				onChanged();
				return this;
			}

			public Builder clearSpentByTransactionIndex() {
				bitField0_ = (bitField0_ & ~0x00000008);
				spentByTransactionIndex_ = 0;
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new TransactionOutput(true);
			defaultInstance.initFields();
		}

	}

	public interface TransactionConfidenceOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasType();

		cros.mail.chain.wallet.Protos.TransactionConfidence.Type getType();

		boolean hasAppearedAtHeight();

		int getAppearedAtHeight();

		boolean hasOverridingTransaction();

		com.google.protobuf.ByteString getOverridingTransaction();

		boolean hasDepth();

		int getDepth();

		java.util.List<cros.mail.chain.wallet.Protos.PeerAddress> getBroadcastByList();

		cros.mail.chain.wallet.Protos.PeerAddress getBroadcastBy(int index);

		int getBroadcastByCount();

		java.util.List<? extends cros.mail.chain.wallet.Protos.PeerAddressOrBuilder> getBroadcastByOrBuilderList();

		cros.mail.chain.wallet.Protos.PeerAddressOrBuilder getBroadcastByOrBuilder(int index);

		boolean hasSource();

		cros.mail.chain.wallet.Protos.TransactionConfidence.Source getSource();
	}

	public static final class TransactionConfidence extends com.google.protobuf.GeneratedMessage implements

			TransactionConfidenceOrBuilder {

		private TransactionConfidence(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private TransactionConfidence(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final TransactionConfidence defaultInstance;

		public static TransactionConfidence getDefaultInstance() {
			return defaultInstance;
		}

		public TransactionConfidence getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private TransactionConfidence(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 8: {
						int rawValue = input.readEnum();
						cros.mail.chain.wallet.Protos.TransactionConfidence.Type value = cros.mail.chain.wallet.Protos.TransactionConfidence.Type
								.valueOf(rawValue);
						if (value == null) {
							unknownFields.mergeVarintField(1, rawValue);
						} else {
							bitField0_ |= 0x00000001;
							type_ = value;
						}
						break;
					}
					case 16: {
						bitField0_ |= 0x00000002;
						appearedAtHeight_ = input.readInt32();
						break;
					}
					case 26: {
						bitField0_ |= 0x00000004;
						overridingTransaction_ = input.readBytes();
						break;
					}
					case 32: {
						bitField0_ |= 0x00000008;
						depth_ = input.readInt32();
						break;
					}
					case 50: {
						if (!((mutable_bitField0_ & 0x00000010) == 0x00000010)) {
							broadcastBy_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.PeerAddress>();
							mutable_bitField0_ |= 0x00000010;
						}
						broadcastBy_
								.add(input.readMessage(cros.mail.chain.wallet.Protos.PeerAddress.PARSER, extensionRegistry));
						break;
					}
					case 56: {
						int rawValue = input.readEnum();
						cros.mail.chain.wallet.Protos.TransactionConfidence.Source value = cros.mail.chain.wallet.Protos.TransactionConfidence.Source
								.valueOf(rawValue);
						if (value == null) {
							unknownFields.mergeVarintField(7, rawValue);
						} else {
							bitField0_ |= 0x00000010;
							source_ = value;
						}
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				if (((mutable_bitField0_ & 0x00000010) == 0x00000010)) {
					broadcastBy_ = java.util.Collections.unmodifiableList(broadcastBy_);
				}
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionConfidence_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionConfidence_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.TransactionConfidence.class,
							cros.mail.chain.wallet.Protos.TransactionConfidence.Builder.class);
		}

		public static com.google.protobuf.Parser<TransactionConfidence> PARSER = new com.google.protobuf.AbstractParser<TransactionConfidence>() {
			public TransactionConfidence parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new TransactionConfidence(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<TransactionConfidence> getParserForType() {
			return PARSER;
		}

		public enum Type implements com.google.protobuf.ProtocolMessageEnum {

			UNKNOWN(0, 0),

			BUILDING(1, 1),

			PENDING(2, 2),

			NOT_IN_BEST_CHAIN(3, 3),

			DEAD(4, 4),;

			public static final int UNKNOWN_VALUE = 0;

			public static final int BUILDING_VALUE = 1;

			public static final int PENDING_VALUE = 2;

			public static final int NOT_IN_BEST_CHAIN_VALUE = 3;

			public static final int DEAD_VALUE = 4;

			public final int getNumber() {
				return value;
			}

			public static Type valueOf(int value) {
				switch (value) {
				case 0:
					return UNKNOWN;
				case 1:
					return BUILDING;
				case 2:
					return PENDING;
				case 3:
					return NOT_IN_BEST_CHAIN;
				case 4:
					return DEAD;
				default:
					return null;
				}
			}

			public static com.google.protobuf.Internal.EnumLiteMap<Type> internalGetValueMap() {
				return internalValueMap;
			}

			private static com.google.protobuf.Internal.EnumLiteMap<Type> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<Type>() {
				public Type findValueByNumber(int number) {
					return Type.valueOf(number);
				}
			};

			public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
				return getDescriptor().getValues().get(index);
			}

			public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
				return getDescriptor();
			}

			public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.TransactionConfidence.getDescriptor().getEnumTypes().get(0);
			}

			private static final Type[] VALUES = values();

			public static Type valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
				if (desc.getType() != getDescriptor()) {
					throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
				}
				return VALUES[desc.getIndex()];
			}

			private final int index;
			private final int value;

			private Type(int index, int value) {
				this.index = index;
				this.value = value;
			}

		}

		public enum Source implements com.google.protobuf.ProtocolMessageEnum {

			SOURCE_UNKNOWN(0, 0),

			SOURCE_NETWORK(1, 1),

			SOURCE_SELF(2, 2),;

			public static final int SOURCE_UNKNOWN_VALUE = 0;

			public static final int SOURCE_NETWORK_VALUE = 1;

			public static final int SOURCE_SELF_VALUE = 2;

			public final int getNumber() {
				return value;
			}

			public static Source valueOf(int value) {
				switch (value) {
				case 0:
					return SOURCE_UNKNOWN;
				case 1:
					return SOURCE_NETWORK;
				case 2:
					return SOURCE_SELF;
				default:
					return null;
				}
			}

			public static com.google.protobuf.Internal.EnumLiteMap<Source> internalGetValueMap() {
				return internalValueMap;
			}

			private static com.google.protobuf.Internal.EnumLiteMap<Source> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<Source>() {
				public Source findValueByNumber(int number) {
					return Source.valueOf(number);
				}
			};

			public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
				return getDescriptor().getValues().get(index);
			}

			public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
				return getDescriptor();
			}

			public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.TransactionConfidence.getDescriptor().getEnumTypes().get(1);
			}

			private static final Source[] VALUES = values();

			public static Source valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
				if (desc.getType() != getDescriptor()) {
					throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
				}
				return VALUES[desc.getIndex()];
			}

			private final int index;
			private final int value;

			private Source(int index, int value) {
				this.index = index;
				this.value = value;
			}

		}

		private int bitField0_;
		public static final int TYPE_FIELD_NUMBER = 1;
		private cros.mail.chain.wallet.Protos.TransactionConfidence.Type type_;

		public boolean hasType() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public cros.mail.chain.wallet.Protos.TransactionConfidence.Type getType() {
			return type_;
		}

		public static final int APPEARED_AT_HEIGHT_FIELD_NUMBER = 2;
		private int appearedAtHeight_;

		public boolean hasAppearedAtHeight() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public int getAppearedAtHeight() {
			return appearedAtHeight_;
		}

		public static final int OVERRIDING_TRANSACTION_FIELD_NUMBER = 3;
		private com.google.protobuf.ByteString overridingTransaction_;

		public boolean hasOverridingTransaction() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public com.google.protobuf.ByteString getOverridingTransaction() {
			return overridingTransaction_;
		}

		public static final int DEPTH_FIELD_NUMBER = 4;
		private int depth_;

		public boolean hasDepth() {
			return ((bitField0_ & 0x00000008) == 0x00000008);
		}

		public int getDepth() {
			return depth_;
		}

		public static final int BROADCAST_BY_FIELD_NUMBER = 6;
		private java.util.List<cros.mail.chain.wallet.Protos.PeerAddress> broadcastBy_;

		public java.util.List<cros.mail.chain.wallet.Protos.PeerAddress> getBroadcastByList() {
			return broadcastBy_;
		}

		public java.util.List<? extends cros.mail.chain.wallet.Protos.PeerAddressOrBuilder> getBroadcastByOrBuilderList() {
			return broadcastBy_;
		}

		public int getBroadcastByCount() {
			return broadcastBy_.size();
		}

		public cros.mail.chain.wallet.Protos.PeerAddress getBroadcastBy(int index) {
			return broadcastBy_.get(index);
		}

		public cros.mail.chain.wallet.Protos.PeerAddressOrBuilder getBroadcastByOrBuilder(int index) {
			return broadcastBy_.get(index);
		}

		public static final int SOURCE_FIELD_NUMBER = 7;
		private cros.mail.chain.wallet.Protos.TransactionConfidence.Source source_;

		public boolean hasSource() {
			return ((bitField0_ & 0x00000010) == 0x00000010);
		}

		public cros.mail.chain.wallet.Protos.TransactionConfidence.Source getSource() {
			return source_;
		}

		private void initFields() {
			type_ = cros.mail.chain.wallet.Protos.TransactionConfidence.Type.UNKNOWN;
			appearedAtHeight_ = 0;
			overridingTransaction_ = com.google.protobuf.ByteString.EMPTY;
			depth_ = 0;
			broadcastBy_ = java.util.Collections.emptyList();
			source_ = cros.mail.chain.wallet.Protos.TransactionConfidence.Source.SOURCE_UNKNOWN;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			for (int i = 0; i < getBroadcastByCount(); i++) {
				if (!getBroadcastBy(i).isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeEnum(1, type_.getNumber());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeInt32(2, appearedAtHeight_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeBytes(3, overridingTransaction_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				output.writeInt32(4, depth_);
			}
			for (int i = 0; i < broadcastBy_.size(); i++) {
				output.writeMessage(6, broadcastBy_.get(i));
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				output.writeEnum(7, source_.getNumber());
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeEnumSize(1, type_.getNumber());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeInt32Size(2, appearedAtHeight_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(3, overridingTransaction_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				size += com.google.protobuf.CodedOutputStream.computeInt32Size(4, depth_);
			}
			for (int i = 0; i < broadcastBy_.size(); i++) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(6, broadcastBy_.get(i));
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				size += com.google.protobuf.CodedOutputStream.computeEnumSize(7, source_.getNumber());
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.TransactionConfidence parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.TransactionConfidence parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionConfidence parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.TransactionConfidence parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionConfidence parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionConfidence parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionConfidence parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionConfidence parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionConfidence parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionConfidence parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.TransactionConfidence prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

		cros.mail.chain.wallet.Protos.TransactionConfidenceOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionConfidence_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionConfidence_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.TransactionConfidence.class,
								cros.mail.chain.wallet.Protos.TransactionConfidence.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
					getBroadcastByFieldBuilder();
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				type_ = cros.mail.chain.wallet.Protos.TransactionConfidence.Type.UNKNOWN;
				bitField0_ = (bitField0_ & ~0x00000001);
				appearedAtHeight_ = 0;
				bitField0_ = (bitField0_ & ~0x00000002);
				overridingTransaction_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000004);
				depth_ = 0;
				bitField0_ = (bitField0_ & ~0x00000008);
				if (broadcastByBuilder_ == null) {
					broadcastBy_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000010);
				} else {
					broadcastByBuilder_.clear();
				}
				source_ = cros.mail.chain.wallet.Protos.TransactionConfidence.Source.SOURCE_UNKNOWN;
				bitField0_ = (bitField0_ & ~0x00000020);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionConfidence_descriptor;
			}

			public cros.mail.chain.wallet.Protos.TransactionConfidence getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.TransactionConfidence.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.TransactionConfidence build() {
				cros.mail.chain.wallet.Protos.TransactionConfidence result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.TransactionConfidence buildPartial() {
				cros.mail.chain.wallet.Protos.TransactionConfidence result = new cros.mail.chain.wallet.Protos.TransactionConfidence(
						this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.type_ = type_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.appearedAtHeight_ = appearedAtHeight_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000004;
				}
				result.overridingTransaction_ = overridingTransaction_;
				if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
					to_bitField0_ |= 0x00000008;
				}
				result.depth_ = depth_;
				if (broadcastByBuilder_ == null) {
					if (((bitField0_ & 0x00000010) == 0x00000010)) {
						broadcastBy_ = java.util.Collections.unmodifiableList(broadcastBy_);
						bitField0_ = (bitField0_ & ~0x00000010);
					}
					result.broadcastBy_ = broadcastBy_;
				} else {
					result.broadcastBy_ = broadcastByBuilder_.build();
				}
				if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
					to_bitField0_ |= 0x00000010;
				}
				result.source_ = source_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.TransactionConfidence) {
					return mergeFrom((cros.mail.chain.wallet.Protos.TransactionConfidence) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.TransactionConfidence other) {
				if (other == cros.mail.chain.wallet.Protos.TransactionConfidence.getDefaultInstance())
					return this;
				if (other.hasType()) {
					setType(other.getType());
				}
				if (other.hasAppearedAtHeight()) {
					setAppearedAtHeight(other.getAppearedAtHeight());
				}
				if (other.hasOverridingTransaction()) {
					setOverridingTransaction(other.getOverridingTransaction());
				}
				if (other.hasDepth()) {
					setDepth(other.getDepth());
				}
				if (broadcastByBuilder_ == null) {
					if (!other.broadcastBy_.isEmpty()) {
						if (broadcastBy_.isEmpty()) {
							broadcastBy_ = other.broadcastBy_;
							bitField0_ = (bitField0_ & ~0x00000010);
						} else {
							ensureBroadcastByIsMutable();
							broadcastBy_.addAll(other.broadcastBy_);
						}
						onChanged();
					}
				} else {
					if (!other.broadcastBy_.isEmpty()) {
						if (broadcastByBuilder_.isEmpty()) {
							broadcastByBuilder_.dispose();
							broadcastByBuilder_ = null;
							broadcastBy_ = other.broadcastBy_;
							bitField0_ = (bitField0_ & ~0x00000010);
							broadcastByBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders
									? getBroadcastByFieldBuilder()
									: null;
						} else {
							broadcastByBuilder_.addAllMessages(other.broadcastBy_);
						}
					}
				}
				if (other.hasSource()) {
					setSource(other.getSource());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				for (int i = 0; i < getBroadcastByCount(); i++) {
					if (!getBroadcastBy(i).isInitialized()) {

						return false;
					}
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.TransactionConfidence parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.TransactionConfidence) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private cros.mail.chain.wallet.Protos.TransactionConfidence.Type type_ = cros.mail.chain.wallet.Protos.TransactionConfidence.Type.UNKNOWN;

			public boolean hasType() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public cros.mail.chain.wallet.Protos.TransactionConfidence.Type getType() {
				return type_;
			}

			public Builder setType(cros.mail.chain.wallet.Protos.TransactionConfidence.Type value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				type_ = value;
				onChanged();
				return this;
			}

			public Builder clearType() {
				bitField0_ = (bitField0_ & ~0x00000001);
				type_ = cros.mail.chain.wallet.Protos.TransactionConfidence.Type.UNKNOWN;
				onChanged();
				return this;
			}

			private int appearedAtHeight_;

			public boolean hasAppearedAtHeight() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public int getAppearedAtHeight() {
				return appearedAtHeight_;
			}

			public Builder setAppearedAtHeight(int value) {
				bitField0_ |= 0x00000002;
				appearedAtHeight_ = value;
				onChanged();
				return this;
			}

			public Builder clearAppearedAtHeight() {
				bitField0_ = (bitField0_ & ~0x00000002);
				appearedAtHeight_ = 0;
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString overridingTransaction_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasOverridingTransaction() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public com.google.protobuf.ByteString getOverridingTransaction() {
				return overridingTransaction_;
			}

			public Builder setOverridingTransaction(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000004;
				overridingTransaction_ = value;
				onChanged();
				return this;
			}

			public Builder clearOverridingTransaction() {
				bitField0_ = (bitField0_ & ~0x00000004);
				overridingTransaction_ = getDefaultInstance().getOverridingTransaction();
				onChanged();
				return this;
			}

			private int depth_;

			public boolean hasDepth() {
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			public int getDepth() {
				return depth_;
			}

			public Builder setDepth(int value) {
				bitField0_ |= 0x00000008;
				depth_ = value;
				onChanged();
				return this;
			}

			public Builder clearDepth() {
				bitField0_ = (bitField0_ & ~0x00000008);
				depth_ = 0;
				onChanged();
				return this;
			}

			private java.util.List<cros.mail.chain.wallet.Protos.PeerAddress> broadcastBy_ = java.util.Collections.emptyList();

			private void ensureBroadcastByIsMutable() {
				if (!((bitField0_ & 0x00000010) == 0x00000010)) {
					broadcastBy_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.PeerAddress>(broadcastBy_);
					bitField0_ |= 0x00000010;
				}
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.PeerAddress, cros.mail.chain.wallet.Protos.PeerAddress.Builder, cros.mail.chain.wallet.Protos.PeerAddressOrBuilder> broadcastByBuilder_;

			public java.util.List<cros.mail.chain.wallet.Protos.PeerAddress> getBroadcastByList() {
				if (broadcastByBuilder_ == null) {
					return java.util.Collections.unmodifiableList(broadcastBy_);
				} else {
					return broadcastByBuilder_.getMessageList();
				}
			}

			public int getBroadcastByCount() {
				if (broadcastByBuilder_ == null) {
					return broadcastBy_.size();
				} else {
					return broadcastByBuilder_.getCount();
				}
			}

			public cros.mail.chain.wallet.Protos.PeerAddress getBroadcastBy(int index) {
				if (broadcastByBuilder_ == null) {
					return broadcastBy_.get(index);
				} else {
					return broadcastByBuilder_.getMessage(index);
				}
			}

			public Builder setBroadcastBy(int index, cros.mail.chain.wallet.Protos.PeerAddress value) {
				if (broadcastByBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureBroadcastByIsMutable();
					broadcastBy_.set(index, value);
					onChanged();
				} else {
					broadcastByBuilder_.setMessage(index, value);
				}
				return this;
			}

			public Builder setBroadcastBy(int index, cros.mail.chain.wallet.Protos.PeerAddress.Builder builderForValue) {
				if (broadcastByBuilder_ == null) {
					ensureBroadcastByIsMutable();
					broadcastBy_.set(index, builderForValue.build());
					onChanged();
				} else {
					broadcastByBuilder_.setMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addBroadcastBy(cros.mail.chain.wallet.Protos.PeerAddress value) {
				if (broadcastByBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureBroadcastByIsMutable();
					broadcastBy_.add(value);
					onChanged();
				} else {
					broadcastByBuilder_.addMessage(value);
				}
				return this;
			}

			public Builder addBroadcastBy(int index, cros.mail.chain.wallet.Protos.PeerAddress value) {
				if (broadcastByBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureBroadcastByIsMutable();
					broadcastBy_.add(index, value);
					onChanged();
				} else {
					broadcastByBuilder_.addMessage(index, value);
				}
				return this;
			}

			public Builder addBroadcastBy(cros.mail.chain.wallet.Protos.PeerAddress.Builder builderForValue) {
				if (broadcastByBuilder_ == null) {
					ensureBroadcastByIsMutable();
					broadcastBy_.add(builderForValue.build());
					onChanged();
				} else {
					broadcastByBuilder_.addMessage(builderForValue.build());
				}
				return this;
			}

			public Builder addBroadcastBy(int index, cros.mail.chain.wallet.Protos.PeerAddress.Builder builderForValue) {
				if (broadcastByBuilder_ == null) {
					ensureBroadcastByIsMutable();
					broadcastBy_.add(index, builderForValue.build());
					onChanged();
				} else {
					broadcastByBuilder_.addMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addAllBroadcastBy(java.lang.Iterable<? extends cros.mail.chain.wallet.Protos.PeerAddress> values) {
				if (broadcastByBuilder_ == null) {
					ensureBroadcastByIsMutable();
					com.google.protobuf.AbstractMessageLite.Builder.addAll(values, broadcastBy_);
					onChanged();
				} else {
					broadcastByBuilder_.addAllMessages(values);
				}
				return this;
			}

			public Builder clearBroadcastBy() {
				if (broadcastByBuilder_ == null) {
					broadcastBy_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000010);
					onChanged();
				} else {
					broadcastByBuilder_.clear();
				}
				return this;
			}

			public Builder removeBroadcastBy(int index) {
				if (broadcastByBuilder_ == null) {
					ensureBroadcastByIsMutable();
					broadcastBy_.remove(index);
					onChanged();
				} else {
					broadcastByBuilder_.remove(index);
				}
				return this;
			}

			public cros.mail.chain.wallet.Protos.PeerAddress.Builder getBroadcastByBuilder(int index) {
				return getBroadcastByFieldBuilder().getBuilder(index);
			}

			public cros.mail.chain.wallet.Protos.PeerAddressOrBuilder getBroadcastByOrBuilder(int index) {
				if (broadcastByBuilder_ == null) {
					return broadcastBy_.get(index);
				} else {
					return broadcastByBuilder_.getMessageOrBuilder(index);
				}
			}

			public java.util.List<? extends cros.mail.chain.wallet.Protos.PeerAddressOrBuilder> getBroadcastByOrBuilderList() {
				if (broadcastByBuilder_ != null) {
					return broadcastByBuilder_.getMessageOrBuilderList();
				} else {
					return java.util.Collections.unmodifiableList(broadcastBy_);
				}
			}

			public cros.mail.chain.wallet.Protos.PeerAddress.Builder addBroadcastByBuilder() {
				return getBroadcastByFieldBuilder().addBuilder(cros.mail.chain.wallet.Protos.PeerAddress.getDefaultInstance());
			}

			public cros.mail.chain.wallet.Protos.PeerAddress.Builder addBroadcastByBuilder(int index) {
				return getBroadcastByFieldBuilder().addBuilder(index,
						cros.mail.chain.wallet.Protos.PeerAddress.getDefaultInstance());
			}

			public java.util.List<cros.mail.chain.wallet.Protos.PeerAddress.Builder> getBroadcastByBuilderList() {
				return getBroadcastByFieldBuilder().getBuilderList();
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.PeerAddress, cros.mail.chain.wallet.Protos.PeerAddress.Builder, cros.mail.chain.wallet.Protos.PeerAddressOrBuilder> getBroadcastByFieldBuilder() {
				if (broadcastByBuilder_ == null) {
					broadcastByBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.PeerAddress, cros.mail.chain.wallet.Protos.PeerAddress.Builder, cros.mail.chain.wallet.Protos.PeerAddressOrBuilder>(
							broadcastBy_, ((bitField0_ & 0x00000010) == 0x00000010), getParentForChildren(), isClean());
					broadcastBy_ = null;
				}
				return broadcastByBuilder_;
			}

			private cros.mail.chain.wallet.Protos.TransactionConfidence.Source source_ = cros.mail.chain.wallet.Protos.TransactionConfidence.Source.SOURCE_UNKNOWN;

			public boolean hasSource() {
				return ((bitField0_ & 0x00000020) == 0x00000020);
			}

			public cros.mail.chain.wallet.Protos.TransactionConfidence.Source getSource() {
				return source_;
			}

			public Builder setSource(cros.mail.chain.wallet.Protos.TransactionConfidence.Source value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000020;
				source_ = value;
				onChanged();
				return this;
			}

			public Builder clearSource() {
				bitField0_ = (bitField0_ & ~0x00000020);
				source_ = cros.mail.chain.wallet.Protos.TransactionConfidence.Source.SOURCE_UNKNOWN;
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new TransactionConfidence(true);
			defaultInstance.initFields();
		}

	}

	public interface TransactionOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasVersion();

		int getVersion();

		boolean hasHash();

		com.google.protobuf.ByteString getHash();

		boolean hasPool();

		cros.mail.chain.wallet.Protos.Transaction.Pool getPool();

		boolean hasLockTime();

		int getLockTime();

		boolean hasUpdatedAt();

		long getUpdatedAt();

		java.util.List<cros.mail.chain.wallet.Protos.TransactionInput> getTransactionInputList();

		cros.mail.chain.wallet.Protos.TransactionInput getTransactionInput(int index);

		int getTransactionInputCount();

		java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionInputOrBuilder> getTransactionInputOrBuilderList();

		cros.mail.chain.wallet.Protos.TransactionInputOrBuilder getTransactionInputOrBuilder(int index);

		java.util.List<cros.mail.chain.wallet.Protos.TransactionOutput> getTransactionOutputList();

		cros.mail.chain.wallet.Protos.TransactionOutput getTransactionOutput(int index);

		int getTransactionOutputCount();

		java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionOutputOrBuilder> getTransactionOutputOrBuilderList();

		cros.mail.chain.wallet.Protos.TransactionOutputOrBuilder getTransactionOutputOrBuilder(int index);

		java.util.List<com.google.protobuf.ByteString> getBlockHashList();

		int getBlockHashCount();

		com.google.protobuf.ByteString getBlockHash(int index);

		java.util.List<java.lang.Integer> getBlockRelativityOffsetsList();

		int getBlockRelativityOffsetsCount();

		int getBlockRelativityOffsets(int index);

		boolean hasConfidence();

		cros.mail.chain.wallet.Protos.TransactionConfidence getConfidence();

		cros.mail.chain.wallet.Protos.TransactionConfidenceOrBuilder getConfidenceOrBuilder();

		boolean hasPurpose();

		cros.mail.chain.wallet.Protos.Transaction.Purpose getPurpose();

		boolean hasExchangeRate();

		cros.mail.chain.wallet.Protos.ExchangeRate getExchangeRate();

		cros.mail.chain.wallet.Protos.ExchangeRateOrBuilder getExchangeRateOrBuilder();

		boolean hasMemo();

		java.lang.String getMemo();

		com.google.protobuf.ByteString getMemoBytes();
	}

	public static final class Transaction extends com.google.protobuf.GeneratedMessage implements

			TransactionOrBuilder {

		private Transaction(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private Transaction(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final Transaction defaultInstance;

		public static Transaction getDefaultInstance() {
			return defaultInstance;
		}

		public Transaction getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private Transaction(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 8: {
						bitField0_ |= 0x00000001;
						version_ = input.readInt32();
						break;
					}
					case 18: {
						bitField0_ |= 0x00000002;
						hash_ = input.readBytes();
						break;
					}
					case 24: {
						int rawValue = input.readEnum();
						cros.mail.chain.wallet.Protos.Transaction.Pool value = cros.mail.chain.wallet.Protos.Transaction.Pool
								.valueOf(rawValue);
						if (value == null) {
							unknownFields.mergeVarintField(3, rawValue);
						} else {
							bitField0_ |= 0x00000004;
							pool_ = value;
						}
						break;
					}
					case 32: {
						bitField0_ |= 0x00000008;
						lockTime_ = input.readUInt32();
						break;
					}
					case 40: {
						bitField0_ |= 0x00000010;
						updatedAt_ = input.readInt64();
						break;
					}
					case 50: {
						if (!((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
							transactionInput_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.TransactionInput>();
							mutable_bitField0_ |= 0x00000020;
						}
						transactionInput_.add(
								input.readMessage(cros.mail.chain.wallet.Protos.TransactionInput.PARSER, extensionRegistry));
						break;
					}
					case 58: {
						if (!((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
							transactionOutput_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.TransactionOutput>();
							mutable_bitField0_ |= 0x00000040;
						}
						transactionOutput_.add(
								input.readMessage(cros.mail.chain.wallet.Protos.TransactionOutput.PARSER, extensionRegistry));
						break;
					}
					case 66: {
						if (!((mutable_bitField0_ & 0x00000080) == 0x00000080)) {
							blockHash_ = new java.util.ArrayList<com.google.protobuf.ByteString>();
							mutable_bitField0_ |= 0x00000080;
						}
						blockHash_.add(input.readBytes());
						break;
					}
					case 74: {
						cros.mail.chain.wallet.Protos.TransactionConfidence.Builder subBuilder = null;
						if (((bitField0_ & 0x00000020) == 0x00000020)) {
							subBuilder = confidence_.toBuilder();
						}
						confidence_ = input.readMessage(cros.mail.chain.wallet.Protos.TransactionConfidence.PARSER,
								extensionRegistry);
						if (subBuilder != null) {
							subBuilder.mergeFrom(confidence_);
							confidence_ = subBuilder.buildPartial();
						}
						bitField0_ |= 0x00000020;
						break;
					}
					case 80: {
						int rawValue = input.readEnum();
						cros.mail.chain.wallet.Protos.Transaction.Purpose value = cros.mail.chain.wallet.Protos.Transaction.Purpose
								.valueOf(rawValue);
						if (value == null) {
							unknownFields.mergeVarintField(10, rawValue);
						} else {
							bitField0_ |= 0x00000040;
							purpose_ = value;
						}
						break;
					}
					case 88: {
						if (!((mutable_bitField0_ & 0x00000100) == 0x00000100)) {
							blockRelativityOffsets_ = new java.util.ArrayList<java.lang.Integer>();
							mutable_bitField0_ |= 0x00000100;
						}
						blockRelativityOffsets_.add(input.readInt32());
						break;
					}
					case 90: {
						int length = input.readRawVarint32();
						int limit = input.pushLimit(length);
						if (!((mutable_bitField0_ & 0x00000100) == 0x00000100) && input.getBytesUntilLimit() > 0) {
							blockRelativityOffsets_ = new java.util.ArrayList<java.lang.Integer>();
							mutable_bitField0_ |= 0x00000100;
						}
						while (input.getBytesUntilLimit() > 0) {
							blockRelativityOffsets_.add(input.readInt32());
						}
						input.popLimit(limit);
						break;
					}
					case 98: {
						cros.mail.chain.wallet.Protos.ExchangeRate.Builder subBuilder = null;
						if (((bitField0_ & 0x00000080) == 0x00000080)) {
							subBuilder = exchangeRate_.toBuilder();
						}
						exchangeRate_ = input.readMessage(cros.mail.chain.wallet.Protos.ExchangeRate.PARSER,
								extensionRegistry);
						if (subBuilder != null) {
							subBuilder.mergeFrom(exchangeRate_);
							exchangeRate_ = subBuilder.buildPartial();
						}
						bitField0_ |= 0x00000080;
						break;
					}
					case 106: {
						com.google.protobuf.ByteString bs = input.readBytes();
						bitField0_ |= 0x00000100;
						memo_ = bs;
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				if (((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
					transactionInput_ = java.util.Collections.unmodifiableList(transactionInput_);
				}
				if (((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
					transactionOutput_ = java.util.Collections.unmodifiableList(transactionOutput_);
				}
				if (((mutable_bitField0_ & 0x00000080) == 0x00000080)) {
					blockHash_ = java.util.Collections.unmodifiableList(blockHash_);
				}
				if (((mutable_bitField0_ & 0x00000100) == 0x00000100)) {
					blockRelativityOffsets_ = java.util.Collections.unmodifiableList(blockRelativityOffsets_);
				}
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Transaction_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Transaction_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.Transaction.class,
							cros.mail.chain.wallet.Protos.Transaction.Builder.class);
		}

		public static com.google.protobuf.Parser<Transaction> PARSER = new com.google.protobuf.AbstractParser<Transaction>() {
			public Transaction parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new Transaction(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<Transaction> getParserForType() {
			return PARSER;
		}

		public enum Pool implements com.google.protobuf.ProtocolMessageEnum {

			UNSPENT(0, 4),

			SPENT(1, 5),

			INACTIVE(2, 2),

			DEAD(3, 10),

			PENDING(4, 16),

			PENDING_INACTIVE(5, 18),;

			public static final int UNSPENT_VALUE = 4;

			public static final int SPENT_VALUE = 5;

			public static final int INACTIVE_VALUE = 2;

			public static final int DEAD_VALUE = 10;

			public static final int PENDING_VALUE = 16;

			public static final int PENDING_INACTIVE_VALUE = 18;

			public final int getNumber() {
				return value;
			}

			public static Pool valueOf(int value) {
				switch (value) {
				case 4:
					return UNSPENT;
				case 5:
					return SPENT;
				case 2:
					return INACTIVE;
				case 10:
					return DEAD;
				case 16:
					return PENDING;
				case 18:
					return PENDING_INACTIVE;
				default:
					return null;
				}
			}

			public static com.google.protobuf.Internal.EnumLiteMap<Pool> internalGetValueMap() {
				return internalValueMap;
			}

			private static com.google.protobuf.Internal.EnumLiteMap<Pool> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<Pool>() {
				public Pool findValueByNumber(int number) {
					return Pool.valueOf(number);
				}
			};

			public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
				return getDescriptor().getValues().get(index);
			}

			public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
				return getDescriptor();
			}

			public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.Transaction.getDescriptor().getEnumTypes().get(0);
			}

			private static final Pool[] VALUES = values();

			public static Pool valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
				if (desc.getType() != getDescriptor()) {
					throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
				}
				return VALUES[desc.getIndex()];
			}

			private final int index;
			private final int value;

			private Pool(int index, int value) {
				this.index = index;
				this.value = value;
			}

		}

		public enum Purpose implements com.google.protobuf.ProtocolMessageEnum {

			UNKNOWN(0, 0),

			USER_PAYMENT(1, 1),

			KEY_ROTATION(2, 2),

			ASSURANCE_CONTRACT_CLAIM(3, 3),

			ASSURANCE_CONTRACT_PLEDGE(4, 4),

			ASSURANCE_CONTRACT_STUB(5, 5),

			RAISE_FEE(6, 6),;

			public static final int UNKNOWN_VALUE = 0;

			public static final int USER_PAYMENT_VALUE = 1;

			public static final int KEY_ROTATION_VALUE = 2;

			public static final int ASSURANCE_CONTRACT_CLAIM_VALUE = 3;

			public static final int ASSURANCE_CONTRACT_PLEDGE_VALUE = 4;

			public static final int ASSURANCE_CONTRACT_STUB_VALUE = 5;

			public static final int RAISE_FEE_VALUE = 6;

			public final int getNumber() {
				return value;
			}

			public static Purpose valueOf(int value) {
				switch (value) {
				case 0:
					return UNKNOWN;
				case 1:
					return USER_PAYMENT;
				case 2:
					return KEY_ROTATION;
				case 3:
					return ASSURANCE_CONTRACT_CLAIM;
				case 4:
					return ASSURANCE_CONTRACT_PLEDGE;
				case 5:
					return ASSURANCE_CONTRACT_STUB;
				case 6:
					return RAISE_FEE;
				default:
					return null;
				}
			}

			public static com.google.protobuf.Internal.EnumLiteMap<Purpose> internalGetValueMap() {
				return internalValueMap;
			}

			private static com.google.protobuf.Internal.EnumLiteMap<Purpose> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<Purpose>() {
				public Purpose findValueByNumber(int number) {
					return Purpose.valueOf(number);
				}
			};

			public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
				return getDescriptor().getValues().get(index);
			}

			public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
				return getDescriptor();
			}

			public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.Transaction.getDescriptor().getEnumTypes().get(1);
			}

			private static final Purpose[] VALUES = values();

			public static Purpose valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
				if (desc.getType() != getDescriptor()) {
					throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
				}
				return VALUES[desc.getIndex()];
			}

			private final int index;
			private final int value;

			private Purpose(int index, int value) {
				this.index = index;
				this.value = value;
			}

		}

		private int bitField0_;
		public static final int VERSION_FIELD_NUMBER = 1;
		private int version_;

		public boolean hasVersion() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public int getVersion() {
			return version_;
		}

		public static final int HASH_FIELD_NUMBER = 2;
		private com.google.protobuf.ByteString hash_;

		public boolean hasHash() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public com.google.protobuf.ByteString getHash() {
			return hash_;
		}

		public static final int POOL_FIELD_NUMBER = 3;
		private cros.mail.chain.wallet.Protos.Transaction.Pool pool_;

		public boolean hasPool() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public cros.mail.chain.wallet.Protos.Transaction.Pool getPool() {
			return pool_;
		}

		public static final int LOCK_TIME_FIELD_NUMBER = 4;
		private int lockTime_;

		public boolean hasLockTime() {
			return ((bitField0_ & 0x00000008) == 0x00000008);
		}

		public int getLockTime() {
			return lockTime_;
		}

		public static final int UPDATED_AT_FIELD_NUMBER = 5;
		private long updatedAt_;

		public boolean hasUpdatedAt() {
			return ((bitField0_ & 0x00000010) == 0x00000010);
		}

		public long getUpdatedAt() {
			return updatedAt_;
		}

		public static final int TRANSACTION_INPUT_FIELD_NUMBER = 6;
		private java.util.List<cros.mail.chain.wallet.Protos.TransactionInput> transactionInput_;

		public java.util.List<cros.mail.chain.wallet.Protos.TransactionInput> getTransactionInputList() {
			return transactionInput_;
		}

		public java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionInputOrBuilder> getTransactionInputOrBuilderList() {
			return transactionInput_;
		}

		public int getTransactionInputCount() {
			return transactionInput_.size();
		}

		public cros.mail.chain.wallet.Protos.TransactionInput getTransactionInput(int index) {
			return transactionInput_.get(index);
		}

		public cros.mail.chain.wallet.Protos.TransactionInputOrBuilder getTransactionInputOrBuilder(int index) {
			return transactionInput_.get(index);
		}

		public static final int TRANSACTION_OUTPUT_FIELD_NUMBER = 7;
		private java.util.List<cros.mail.chain.wallet.Protos.TransactionOutput> transactionOutput_;

		public java.util.List<cros.mail.chain.wallet.Protos.TransactionOutput> getTransactionOutputList() {
			return transactionOutput_;
		}

		public java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionOutputOrBuilder> getTransactionOutputOrBuilderList() {
			return transactionOutput_;
		}

		public int getTransactionOutputCount() {
			return transactionOutput_.size();
		}

		public cros.mail.chain.wallet.Protos.TransactionOutput getTransactionOutput(int index) {
			return transactionOutput_.get(index);
		}

		public cros.mail.chain.wallet.Protos.TransactionOutputOrBuilder getTransactionOutputOrBuilder(int index) {
			return transactionOutput_.get(index);
		}

		public static final int BLOCK_HASH_FIELD_NUMBER = 8;
		private java.util.List<com.google.protobuf.ByteString> blockHash_;

		public java.util.List<com.google.protobuf.ByteString> getBlockHashList() {
			return blockHash_;
		}

		public int getBlockHashCount() {
			return blockHash_.size();
		}

		public com.google.protobuf.ByteString getBlockHash(int index) {
			return blockHash_.get(index);
		}

		public static final int BLOCK_RELATIVITY_OFFSETS_FIELD_NUMBER = 11;
		private java.util.List<java.lang.Integer> blockRelativityOffsets_;

		public java.util.List<java.lang.Integer> getBlockRelativityOffsetsList() {
			return blockRelativityOffsets_;
		}

		public int getBlockRelativityOffsetsCount() {
			return blockRelativityOffsets_.size();
		}

		public int getBlockRelativityOffsets(int index) {
			return blockRelativityOffsets_.get(index);
		}

		public static final int CONFIDENCE_FIELD_NUMBER = 9;
		private cros.mail.chain.wallet.Protos.TransactionConfidence confidence_;

		public boolean hasConfidence() {
			return ((bitField0_ & 0x00000020) == 0x00000020);
		}

		public cros.mail.chain.wallet.Protos.TransactionConfidence getConfidence() {
			return confidence_;
		}

		public cros.mail.chain.wallet.Protos.TransactionConfidenceOrBuilder getConfidenceOrBuilder() {
			return confidence_;
		}

		public static final int PURPOSE_FIELD_NUMBER = 10;
		private cros.mail.chain.wallet.Protos.Transaction.Purpose purpose_;

		public boolean hasPurpose() {
			return ((bitField0_ & 0x00000040) == 0x00000040);
		}

		public cros.mail.chain.wallet.Protos.Transaction.Purpose getPurpose() {
			return purpose_;
		}

		public static final int EXCHANGE_RATE_FIELD_NUMBER = 12;
		private cros.mail.chain.wallet.Protos.ExchangeRate exchangeRate_;

		public boolean hasExchangeRate() {
			return ((bitField0_ & 0x00000080) == 0x00000080);
		}

		public cros.mail.chain.wallet.Protos.ExchangeRate getExchangeRate() {
			return exchangeRate_;
		}

		public cros.mail.chain.wallet.Protos.ExchangeRateOrBuilder getExchangeRateOrBuilder() {
			return exchangeRate_;
		}

		public static final int MEMO_FIELD_NUMBER = 13;
		private java.lang.Object memo_;

		public boolean hasMemo() {
			return ((bitField0_ & 0x00000100) == 0x00000100);
		}

		public java.lang.String getMemo() {
			java.lang.Object ref = memo_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8()) {
					memo_ = s;
				}
				return s;
			}
		}

		public com.google.protobuf.ByteString getMemoBytes() {
			java.lang.Object ref = memo_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				memo_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}

		private void initFields() {
			version_ = 0;
			hash_ = com.google.protobuf.ByteString.EMPTY;
			pool_ = cros.mail.chain.wallet.Protos.Transaction.Pool.UNSPENT;
			lockTime_ = 0;
			updatedAt_ = 0L;
			transactionInput_ = java.util.Collections.emptyList();
			transactionOutput_ = java.util.Collections.emptyList();
			blockHash_ = java.util.Collections.emptyList();
			blockRelativityOffsets_ = java.util.Collections.emptyList();
			confidence_ = cros.mail.chain.wallet.Protos.TransactionConfidence.getDefaultInstance();
			purpose_ = cros.mail.chain.wallet.Protos.Transaction.Purpose.UNKNOWN;
			exchangeRate_ = cros.mail.chain.wallet.Protos.ExchangeRate.getDefaultInstance();
			memo_ = "";
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasVersion()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasHash()) {
				memoizedIsInitialized = 0;
				return false;
			}
			for (int i = 0; i < getTransactionInputCount(); i++) {
				if (!getTransactionInput(i).isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			for (int i = 0; i < getTransactionOutputCount(); i++) {
				if (!getTransactionOutput(i).isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			if (hasConfidence()) {
				if (!getConfidence().isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			if (hasExchangeRate()) {
				if (!getExchangeRate().isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeInt32(1, version_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeBytes(2, hash_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeEnum(3, pool_.getNumber());
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				output.writeUInt32(4, lockTime_);
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				output.writeInt64(5, updatedAt_);
			}
			for (int i = 0; i < transactionInput_.size(); i++) {
				output.writeMessage(6, transactionInput_.get(i));
			}
			for (int i = 0; i < transactionOutput_.size(); i++) {
				output.writeMessage(7, transactionOutput_.get(i));
			}
			for (int i = 0; i < blockHash_.size(); i++) {
				output.writeBytes(8, blockHash_.get(i));
			}
			if (((bitField0_ & 0x00000020) == 0x00000020)) {
				output.writeMessage(9, confidence_);
			}
			if (((bitField0_ & 0x00000040) == 0x00000040)) {
				output.writeEnum(10, purpose_.getNumber());
			}
			for (int i = 0; i < blockRelativityOffsets_.size(); i++) {
				output.writeInt32(11, blockRelativityOffsets_.get(i));
			}
			if (((bitField0_ & 0x00000080) == 0x00000080)) {
				output.writeMessage(12, exchangeRate_);
			}
			if (((bitField0_ & 0x00000100) == 0x00000100)) {
				output.writeBytes(13, getMemoBytes());
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeInt32Size(1, version_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, hash_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeEnumSize(3, pool_.getNumber());
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				size += com.google.protobuf.CodedOutputStream.computeUInt32Size(4, lockTime_);
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				size += com.google.protobuf.CodedOutputStream.computeInt64Size(5, updatedAt_);
			}
			for (int i = 0; i < transactionInput_.size(); i++) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(6, transactionInput_.get(i));
			}
			for (int i = 0; i < transactionOutput_.size(); i++) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(7, transactionOutput_.get(i));
			}
			{
				int dataSize = 0;
				for (int i = 0; i < blockHash_.size(); i++) {
					dataSize += com.google.protobuf.CodedOutputStream.computeBytesSizeNoTag(blockHash_.get(i));
				}
				size += dataSize;
				size += 1 * getBlockHashList().size();
			}
			if (((bitField0_ & 0x00000020) == 0x00000020)) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(9, confidence_);
			}
			if (((bitField0_ & 0x00000040) == 0x00000040)) {
				size += com.google.protobuf.CodedOutputStream.computeEnumSize(10, purpose_.getNumber());
			}
			{
				int dataSize = 0;
				for (int i = 0; i < blockRelativityOffsets_.size(); i++) {
					dataSize += com.google.protobuf.CodedOutputStream
							.computeInt32SizeNoTag(blockRelativityOffsets_.get(i));
				}
				size += dataSize;
				size += 1 * getBlockRelativityOffsetsList().size();
			}
			if (((bitField0_ & 0x00000080) == 0x00000080)) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(12, exchangeRate_);
			}
			if (((bitField0_ & 0x00000100) == 0x00000100)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(13, getMemoBytes());
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.Transaction parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Transaction parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Transaction parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Transaction parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Transaction parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Transaction parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Transaction parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Transaction parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Transaction parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Transaction parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.Transaction prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

				cros.mail.chain.wallet.Protos.TransactionOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Transaction_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Transaction_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.Transaction.class,
								cros.mail.chain.wallet.Protos.Transaction.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
					getTransactionInputFieldBuilder();
					getTransactionOutputFieldBuilder();
					getConfidenceFieldBuilder();
					getExchangeRateFieldBuilder();
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				version_ = 0;
				bitField0_ = (bitField0_ & ~0x00000001);
				hash_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000002);
				pool_ = cros.mail.chain.wallet.Protos.Transaction.Pool.UNSPENT;
				bitField0_ = (bitField0_ & ~0x00000004);
				lockTime_ = 0;
				bitField0_ = (bitField0_ & ~0x00000008);
				updatedAt_ = 0L;
				bitField0_ = (bitField0_ & ~0x00000010);
				if (transactionInputBuilder_ == null) {
					transactionInput_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000020);
				} else {
					transactionInputBuilder_.clear();
				}
				if (transactionOutputBuilder_ == null) {
					transactionOutput_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000040);
				} else {
					transactionOutputBuilder_.clear();
				}
				blockHash_ = java.util.Collections.emptyList();
				bitField0_ = (bitField0_ & ~0x00000080);
				blockRelativityOffsets_ = java.util.Collections.emptyList();
				bitField0_ = (bitField0_ & ~0x00000100);
				if (confidenceBuilder_ == null) {
					confidence_ = cros.mail.chain.wallet.Protos.TransactionConfidence.getDefaultInstance();
				} else {
					confidenceBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000200);
				purpose_ = cros.mail.chain.wallet.Protos.Transaction.Purpose.UNKNOWN;
				bitField0_ = (bitField0_ & ~0x00000400);
				if (exchangeRateBuilder_ == null) {
					exchangeRate_ = cros.mail.chain.wallet.Protos.ExchangeRate.getDefaultInstance();
				} else {
					exchangeRateBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000800);
				memo_ = "";
				bitField0_ = (bitField0_ & ~0x00001000);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Transaction_descriptor;
			}

			public cros.mail.chain.wallet.Protos.Transaction getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.Transaction.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.Transaction build() {
				cros.mail.chain.wallet.Protos.Transaction result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.Transaction buildPartial() {
				cros.mail.chain.wallet.Protos.Transaction result = new cros.mail.chain.wallet.Protos.Transaction(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.version_ = version_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.hash_ = hash_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000004;
				}
				result.pool_ = pool_;
				if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
					to_bitField0_ |= 0x00000008;
				}
				result.lockTime_ = lockTime_;
				if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
					to_bitField0_ |= 0x00000010;
				}
				result.updatedAt_ = updatedAt_;
				if (transactionInputBuilder_ == null) {
					if (((bitField0_ & 0x00000020) == 0x00000020)) {
						transactionInput_ = java.util.Collections.unmodifiableList(transactionInput_);
						bitField0_ = (bitField0_ & ~0x00000020);
					}
					result.transactionInput_ = transactionInput_;
				} else {
					result.transactionInput_ = transactionInputBuilder_.build();
				}
				if (transactionOutputBuilder_ == null) {
					if (((bitField0_ & 0x00000040) == 0x00000040)) {
						transactionOutput_ = java.util.Collections.unmodifiableList(transactionOutput_);
						bitField0_ = (bitField0_ & ~0x00000040);
					}
					result.transactionOutput_ = transactionOutput_;
				} else {
					result.transactionOutput_ = transactionOutputBuilder_.build();
				}
				if (((bitField0_ & 0x00000080) == 0x00000080)) {
					blockHash_ = java.util.Collections.unmodifiableList(blockHash_);
					bitField0_ = (bitField0_ & ~0x00000080);
				}
				result.blockHash_ = blockHash_;
				if (((bitField0_ & 0x00000100) == 0x00000100)) {
					blockRelativityOffsets_ = java.util.Collections.unmodifiableList(blockRelativityOffsets_);
					bitField0_ = (bitField0_ & ~0x00000100);
				}
				result.blockRelativityOffsets_ = blockRelativityOffsets_;
				if (((from_bitField0_ & 0x00000200) == 0x00000200)) {
					to_bitField0_ |= 0x00000020;
				}
				if (confidenceBuilder_ == null) {
					result.confidence_ = confidence_;
				} else {
					result.confidence_ = confidenceBuilder_.build();
				}
				if (((from_bitField0_ & 0x00000400) == 0x00000400)) {
					to_bitField0_ |= 0x00000040;
				}
				result.purpose_ = purpose_;
				if (((from_bitField0_ & 0x00000800) == 0x00000800)) {
					to_bitField0_ |= 0x00000080;
				}
				if (exchangeRateBuilder_ == null) {
					result.exchangeRate_ = exchangeRate_;
				} else {
					result.exchangeRate_ = exchangeRateBuilder_.build();
				}
				if (((from_bitField0_ & 0x00001000) == 0x00001000)) {
					to_bitField0_ |= 0x00000100;
				}
				result.memo_ = memo_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.Transaction) {
					return mergeFrom((cros.mail.chain.wallet.Protos.Transaction) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.Transaction other) {
				if (other == cros.mail.chain.wallet.Protos.Transaction.getDefaultInstance())
					return this;
				if (other.hasVersion()) {
					setVersion(other.getVersion());
				}
				if (other.hasHash()) {
					setHash(other.getHash());
				}
				if (other.hasPool()) {
					setPool(other.getPool());
				}
				if (other.hasLockTime()) {
					setLockTime(other.getLockTime());
				}
				if (other.hasUpdatedAt()) {
					setUpdatedAt(other.getUpdatedAt());
				}
				if (transactionInputBuilder_ == null) {
					if (!other.transactionInput_.isEmpty()) {
						if (transactionInput_.isEmpty()) {
							transactionInput_ = other.transactionInput_;
							bitField0_ = (bitField0_ & ~0x00000020);
						} else {
							ensureTransactionInputIsMutable();
							transactionInput_.addAll(other.transactionInput_);
						}
						onChanged();
					}
				} else {
					if (!other.transactionInput_.isEmpty()) {
						if (transactionInputBuilder_.isEmpty()) {
							transactionInputBuilder_.dispose();
							transactionInputBuilder_ = null;
							transactionInput_ = other.transactionInput_;
							bitField0_ = (bitField0_ & ~0x00000020);
							transactionInputBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders
									? getTransactionInputFieldBuilder()
									: null;
						} else {
							transactionInputBuilder_.addAllMessages(other.transactionInput_);
						}
					}
				}
				if (transactionOutputBuilder_ == null) {
					if (!other.transactionOutput_.isEmpty()) {
						if (transactionOutput_.isEmpty()) {
							transactionOutput_ = other.transactionOutput_;
							bitField0_ = (bitField0_ & ~0x00000040);
						} else {
							ensureTransactionOutputIsMutable();
							transactionOutput_.addAll(other.transactionOutput_);
						}
						onChanged();
					}
				} else {
					if (!other.transactionOutput_.isEmpty()) {
						if (transactionOutputBuilder_.isEmpty()) {
							transactionOutputBuilder_.dispose();
							transactionOutputBuilder_ = null;
							transactionOutput_ = other.transactionOutput_;
							bitField0_ = (bitField0_ & ~0x00000040);
							transactionOutputBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders
									? getTransactionOutputFieldBuilder()
									: null;
						} else {
							transactionOutputBuilder_.addAllMessages(other.transactionOutput_);
						}
					}
				}
				if (!other.blockHash_.isEmpty()) {
					if (blockHash_.isEmpty()) {
						blockHash_ = other.blockHash_;
						bitField0_ = (bitField0_ & ~0x00000080);
					} else {
						ensureBlockHashIsMutable();
						blockHash_.addAll(other.blockHash_);
					}
					onChanged();
				}
				if (!other.blockRelativityOffsets_.isEmpty()) {
					if (blockRelativityOffsets_.isEmpty()) {
						blockRelativityOffsets_ = other.blockRelativityOffsets_;
						bitField0_ = (bitField0_ & ~0x00000100);
					} else {
						ensureBlockRelativityOffsetsIsMutable();
						blockRelativityOffsets_.addAll(other.blockRelativityOffsets_);
					}
					onChanged();
				}
				if (other.hasConfidence()) {
					mergeConfidence(other.getConfidence());
				}
				if (other.hasPurpose()) {
					setPurpose(other.getPurpose());
				}
				if (other.hasExchangeRate()) {
					mergeExchangeRate(other.getExchangeRate());
				}
				if (other.hasMemo()) {
					bitField0_ |= 0x00001000;
					memo_ = other.memo_;
					onChanged();
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasVersion()) {

					return false;
				}
				if (!hasHash()) {

					return false;
				}
				for (int i = 0; i < getTransactionInputCount(); i++) {
					if (!getTransactionInput(i).isInitialized()) {

						return false;
					}
				}
				for (int i = 0; i < getTransactionOutputCount(); i++) {
					if (!getTransactionOutput(i).isInitialized()) {

						return false;
					}
				}
				if (hasConfidence()) {
					if (!getConfidence().isInitialized()) {

						return false;
					}
				}
				if (hasExchangeRate()) {
					if (!getExchangeRate().isInitialized()) {

						return false;
					}
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.Transaction parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.Transaction) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private int version_;

			public boolean hasVersion() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public int getVersion() {
				return version_;
			}

			public Builder setVersion(int value) {
				bitField0_ |= 0x00000001;
				version_ = value;
				onChanged();
				return this;
			}

			public Builder clearVersion() {
				bitField0_ = (bitField0_ & ~0x00000001);
				version_ = 0;
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString hash_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasHash() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public com.google.protobuf.ByteString getHash() {
				return hash_;
			}

			public Builder setHash(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000002;
				hash_ = value;
				onChanged();
				return this;
			}

			public Builder clearHash() {
				bitField0_ = (bitField0_ & ~0x00000002);
				hash_ = getDefaultInstance().getHash();
				onChanged();
				return this;
			}

			private cros.mail.chain.wallet.Protos.Transaction.Pool pool_ = cros.mail.chain.wallet.Protos.Transaction.Pool.UNSPENT;

			public boolean hasPool() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public cros.mail.chain.wallet.Protos.Transaction.Pool getPool() {
				return pool_;
			}

			public Builder setPool(cros.mail.chain.wallet.Protos.Transaction.Pool value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000004;
				pool_ = value;
				onChanged();
				return this;
			}

			public Builder clearPool() {
				bitField0_ = (bitField0_ & ~0x00000004);
				pool_ = cros.mail.chain.wallet.Protos.Transaction.Pool.UNSPENT;
				onChanged();
				return this;
			}

			private int lockTime_;

			public boolean hasLockTime() {
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			public int getLockTime() {
				return lockTime_;
			}

			public Builder setLockTime(int value) {
				bitField0_ |= 0x00000008;
				lockTime_ = value;
				onChanged();
				return this;
			}

			public Builder clearLockTime() {
				bitField0_ = (bitField0_ & ~0x00000008);
				lockTime_ = 0;
				onChanged();
				return this;
			}

			private long updatedAt_;

			public boolean hasUpdatedAt() {
				return ((bitField0_ & 0x00000010) == 0x00000010);
			}

			public long getUpdatedAt() {
				return updatedAt_;
			}

			public Builder setUpdatedAt(long value) {
				bitField0_ |= 0x00000010;
				updatedAt_ = value;
				onChanged();
				return this;
			}

			public Builder clearUpdatedAt() {
				bitField0_ = (bitField0_ & ~0x00000010);
				updatedAt_ = 0L;
				onChanged();
				return this;
			}

			private java.util.List<cros.mail.chain.wallet.Protos.TransactionInput> transactionInput_ = java.util.Collections
					.emptyList();

			private void ensureTransactionInputIsMutable() {
				if (!((bitField0_ & 0x00000020) == 0x00000020)) {
					transactionInput_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.TransactionInput>(
							transactionInput_);
					bitField0_ |= 0x00000020;
				}
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.TransactionInput, cros.mail.chain.wallet.Protos.TransactionInput.Builder, cros.mail.chain.wallet.Protos.TransactionInputOrBuilder> transactionInputBuilder_;

			public java.util.List<cros.mail.chain.wallet.Protos.TransactionInput> getTransactionInputList() {
				if (transactionInputBuilder_ == null) {
					return java.util.Collections.unmodifiableList(transactionInput_);
				} else {
					return transactionInputBuilder_.getMessageList();
				}
			}

			public int getTransactionInputCount() {
				if (transactionInputBuilder_ == null) {
					return transactionInput_.size();
				} else {
					return transactionInputBuilder_.getCount();
				}
			}

			public cros.mail.chain.wallet.Protos.TransactionInput getTransactionInput(int index) {
				if (transactionInputBuilder_ == null) {
					return transactionInput_.get(index);
				} else {
					return transactionInputBuilder_.getMessage(index);
				}
			}

			public Builder setTransactionInput(int index, cros.mail.chain.wallet.Protos.TransactionInput value) {
				if (transactionInputBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionInputIsMutable();
					transactionInput_.set(index, value);
					onChanged();
				} else {
					transactionInputBuilder_.setMessage(index, value);
				}
				return this;
			}

			public Builder setTransactionInput(int index,
					cros.mail.chain.wallet.Protos.TransactionInput.Builder builderForValue) {
				if (transactionInputBuilder_ == null) {
					ensureTransactionInputIsMutable();
					transactionInput_.set(index, builderForValue.build());
					onChanged();
				} else {
					transactionInputBuilder_.setMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addTransactionInput(cros.mail.chain.wallet.Protos.TransactionInput value) {
				if (transactionInputBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionInputIsMutable();
					transactionInput_.add(value);
					onChanged();
				} else {
					transactionInputBuilder_.addMessage(value);
				}
				return this;
			}

			public Builder addTransactionInput(int index, cros.mail.chain.wallet.Protos.TransactionInput value) {
				if (transactionInputBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionInputIsMutable();
					transactionInput_.add(index, value);
					onChanged();
				} else {
					transactionInputBuilder_.addMessage(index, value);
				}
				return this;
			}

			public Builder addTransactionInput(cros.mail.chain.wallet.Protos.TransactionInput.Builder builderForValue) {
				if (transactionInputBuilder_ == null) {
					ensureTransactionInputIsMutable();
					transactionInput_.add(builderForValue.build());
					onChanged();
				} else {
					transactionInputBuilder_.addMessage(builderForValue.build());
				}
				return this;
			}

			public Builder addTransactionInput(int index,
					cros.mail.chain.wallet.Protos.TransactionInput.Builder builderForValue) {
				if (transactionInputBuilder_ == null) {
					ensureTransactionInputIsMutable();
					transactionInput_.add(index, builderForValue.build());
					onChanged();
				} else {
					transactionInputBuilder_.addMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addAllTransactionInput(
					java.lang.Iterable<? extends cros.mail.chain.wallet.Protos.TransactionInput> values) {
				if (transactionInputBuilder_ == null) {
					ensureTransactionInputIsMutable();
					com.google.protobuf.AbstractMessageLite.Builder.addAll(values, transactionInput_);
					onChanged();
				} else {
					transactionInputBuilder_.addAllMessages(values);
				}
				return this;
			}

			public Builder clearTransactionInput() {
				if (transactionInputBuilder_ == null) {
					transactionInput_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000020);
					onChanged();
				} else {
					transactionInputBuilder_.clear();
				}
				return this;
			}

			public Builder removeTransactionInput(int index) {
				if (transactionInputBuilder_ == null) {
					ensureTransactionInputIsMutable();
					transactionInput_.remove(index);
					onChanged();
				} else {
					transactionInputBuilder_.remove(index);
				}
				return this;
			}

			public cros.mail.chain.wallet.Protos.TransactionInput.Builder getTransactionInputBuilder(int index) {
				return getTransactionInputFieldBuilder().getBuilder(index);
			}

			public cros.mail.chain.wallet.Protos.TransactionInputOrBuilder getTransactionInputOrBuilder(int index) {
				if (transactionInputBuilder_ == null) {
					return transactionInput_.get(index);
				} else {
					return transactionInputBuilder_.getMessageOrBuilder(index);
				}
			}

			public java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionInputOrBuilder> getTransactionInputOrBuilderList() {
				if (transactionInputBuilder_ != null) {
					return transactionInputBuilder_.getMessageOrBuilderList();
				} else {
					return java.util.Collections.unmodifiableList(transactionInput_);
				}
			}

			public cros.mail.chain.wallet.Protos.TransactionInput.Builder addTransactionInputBuilder() {
				return getTransactionInputFieldBuilder()
						.addBuilder(cros.mail.chain.wallet.Protos.TransactionInput.getDefaultInstance());
			}

			public cros.mail.chain.wallet.Protos.TransactionInput.Builder addTransactionInputBuilder(int index) {
				return getTransactionInputFieldBuilder().addBuilder(index,
						cros.mail.chain.wallet.Protos.TransactionInput.getDefaultInstance());
			}

			public java.util.List<cros.mail.chain.wallet.Protos.TransactionInput.Builder> getTransactionInputBuilderList() {
				return getTransactionInputFieldBuilder().getBuilderList();
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.TransactionInput, cros.mail.chain.wallet.Protos.TransactionInput.Builder, cros.mail.chain.wallet.Protos.TransactionInputOrBuilder> getTransactionInputFieldBuilder() {
				if (transactionInputBuilder_ == null) {
					transactionInputBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.TransactionInput, cros.mail.chain.wallet.Protos.TransactionInput.Builder, cros.mail.chain.wallet.Protos.TransactionInputOrBuilder>(
							transactionInput_, ((bitField0_ & 0x00000020) == 0x00000020), getParentForChildren(),
							isClean());
					transactionInput_ = null;
				}
				return transactionInputBuilder_;
			}

			private java.util.List<cros.mail.chain.wallet.Protos.TransactionOutput> transactionOutput_ = java.util.Collections
					.emptyList();

			private void ensureTransactionOutputIsMutable() {
				if (!((bitField0_ & 0x00000040) == 0x00000040)) {
					transactionOutput_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.TransactionOutput>(
							transactionOutput_);
					bitField0_ |= 0x00000040;
				}
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.TransactionOutput, cros.mail.chain.wallet.Protos.TransactionOutput.Builder, cros.mail.chain.wallet.Protos.TransactionOutputOrBuilder> transactionOutputBuilder_;

			public java.util.List<cros.mail.chain.wallet.Protos.TransactionOutput> getTransactionOutputList() {
				if (transactionOutputBuilder_ == null) {
					return java.util.Collections.unmodifiableList(transactionOutput_);
				} else {
					return transactionOutputBuilder_.getMessageList();
				}
			}

			public int getTransactionOutputCount() {
				if (transactionOutputBuilder_ == null) {
					return transactionOutput_.size();
				} else {
					return transactionOutputBuilder_.getCount();
				}
			}

			public cros.mail.chain.wallet.Protos.TransactionOutput getTransactionOutput(int index) {
				if (transactionOutputBuilder_ == null) {
					return transactionOutput_.get(index);
				} else {
					return transactionOutputBuilder_.getMessage(index);
				}
			}

			public Builder setTransactionOutput(int index, cros.mail.chain.wallet.Protos.TransactionOutput value) {
				if (transactionOutputBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionOutputIsMutable();
					transactionOutput_.set(index, value);
					onChanged();
				} else {
					transactionOutputBuilder_.setMessage(index, value);
				}
				return this;
			}

			public Builder setTransactionOutput(int index,
					cros.mail.chain.wallet.Protos.TransactionOutput.Builder builderForValue) {
				if (transactionOutputBuilder_ == null) {
					ensureTransactionOutputIsMutable();
					transactionOutput_.set(index, builderForValue.build());
					onChanged();
				} else {
					transactionOutputBuilder_.setMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addTransactionOutput(cros.mail.chain.wallet.Protos.TransactionOutput value) {
				if (transactionOutputBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionOutputIsMutable();
					transactionOutput_.add(value);
					onChanged();
				} else {
					transactionOutputBuilder_.addMessage(value);
				}
				return this;
			}

			public Builder addTransactionOutput(int index, cros.mail.chain.wallet.Protos.TransactionOutput value) {
				if (transactionOutputBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionOutputIsMutable();
					transactionOutput_.add(index, value);
					onChanged();
				} else {
					transactionOutputBuilder_.addMessage(index, value);
				}
				return this;
			}

			public Builder addTransactionOutput(cros.mail.chain.wallet.Protos.TransactionOutput.Builder builderForValue) {
				if (transactionOutputBuilder_ == null) {
					ensureTransactionOutputIsMutable();
					transactionOutput_.add(builderForValue.build());
					onChanged();
				} else {
					transactionOutputBuilder_.addMessage(builderForValue.build());
				}
				return this;
			}

			public Builder addTransactionOutput(int index,
					cros.mail.chain.wallet.Protos.TransactionOutput.Builder builderForValue) {
				if (transactionOutputBuilder_ == null) {
					ensureTransactionOutputIsMutable();
					transactionOutput_.add(index, builderForValue.build());
					onChanged();
				} else {
					transactionOutputBuilder_.addMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addAllTransactionOutput(
					java.lang.Iterable<? extends cros.mail.chain.wallet.Protos.TransactionOutput> values) {
				if (transactionOutputBuilder_ == null) {
					ensureTransactionOutputIsMutable();
					com.google.protobuf.AbstractMessageLite.Builder.addAll(values, transactionOutput_);
					onChanged();
				} else {
					transactionOutputBuilder_.addAllMessages(values);
				}
				return this;
			}

			public Builder clearTransactionOutput() {
				if (transactionOutputBuilder_ == null) {
					transactionOutput_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000040);
					onChanged();
				} else {
					transactionOutputBuilder_.clear();
				}
				return this;
			}

			public Builder removeTransactionOutput(int index) {
				if (transactionOutputBuilder_ == null) {
					ensureTransactionOutputIsMutable();
					transactionOutput_.remove(index);
					onChanged();
				} else {
					transactionOutputBuilder_.remove(index);
				}
				return this;
			}

			public cros.mail.chain.wallet.Protos.TransactionOutput.Builder getTransactionOutputBuilder(int index) {
				return getTransactionOutputFieldBuilder().getBuilder(index);
			}

			public cros.mail.chain.wallet.Protos.TransactionOutputOrBuilder getTransactionOutputOrBuilder(int index) {
				if (transactionOutputBuilder_ == null) {
					return transactionOutput_.get(index);
				} else {
					return transactionOutputBuilder_.getMessageOrBuilder(index);
				}
			}

			public java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionOutputOrBuilder> getTransactionOutputOrBuilderList() {
				if (transactionOutputBuilder_ != null) {
					return transactionOutputBuilder_.getMessageOrBuilderList();
				} else {
					return java.util.Collections.unmodifiableList(transactionOutput_);
				}
			}

			public cros.mail.chain.wallet.Protos.TransactionOutput.Builder addTransactionOutputBuilder() {
				return getTransactionOutputFieldBuilder()
						.addBuilder(cros.mail.chain.wallet.Protos.TransactionOutput.getDefaultInstance());
			}

			public cros.mail.chain.wallet.Protos.TransactionOutput.Builder addTransactionOutputBuilder(int index) {
				return getTransactionOutputFieldBuilder().addBuilder(index,
						cros.mail.chain.wallet.Protos.TransactionOutput.getDefaultInstance());
			}

			public java.util.List<cros.mail.chain.wallet.Protos.TransactionOutput.Builder> getTransactionOutputBuilderList() {
				return getTransactionOutputFieldBuilder().getBuilderList();
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.TransactionOutput, cros.mail.chain.wallet.Protos.TransactionOutput.Builder, cros.mail.chain.wallet.Protos.TransactionOutputOrBuilder> getTransactionOutputFieldBuilder() {
				if (transactionOutputBuilder_ == null) {
					transactionOutputBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.TransactionOutput, cros.mail.chain.wallet.Protos.TransactionOutput.Builder, cros.mail.chain.wallet.Protos.TransactionOutputOrBuilder>(
							transactionOutput_, ((bitField0_ & 0x00000040) == 0x00000040), getParentForChildren(),
							isClean());
					transactionOutput_ = null;
				}
				return transactionOutputBuilder_;
			}

			private java.util.List<com.google.protobuf.ByteString> blockHash_ = java.util.Collections.emptyList();

			private void ensureBlockHashIsMutable() {
				if (!((bitField0_ & 0x00000080) == 0x00000080)) {
					blockHash_ = new java.util.ArrayList<com.google.protobuf.ByteString>(blockHash_);
					bitField0_ |= 0x00000080;
				}
			}

			public java.util.List<com.google.protobuf.ByteString> getBlockHashList() {
				return java.util.Collections.unmodifiableList(blockHash_);
			}

			public int getBlockHashCount() {
				return blockHash_.size();
			}

			public com.google.protobuf.ByteString getBlockHash(int index) {
				return blockHash_.get(index);
			}

			public Builder setBlockHash(int index, com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				ensureBlockHashIsMutable();
				blockHash_.set(index, value);
				onChanged();
				return this;
			}

			public Builder addBlockHash(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				ensureBlockHashIsMutable();
				blockHash_.add(value);
				onChanged();
				return this;
			}

			public Builder addAllBlockHash(java.lang.Iterable<? extends com.google.protobuf.ByteString> values) {
				ensureBlockHashIsMutable();
				com.google.protobuf.AbstractMessageLite.Builder.addAll(values, blockHash_);
				onChanged();
				return this;
			}

			public Builder clearBlockHash() {
				blockHash_ = java.util.Collections.emptyList();
				bitField0_ = (bitField0_ & ~0x00000080);
				onChanged();
				return this;
			}

			private java.util.List<java.lang.Integer> blockRelativityOffsets_ = java.util.Collections.emptyList();

			private void ensureBlockRelativityOffsetsIsMutable() {
				if (!((bitField0_ & 0x00000100) == 0x00000100)) {
					blockRelativityOffsets_ = new java.util.ArrayList<java.lang.Integer>(blockRelativityOffsets_);
					bitField0_ |= 0x00000100;
				}
			}

			public java.util.List<java.lang.Integer> getBlockRelativityOffsetsList() {
				return java.util.Collections.unmodifiableList(blockRelativityOffsets_);
			}

			public int getBlockRelativityOffsetsCount() {
				return blockRelativityOffsets_.size();
			}

			public int getBlockRelativityOffsets(int index) {
				return blockRelativityOffsets_.get(index);
			}

			public Builder setBlockRelativityOffsets(int index, int value) {
				ensureBlockRelativityOffsetsIsMutable();
				blockRelativityOffsets_.set(index, value);
				onChanged();
				return this;
			}

			public Builder addBlockRelativityOffsets(int value) {
				ensureBlockRelativityOffsetsIsMutable();
				blockRelativityOffsets_.add(value);
				onChanged();
				return this;
			}

			public Builder addAllBlockRelativityOffsets(java.lang.Iterable<? extends java.lang.Integer> values) {
				ensureBlockRelativityOffsetsIsMutable();
				com.google.protobuf.AbstractMessageLite.Builder.addAll(values, blockRelativityOffsets_);
				onChanged();
				return this;
			}

			public Builder clearBlockRelativityOffsets() {
				blockRelativityOffsets_ = java.util.Collections.emptyList();
				bitField0_ = (bitField0_ & ~0x00000100);
				onChanged();
				return this;
			}

			private cros.mail.chain.wallet.Protos.TransactionConfidence confidence_ = cros.mail.chain.wallet.Protos.TransactionConfidence
					.getDefaultInstance();
			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.TransactionConfidence, cros.mail.chain.wallet.Protos.TransactionConfidence.Builder, cros.mail.chain.wallet.Protos.TransactionConfidenceOrBuilder> confidenceBuilder_;

			public boolean hasConfidence() {
				return ((bitField0_ & 0x00000200) == 0x00000200);
			}

			public cros.mail.chain.wallet.Protos.TransactionConfidence getConfidence() {
				if (confidenceBuilder_ == null) {
					return confidence_;
				} else {
					return confidenceBuilder_.getMessage();
				}
			}

			public Builder setConfidence(cros.mail.chain.wallet.Protos.TransactionConfidence value) {
				if (confidenceBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					confidence_ = value;
					onChanged();
				} else {
					confidenceBuilder_.setMessage(value);
				}
				bitField0_ |= 0x00000200;
				return this;
			}

			public Builder setConfidence(cros.mail.chain.wallet.Protos.TransactionConfidence.Builder builderForValue) {
				if (confidenceBuilder_ == null) {
					confidence_ = builderForValue.build();
					onChanged();
				} else {
					confidenceBuilder_.setMessage(builderForValue.build());
				}
				bitField0_ |= 0x00000200;
				return this;
			}

			public Builder mergeConfidence(cros.mail.chain.wallet.Protos.TransactionConfidence value) {
				if (confidenceBuilder_ == null) {
					if (((bitField0_ & 0x00000200) == 0x00000200)
							&& confidence_ != cros.mail.chain.wallet.Protos.TransactionConfidence.getDefaultInstance()) {
						confidence_ = cros.mail.chain.wallet.Protos.TransactionConfidence.newBuilder(confidence_)
								.mergeFrom(value).buildPartial();
					} else {
						confidence_ = value;
					}
					onChanged();
				} else {
					confidenceBuilder_.mergeFrom(value);
				}
				bitField0_ |= 0x00000200;
				return this;
			}

			public Builder clearConfidence() {
				if (confidenceBuilder_ == null) {
					confidence_ = cros.mail.chain.wallet.Protos.TransactionConfidence.getDefaultInstance();
					onChanged();
				} else {
					confidenceBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000200);
				return this;
			}

			public cros.mail.chain.wallet.Protos.TransactionConfidence.Builder getConfidenceBuilder() {
				bitField0_ |= 0x00000200;
				onChanged();
				return getConfidenceFieldBuilder().getBuilder();
			}

			public cros.mail.chain.wallet.Protos.TransactionConfidenceOrBuilder getConfidenceOrBuilder() {
				if (confidenceBuilder_ != null) {
					return confidenceBuilder_.getMessageOrBuilder();
				} else {
					return confidence_;
				}
			}

			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.TransactionConfidence, cros.mail.chain.wallet.Protos.TransactionConfidence.Builder, cros.mail.chain.wallet.Protos.TransactionConfidenceOrBuilder> getConfidenceFieldBuilder() {
				if (confidenceBuilder_ == null) {
					confidenceBuilder_ = new com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.TransactionConfidence, cros.mail.chain.wallet.Protos.TransactionConfidence.Builder, cros.mail.chain.wallet.Protos.TransactionConfidenceOrBuilder>(
							getConfidence(), getParentForChildren(), isClean());
					confidence_ = null;
				}
				return confidenceBuilder_;
			}

			private cros.mail.chain.wallet.Protos.Transaction.Purpose purpose_ = cros.mail.chain.wallet.Protos.Transaction.Purpose.UNKNOWN;

			public boolean hasPurpose() {
				return ((bitField0_ & 0x00000400) == 0x00000400);
			}

			public cros.mail.chain.wallet.Protos.Transaction.Purpose getPurpose() {
				return purpose_;
			}

			public Builder setPurpose(cros.mail.chain.wallet.Protos.Transaction.Purpose value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000400;
				purpose_ = value;
				onChanged();
				return this;
			}

			public Builder clearPurpose() {
				bitField0_ = (bitField0_ & ~0x00000400);
				purpose_ = cros.mail.chain.wallet.Protos.Transaction.Purpose.UNKNOWN;
				onChanged();
				return this;
			}

			private cros.mail.chain.wallet.Protos.ExchangeRate exchangeRate_ = cros.mail.chain.wallet.Protos.ExchangeRate
					.getDefaultInstance();
			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.ExchangeRate, cros.mail.chain.wallet.Protos.ExchangeRate.Builder, cros.mail.chain.wallet.Protos.ExchangeRateOrBuilder> exchangeRateBuilder_;

			public boolean hasExchangeRate() {
				return ((bitField0_ & 0x00000800) == 0x00000800);
			}

			public cros.mail.chain.wallet.Protos.ExchangeRate getExchangeRate() {
				if (exchangeRateBuilder_ == null) {
					return exchangeRate_;
				} else {
					return exchangeRateBuilder_.getMessage();
				}
			}

			public Builder setExchangeRate(cros.mail.chain.wallet.Protos.ExchangeRate value) {
				if (exchangeRateBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					exchangeRate_ = value;
					onChanged();
				} else {
					exchangeRateBuilder_.setMessage(value);
				}
				bitField0_ |= 0x00000800;
				return this;
			}

			public Builder setExchangeRate(cros.mail.chain.wallet.Protos.ExchangeRate.Builder builderForValue) {
				if (exchangeRateBuilder_ == null) {
					exchangeRate_ = builderForValue.build();
					onChanged();
				} else {
					exchangeRateBuilder_.setMessage(builderForValue.build());
				}
				bitField0_ |= 0x00000800;
				return this;
			}

			public Builder mergeExchangeRate(cros.mail.chain.wallet.Protos.ExchangeRate value) {
				if (exchangeRateBuilder_ == null) {
					if (((bitField0_ & 0x00000800) == 0x00000800)
							&& exchangeRate_ != cros.mail.chain.wallet.Protos.ExchangeRate.getDefaultInstance()) {
						exchangeRate_ = cros.mail.chain.wallet.Protos.ExchangeRate.newBuilder(exchangeRate_).mergeFrom(value)
								.buildPartial();
					} else {
						exchangeRate_ = value;
					}
					onChanged();
				} else {
					exchangeRateBuilder_.mergeFrom(value);
				}
				bitField0_ |= 0x00000800;
				return this;
			}

			public Builder clearExchangeRate() {
				if (exchangeRateBuilder_ == null) {
					exchangeRate_ = cros.mail.chain.wallet.Protos.ExchangeRate.getDefaultInstance();
					onChanged();
				} else {
					exchangeRateBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000800);
				return this;
			}

			public cros.mail.chain.wallet.Protos.ExchangeRate.Builder getExchangeRateBuilder() {
				bitField0_ |= 0x00000800;
				onChanged();
				return getExchangeRateFieldBuilder().getBuilder();
			}

			public cros.mail.chain.wallet.Protos.ExchangeRateOrBuilder getExchangeRateOrBuilder() {
				if (exchangeRateBuilder_ != null) {
					return exchangeRateBuilder_.getMessageOrBuilder();
				} else {
					return exchangeRate_;
				}
			}

			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.ExchangeRate, cros.mail.chain.wallet.Protos.ExchangeRate.Builder, cros.mail.chain.wallet.Protos.ExchangeRateOrBuilder> getExchangeRateFieldBuilder() {
				if (exchangeRateBuilder_ == null) {
					exchangeRateBuilder_ = new com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.ExchangeRate, cros.mail.chain.wallet.Protos.ExchangeRate.Builder, cros.mail.chain.wallet.Protos.ExchangeRateOrBuilder>(
							getExchangeRate(), getParentForChildren(), isClean());
					exchangeRate_ = null;
				}
				return exchangeRateBuilder_;
			}

			private java.lang.Object memo_ = "";

			public boolean hasMemo() {
				return ((bitField0_ & 0x00001000) == 0x00001000);
			}

			public java.lang.String getMemo() {
				java.lang.Object ref = memo_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						memo_ = s;
					}
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}

			public com.google.protobuf.ByteString getMemoBytes() {
				java.lang.Object ref = memo_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					memo_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			public Builder setMemo(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00001000;
				memo_ = value;
				onChanged();
				return this;
			}

			public Builder clearMemo() {
				bitField0_ = (bitField0_ & ~0x00001000);
				memo_ = getDefaultInstance().getMemo();
				onChanged();
				return this;
			}

			public Builder setMemoBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00001000;
				memo_ = value;
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new Transaction(true);
			defaultInstance.initFields();
		}

	}

	public interface ScryptParametersOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasSalt();

		com.google.protobuf.ByteString getSalt();

		boolean hasN();

		long getN();

		boolean hasR();

		int getR();

		boolean hasP();

		int getP();
	}

	public static final class ScryptParameters extends com.google.protobuf.GeneratedMessage implements

			ScryptParametersOrBuilder {

		private ScryptParameters(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private ScryptParameters(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final ScryptParameters defaultInstance;

		public static ScryptParameters getDefaultInstance() {
			return defaultInstance;
		}

		public ScryptParameters getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private ScryptParameters(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 10: {
						bitField0_ |= 0x00000001;
						salt_ = input.readBytes();
						break;
					}
					case 16: {
						bitField0_ |= 0x00000002;
						n_ = input.readInt64();
						break;
					}
					case 24: {
						bitField0_ |= 0x00000004;
						r_ = input.readInt32();
						break;
					}
					case 32: {
						bitField0_ |= 0x00000008;
						p_ = input.readInt32();
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_ScryptParameters_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_ScryptParameters_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.ScryptParameters.class,
							cros.mail.chain.wallet.Protos.ScryptParameters.Builder.class);
		}

		public static com.google.protobuf.Parser<ScryptParameters> PARSER = new com.google.protobuf.AbstractParser<ScryptParameters>() {
			public ScryptParameters parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new ScryptParameters(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<ScryptParameters> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int SALT_FIELD_NUMBER = 1;
		private com.google.protobuf.ByteString salt_;

		public boolean hasSalt() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public com.google.protobuf.ByteString getSalt() {
			return salt_;
		}

		public static final int N_FIELD_NUMBER = 2;
		private long n_;

		public boolean hasN() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public long getN() {
			return n_;
		}

		public static final int R_FIELD_NUMBER = 3;
		private int r_;

		public boolean hasR() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public int getR() {
			return r_;
		}

		public static final int P_FIELD_NUMBER = 4;
		private int p_;

		public boolean hasP() {
			return ((bitField0_ & 0x00000008) == 0x00000008);
		}

		public int getP() {
			return p_;
		}

		private void initFields() {
			salt_ = com.google.protobuf.ByteString.EMPTY;
			n_ = 16384L;
			r_ = 8;
			p_ = 1;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasSalt()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeBytes(1, salt_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeInt64(2, n_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeInt32(3, r_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				output.writeInt32(4, p_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, salt_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeInt64Size(2, n_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeInt32Size(3, r_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				size += com.google.protobuf.CodedOutputStream.computeInt32Size(4, p_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.ScryptParameters parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.ScryptParameters parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.ScryptParameters parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.ScryptParameters parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.ScryptParameters parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.ScryptParameters parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.ScryptParameters parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.ScryptParameters parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.ScryptParameters parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.ScryptParameters parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.ScryptParameters prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

				cros.mail.chain.wallet.Protos.ScryptParametersOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_ScryptParameters_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_ScryptParameters_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.ScryptParameters.class,
								cros.mail.chain.wallet.Protos.ScryptParameters.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				salt_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000001);
				n_ = 16384L;
				bitField0_ = (bitField0_ & ~0x00000002);
				r_ = 8;
				bitField0_ = (bitField0_ & ~0x00000004);
				p_ = 1;
				bitField0_ = (bitField0_ & ~0x00000008);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_ScryptParameters_descriptor;
			}

			public cros.mail.chain.wallet.Protos.ScryptParameters getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.ScryptParameters.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.ScryptParameters build() {
				cros.mail.chain.wallet.Protos.ScryptParameters result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.ScryptParameters buildPartial() {
				cros.mail.chain.wallet.Protos.ScryptParameters result = new cros.mail.chain.wallet.Protos.ScryptParameters(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.salt_ = salt_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.n_ = n_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000004;
				}
				result.r_ = r_;
				if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
					to_bitField0_ |= 0x00000008;
				}
				result.p_ = p_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.ScryptParameters) {
					return mergeFrom((cros.mail.chain.wallet.Protos.ScryptParameters) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.ScryptParameters other) {
				if (other == cros.mail.chain.wallet.Protos.ScryptParameters.getDefaultInstance())
					return this;
				if (other.hasSalt()) {
					setSalt(other.getSalt());
				}
				if (other.hasN()) {
					setN(other.getN());
				}
				if (other.hasR()) {
					setR(other.getR());
				}
				if (other.hasP()) {
					setP(other.getP());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasSalt()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.ScryptParameters parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.ScryptParameters) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private com.google.protobuf.ByteString salt_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasSalt() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public com.google.protobuf.ByteString getSalt() {
				return salt_;
			}

			public Builder setSalt(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				salt_ = value;
				onChanged();
				return this;
			}

			public Builder clearSalt() {
				bitField0_ = (bitField0_ & ~0x00000001);
				salt_ = getDefaultInstance().getSalt();
				onChanged();
				return this;
			}

			private long n_ = 16384L;

			public boolean hasN() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public long getN() {
				return n_;
			}

			public Builder setN(long value) {
				bitField0_ |= 0x00000002;
				n_ = value;
				onChanged();
				return this;
			}

			public Builder clearN() {
				bitField0_ = (bitField0_ & ~0x00000002);
				n_ = 16384L;
				onChanged();
				return this;
			}

			private int r_ = 8;

			public boolean hasR() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public int getR() {
				return r_;
			}

			public Builder setR(int value) {
				bitField0_ |= 0x00000004;
				r_ = value;
				onChanged();
				return this;
			}

			public Builder clearR() {
				bitField0_ = (bitField0_ & ~0x00000004);
				r_ = 8;
				onChanged();
				return this;
			}

			private int p_ = 1;

			public boolean hasP() {
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			public int getP() {
				return p_;
			}

			public Builder setP(int value) {
				bitField0_ |= 0x00000008;
				p_ = value;
				onChanged();
				return this;
			}

			public Builder clearP() {
				bitField0_ = (bitField0_ & ~0x00000008);
				p_ = 1;
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new ScryptParameters(true);
			defaultInstance.initFields();
		}

	}

	public interface ExtensionOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasId();

		java.lang.String getId();

		com.google.protobuf.ByteString getIdBytes();

		boolean hasData();

		com.google.protobuf.ByteString getData();

		boolean hasMandatory();

		boolean getMandatory();
	}

	public static final class Extension extends com.google.protobuf.GeneratedMessage implements

			ExtensionOrBuilder {

		private Extension(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private Extension(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final Extension defaultInstance;

		public static Extension getDefaultInstance() {
			return defaultInstance;
		}

		public Extension getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private Extension(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 10: {
						com.google.protobuf.ByteString bs = input.readBytes();
						bitField0_ |= 0x00000001;
						id_ = bs;
						break;
					}
					case 18: {
						bitField0_ |= 0x00000002;
						data_ = input.readBytes();
						break;
					}
					case 24: {
						bitField0_ |= 0x00000004;
						mandatory_ = input.readBool();
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Extension_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Extension_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.Extension.class,
							cros.mail.chain.wallet.Protos.Extension.Builder.class);
		}

		public static com.google.protobuf.Parser<Extension> PARSER = new com.google.protobuf.AbstractParser<Extension>() {
			public Extension parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new Extension(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<Extension> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int ID_FIELD_NUMBER = 1;
		private java.lang.Object id_;

		public boolean hasId() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public java.lang.String getId() {
			java.lang.Object ref = id_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8()) {
					id_ = s;
				}
				return s;
			}
		}

		public com.google.protobuf.ByteString getIdBytes() {
			java.lang.Object ref = id_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				id_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}

		public static final int DATA_FIELD_NUMBER = 2;
		private com.google.protobuf.ByteString data_;

		public boolean hasData() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public com.google.protobuf.ByteString getData() {
			return data_;
		}

		public static final int MANDATORY_FIELD_NUMBER = 3;
		private boolean mandatory_;

		public boolean hasMandatory() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public boolean getMandatory() {
			return mandatory_;
		}

		private void initFields() {
			id_ = "";
			data_ = com.google.protobuf.ByteString.EMPTY;
			mandatory_ = false;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasId()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasData()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasMandatory()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeBytes(1, getIdBytes());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeBytes(2, data_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeBool(3, mandatory_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getIdBytes());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, data_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeBoolSize(3, mandatory_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.Extension parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Extension parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Extension parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Extension parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Extension parseFrom(java.io.InputStream input) throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Extension parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Extension parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Extension parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Extension parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Extension parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.Extension prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

				cros.mail.chain.wallet.Protos.ExtensionOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Extension_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Extension_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.Extension.class,
								cros.mail.chain.wallet.Protos.Extension.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				id_ = "";
				bitField0_ = (bitField0_ & ~0x00000001);
				data_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000002);
				mandatory_ = false;
				bitField0_ = (bitField0_ & ~0x00000004);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Extension_descriptor;
			}

			public cros.mail.chain.wallet.Protos.Extension getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.Extension.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.Extension build() {
				cros.mail.chain.wallet.Protos.Extension result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.Extension buildPartial() {
				cros.mail.chain.wallet.Protos.Extension result = new cros.mail.chain.wallet.Protos.Extension(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.id_ = id_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.data_ = data_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000004;
				}
				result.mandatory_ = mandatory_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.Extension) {
					return mergeFrom((cros.mail.chain.wallet.Protos.Extension) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.Extension other) {
				if (other == cros.mail.chain.wallet.Protos.Extension.getDefaultInstance())
					return this;
				if (other.hasId()) {
					bitField0_ |= 0x00000001;
					id_ = other.id_;
					onChanged();
				}
				if (other.hasData()) {
					setData(other.getData());
				}
				if (other.hasMandatory()) {
					setMandatory(other.getMandatory());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasId()) {

					return false;
				}
				if (!hasData()) {

					return false;
				}
				if (!hasMandatory()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.Extension parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.Extension) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private java.lang.Object id_ = "";

			public boolean hasId() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public java.lang.String getId() {
				java.lang.Object ref = id_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						id_ = s;
					}
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}

			public com.google.protobuf.ByteString getIdBytes() {
				java.lang.Object ref = id_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					id_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			public Builder setId(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				id_ = value;
				onChanged();
				return this;
			}

			public Builder clearId() {
				bitField0_ = (bitField0_ & ~0x00000001);
				id_ = getDefaultInstance().getId();
				onChanged();
				return this;
			}

			public Builder setIdBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				id_ = value;
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasData() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public com.google.protobuf.ByteString getData() {
				return data_;
			}

			public Builder setData(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000002;
				data_ = value;
				onChanged();
				return this;
			}

			public Builder clearData() {
				bitField0_ = (bitField0_ & ~0x00000002);
				data_ = getDefaultInstance().getData();
				onChanged();
				return this;
			}

			private boolean mandatory_;

			public boolean hasMandatory() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public boolean getMandatory() {
				return mandatory_;
			}

			public Builder setMandatory(boolean value) {
				bitField0_ |= 0x00000004;
				mandatory_ = value;
				onChanged();
				return this;
			}

			public Builder clearMandatory() {
				bitField0_ = (bitField0_ & ~0x00000004);
				mandatory_ = false;
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new Extension(true);
			defaultInstance.initFields();
		}

	}

	public interface TagOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasTag();

		java.lang.String getTag();

		com.google.protobuf.ByteString getTagBytes();

		boolean hasData();

		com.google.protobuf.ByteString getData();
	}

	public static final class Tag extends com.google.protobuf.GeneratedMessage implements

			TagOrBuilder {

		private Tag(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private Tag(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final Tag defaultInstance;

		public static Tag getDefaultInstance() {
			return defaultInstance;
		}

		public Tag getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private Tag(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 10: {
						com.google.protobuf.ByteString bs = input.readBytes();
						bitField0_ |= 0x00000001;
						tag_ = bs;
						break;
					}
					case 18: {
						bitField0_ |= 0x00000002;
						data_ = input.readBytes();
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Tag_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Tag_fieldAccessorTable.ensureFieldAccessorsInitialized(
					cros.mail.chain.wallet.Protos.Tag.class, cros.mail.chain.wallet.Protos.Tag.Builder.class);
		}

		public static com.google.protobuf.Parser<Tag> PARSER = new com.google.protobuf.AbstractParser<Tag>() {
			public Tag parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new Tag(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<Tag> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int TAG_FIELD_NUMBER = 1;
		private java.lang.Object tag_;

		public boolean hasTag() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public java.lang.String getTag() {
			java.lang.Object ref = tag_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8()) {
					tag_ = s;
				}
				return s;
			}
		}

		public com.google.protobuf.ByteString getTagBytes() {
			java.lang.Object ref = tag_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				tag_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}

		public static final int DATA_FIELD_NUMBER = 2;
		private com.google.protobuf.ByteString data_;

		public boolean hasData() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public com.google.protobuf.ByteString getData() {
			return data_;
		}

		private void initFields() {
			tag_ = "";
			data_ = com.google.protobuf.ByteString.EMPTY;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasTag()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasData()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeBytes(1, getTagBytes());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeBytes(2, data_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getTagBytes());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, data_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.Tag parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Tag parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Tag parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Tag parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Tag parseFrom(java.io.InputStream input) throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Tag parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Tag parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Tag parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Tag parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Tag parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.Tag prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

				cros.mail.chain.wallet.Protos.TagOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Tag_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Tag_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.Tag.class,
								cros.mail.chain.wallet.Protos.Tag.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				tag_ = "";
				bitField0_ = (bitField0_ & ~0x00000001);
				data_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000002);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Tag_descriptor;
			}

			public cros.mail.chain.wallet.Protos.Tag getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.Tag.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.Tag build() {
				cros.mail.chain.wallet.Protos.Tag result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.Tag buildPartial() {
				cros.mail.chain.wallet.Protos.Tag result = new cros.mail.chain.wallet.Protos.Tag(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.tag_ = tag_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.data_ = data_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.Tag) {
					return mergeFrom((cros.mail.chain.wallet.Protos.Tag) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.Tag other) {
				if (other == cros.mail.chain.wallet.Protos.Tag.getDefaultInstance())
					return this;
				if (other.hasTag()) {
					bitField0_ |= 0x00000001;
					tag_ = other.tag_;
					onChanged();
				}
				if (other.hasData()) {
					setData(other.getData());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasTag()) {

					return false;
				}
				if (!hasData()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.Tag parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.Tag) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private java.lang.Object tag_ = "";

			public boolean hasTag() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public java.lang.String getTag() {
				java.lang.Object ref = tag_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						tag_ = s;
					}
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}

			public com.google.protobuf.ByteString getTagBytes() {
				java.lang.Object ref = tag_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					tag_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			public Builder setTag(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				tag_ = value;
				onChanged();
				return this;
			}

			public Builder clearTag() {
				bitField0_ = (bitField0_ & ~0x00000001);
				tag_ = getDefaultInstance().getTag();
				onChanged();
				return this;
			}

			public Builder setTagBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				tag_ = value;
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasData() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public com.google.protobuf.ByteString getData() {
				return data_;
			}

			public Builder setData(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000002;
				data_ = value;
				onChanged();
				return this;
			}

			public Builder clearData() {
				bitField0_ = (bitField0_ & ~0x00000002);
				data_ = getDefaultInstance().getData();
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new Tag(true);
			defaultInstance.initFields();
		}

	}

	public interface TransactionSignerOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasClassName();

		java.lang.String getClassName();

		com.google.protobuf.ByteString getClassNameBytes();

		boolean hasData();

		com.google.protobuf.ByteString getData();
	}

	public static final class TransactionSigner extends com.google.protobuf.GeneratedMessage implements

			TransactionSignerOrBuilder {

		private TransactionSigner(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private TransactionSigner(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final TransactionSigner defaultInstance;

		public static TransactionSigner getDefaultInstance() {
			return defaultInstance;
		}

		public TransactionSigner getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private TransactionSigner(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 10: {
						com.google.protobuf.ByteString bs = input.readBytes();
						bitField0_ |= 0x00000001;
						className_ = bs;
						break;
					}
					case 18: {
						bitField0_ |= 0x00000002;
						data_ = input.readBytes();
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionSigner_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionSigner_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.TransactionSigner.class,
							cros.mail.chain.wallet.Protos.TransactionSigner.Builder.class);
		}

		public static com.google.protobuf.Parser<TransactionSigner> PARSER = new com.google.protobuf.AbstractParser<TransactionSigner>() {
			public TransactionSigner parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new TransactionSigner(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<TransactionSigner> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int CLASS_NAME_FIELD_NUMBER = 1;
		private java.lang.Object className_;

		public boolean hasClassName() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public java.lang.String getClassName() {
			java.lang.Object ref = className_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8()) {
					className_ = s;
				}
				return s;
			}
		}

		public com.google.protobuf.ByteString getClassNameBytes() {
			java.lang.Object ref = className_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				className_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}

		public static final int DATA_FIELD_NUMBER = 2;
		private com.google.protobuf.ByteString data_;

		public boolean hasData() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public com.google.protobuf.ByteString getData() {
			return data_;
		}

		private void initFields() {
			className_ = "";
			data_ = com.google.protobuf.ByteString.EMPTY;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasClassName()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeBytes(1, getClassNameBytes());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeBytes(2, data_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getClassNameBytes());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, data_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.TransactionSigner parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.TransactionSigner parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionSigner parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.TransactionSigner parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionSigner parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionSigner parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionSigner parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionSigner parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.TransactionSigner parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.TransactionSigner parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.TransactionSigner prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

				cros.mail.chain.wallet.Protos.TransactionSignerOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionSigner_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionSigner_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.TransactionSigner.class,
								cros.mail.chain.wallet.Protos.TransactionSigner.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				className_ = "";
				bitField0_ = (bitField0_ & ~0x00000001);
				data_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000002);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_TransactionSigner_descriptor;
			}

			public cros.mail.chain.wallet.Protos.TransactionSigner getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.TransactionSigner.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.TransactionSigner build() {
				cros.mail.chain.wallet.Protos.TransactionSigner result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.TransactionSigner buildPartial() {
				cros.mail.chain.wallet.Protos.TransactionSigner result = new cros.mail.chain.wallet.Protos.TransactionSigner(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.className_ = className_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.data_ = data_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.TransactionSigner) {
					return mergeFrom((cros.mail.chain.wallet.Protos.TransactionSigner) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.TransactionSigner other) {
				if (other == cros.mail.chain.wallet.Protos.TransactionSigner.getDefaultInstance())
					return this;
				if (other.hasClassName()) {
					bitField0_ |= 0x00000001;
					className_ = other.className_;
					onChanged();
				}
				if (other.hasData()) {
					setData(other.getData());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasClassName()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.TransactionSigner parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.TransactionSigner) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private java.lang.Object className_ = "";

			public boolean hasClassName() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public java.lang.String getClassName() {
				java.lang.Object ref = className_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						className_ = s;
					}
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}

			public com.google.protobuf.ByteString getClassNameBytes() {
				java.lang.Object ref = className_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					className_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			public Builder setClassName(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				className_ = value;
				onChanged();
				return this;
			}

			public Builder clearClassName() {
				bitField0_ = (bitField0_ & ~0x00000001);
				className_ = getDefaultInstance().getClassName();
				onChanged();
				return this;
			}

			public Builder setClassNameBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				className_ = value;
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasData() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public com.google.protobuf.ByteString getData() {
				return data_;
			}

			public Builder setData(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000002;
				data_ = value;
				onChanged();
				return this;
			}

			public Builder clearData() {
				bitField0_ = (bitField0_ & ~0x00000002);
				data_ = getDefaultInstance().getData();
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new TransactionSigner(true);
			defaultInstance.initFields();
		}

	}

	public interface WalletOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasNetworkIdentifier();

		java.lang.String getNetworkIdentifier();

		com.google.protobuf.ByteString getNetworkIdentifierBytes();

		boolean hasLastSeenBlockHash();

		com.google.protobuf.ByteString getLastSeenBlockHash();

		boolean hasLastSeenBlockHeight();

		int getLastSeenBlockHeight();

		boolean hasLastSeenBlockTimeSecs();

		long getLastSeenBlockTimeSecs();

		java.util.List<cros.mail.chain.wallet.Protos.Key> getKeyList();

		cros.mail.chain.wallet.Protos.Key getKey(int index);

		int getKeyCount();

		java.util.List<? extends cros.mail.chain.wallet.Protos.KeyOrBuilder> getKeyOrBuilderList();

		cros.mail.chain.wallet.Protos.KeyOrBuilder getKeyOrBuilder(int index);

		java.util.List<cros.mail.chain.wallet.Protos.Transaction> getTransactionList();

		cros.mail.chain.wallet.Protos.Transaction getTransaction(int index);

		int getTransactionCount();

		java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionOrBuilder> getTransactionOrBuilderList();

		cros.mail.chain.wallet.Protos.TransactionOrBuilder getTransactionOrBuilder(int index);

		java.util.List<cros.mail.chain.wallet.Protos.Script> getWatchedScriptList();

		cros.mail.chain.wallet.Protos.Script getWatchedScript(int index);

		int getWatchedScriptCount();

		java.util.List<? extends cros.mail.chain.wallet.Protos.ScriptOrBuilder> getWatchedScriptOrBuilderList();

		cros.mail.chain.wallet.Protos.ScriptOrBuilder getWatchedScriptOrBuilder(int index);

		boolean hasEncryptionType();

		cros.mail.chain.wallet.Protos.Wallet.EncryptionType getEncryptionType();

		boolean hasEncryptionParameters();

		cros.mail.chain.wallet.Protos.ScryptParameters getEncryptionParameters();

		cros.mail.chain.wallet.Protos.ScryptParametersOrBuilder getEncryptionParametersOrBuilder();

		boolean hasVersion();

		int getVersion();

		java.util.List<cros.mail.chain.wallet.Protos.Extension> getExtensionList();

		cros.mail.chain.wallet.Protos.Extension getExtension(int index);

		int getExtensionCount();

		java.util.List<? extends cros.mail.chain.wallet.Protos.ExtensionOrBuilder> getExtensionOrBuilderList();

		cros.mail.chain.wallet.Protos.ExtensionOrBuilder getExtensionOrBuilder(int index);

		boolean hasDescription();

		java.lang.String getDescription();

		com.google.protobuf.ByteString getDescriptionBytes();

		boolean hasKeyRotationTime();

		long getKeyRotationTime();

		java.util.List<cros.mail.chain.wallet.Protos.Tag> getTagsList();

		cros.mail.chain.wallet.Protos.Tag getTags(int index);

		int getTagsCount();

		java.util.List<? extends cros.mail.chain.wallet.Protos.TagOrBuilder> getTagsOrBuilderList();

		cros.mail.chain.wallet.Protos.TagOrBuilder getTagsOrBuilder(int index);

		java.util.List<cros.mail.chain.wallet.Protos.TransactionSigner> getTransactionSignersList();

		cros.mail.chain.wallet.Protos.TransactionSigner getTransactionSigners(int index);

		int getTransactionSignersCount();

		java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionSignerOrBuilder> getTransactionSignersOrBuilderList();

		cros.mail.chain.wallet.Protos.TransactionSignerOrBuilder getTransactionSignersOrBuilder(int index);
	}

	public static final class Wallet extends com.google.protobuf.GeneratedMessage implements

			WalletOrBuilder {

		private Wallet(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private Wallet(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final Wallet defaultInstance;

		public static Wallet getDefaultInstance() {
			return defaultInstance;
		}

		public Wallet getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private Wallet(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 10: {
						com.google.protobuf.ByteString bs = input.readBytes();
						bitField0_ |= 0x00000001;
						networkIdentifier_ = bs;
						break;
					}
					case 18: {
						bitField0_ |= 0x00000002;
						lastSeenBlockHash_ = input.readBytes();
						break;
					}
					case 26: {
						if (!((mutable_bitField0_ & 0x00000010) == 0x00000010)) {
							key_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.Key>();
							mutable_bitField0_ |= 0x00000010;
						}
						key_.add(input.readMessage(cros.mail.chain.wallet.Protos.Key.PARSER, extensionRegistry));
						break;
					}
					case 34: {
						if (!((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
							transaction_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.Transaction>();
							mutable_bitField0_ |= 0x00000020;
						}
						transaction_
								.add(input.readMessage(cros.mail.chain.wallet.Protos.Transaction.PARSER, extensionRegistry));
						break;
					}
					case 40: {
						int rawValue = input.readEnum();
						cros.mail.chain.wallet.Protos.Wallet.EncryptionType value = cros.mail.chain.wallet.Protos.Wallet.EncryptionType
								.valueOf(rawValue);
						if (value == null) {
							unknownFields.mergeVarintField(5, rawValue);
						} else {
							bitField0_ |= 0x00000010;
							encryptionType_ = value;
						}
						break;
					}
					case 50: {
						cros.mail.chain.wallet.Protos.ScryptParameters.Builder subBuilder = null;
						if (((bitField0_ & 0x00000020) == 0x00000020)) {
							subBuilder = encryptionParameters_.toBuilder();
						}
						encryptionParameters_ = input.readMessage(cros.mail.chain.wallet.Protos.ScryptParameters.PARSER,
								extensionRegistry);
						if (subBuilder != null) {
							subBuilder.mergeFrom(encryptionParameters_);
							encryptionParameters_ = subBuilder.buildPartial();
						}
						bitField0_ |= 0x00000020;
						break;
					}
					case 56: {
						bitField0_ |= 0x00000040;
						version_ = input.readInt32();
						break;
					}
					case 82: {
						if (!((mutable_bitField0_ & 0x00000400) == 0x00000400)) {
							extension_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.Extension>();
							mutable_bitField0_ |= 0x00000400;
						}
						extension_.add(input.readMessage(cros.mail.chain.wallet.Protos.Extension.PARSER, extensionRegistry));
						break;
					}
					case 90: {
						com.google.protobuf.ByteString bs = input.readBytes();
						bitField0_ |= 0x00000080;
						description_ = bs;
						break;
					}
					case 96: {
						bitField0_ |= 0x00000004;
						lastSeenBlockHeight_ = input.readUInt32();
						break;
					}
					case 104: {
						bitField0_ |= 0x00000100;
						keyRotationTime_ = input.readUInt64();
						break;
					}
					case 112: {
						bitField0_ |= 0x00000008;
						lastSeenBlockTimeSecs_ = input.readInt64();
						break;
					}
					case 122: {
						if (!((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
							watchedScript_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.Script>();
							mutable_bitField0_ |= 0x00000040;
						}
						watchedScript_.add(input.readMessage(cros.mail.chain.wallet.Protos.Script.PARSER, extensionRegistry));
						break;
					}
					case 130: {
						if (!((mutable_bitField0_ & 0x00002000) == 0x00002000)) {
							tags_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.Tag>();
							mutable_bitField0_ |= 0x00002000;
						}
						tags_.add(input.readMessage(cros.mail.chain.wallet.Protos.Tag.PARSER, extensionRegistry));
						break;
					}
					case 138: {
						if (!((mutable_bitField0_ & 0x00004000) == 0x00004000)) {
							transactionSigners_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.TransactionSigner>();
							mutable_bitField0_ |= 0x00004000;
						}
						transactionSigners_.add(
								input.readMessage(cros.mail.chain.wallet.Protos.TransactionSigner.PARSER, extensionRegistry));
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				if (((mutable_bitField0_ & 0x00000010) == 0x00000010)) {
					key_ = java.util.Collections.unmodifiableList(key_);
				}
				if (((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
					transaction_ = java.util.Collections.unmodifiableList(transaction_);
				}
				if (((mutable_bitField0_ & 0x00000400) == 0x00000400)) {
					extension_ = java.util.Collections.unmodifiableList(extension_);
				}
				if (((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
					watchedScript_ = java.util.Collections.unmodifiableList(watchedScript_);
				}
				if (((mutable_bitField0_ & 0x00002000) == 0x00002000)) {
					tags_ = java.util.Collections.unmodifiableList(tags_);
				}
				if (((mutable_bitField0_ & 0x00004000) == 0x00004000)) {
					transactionSigners_ = java.util.Collections.unmodifiableList(transactionSigners_);
				}
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Wallet_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_Wallet_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.Wallet.class,
							cros.mail.chain.wallet.Protos.Wallet.Builder.class);
		}

		public static com.google.protobuf.Parser<Wallet> PARSER = new com.google.protobuf.AbstractParser<Wallet>() {
			public Wallet parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new Wallet(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<Wallet> getParserForType() {
			return PARSER;
		}

		public enum EncryptionType implements com.google.protobuf.ProtocolMessageEnum {

			UNENCRYPTED(0, 1),

			ENCRYPTED_SCRYPT_AES(1, 2),;

			public static final int UNENCRYPTED_VALUE = 1;

			public static final int ENCRYPTED_SCRYPT_AES_VALUE = 2;

			public final int getNumber() {
				return value;
			}

			public static EncryptionType valueOf(int value) {
				switch (value) {
				case 1:
					return UNENCRYPTED;
				case 2:
					return ENCRYPTED_SCRYPT_AES;
				default:
					return null;
				}
			}

			public static com.google.protobuf.Internal.EnumLiteMap<EncryptionType> internalGetValueMap() {
				return internalValueMap;
			}

			private static com.google.protobuf.Internal.EnumLiteMap<EncryptionType> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<EncryptionType>() {
				public EncryptionType findValueByNumber(int number) {
					return EncryptionType.valueOf(number);
				}
			};

			public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
				return getDescriptor().getValues().get(index);
			}

			public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
				return getDescriptor();
			}

			public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.Wallet.getDescriptor().getEnumTypes().get(0);
			}

			private static final EncryptionType[] VALUES = values();

			public static EncryptionType valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
				if (desc.getType() != getDescriptor()) {
					throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
				}
				return VALUES[desc.getIndex()];
			}

			private final int index;
			private final int value;

			private EncryptionType(int index, int value) {
				this.index = index;
				this.value = value;
			}

		}

		private int bitField0_;
		public static final int NETWORK_IDENTIFIER_FIELD_NUMBER = 1;
		private java.lang.Object networkIdentifier_;

		public boolean hasNetworkIdentifier() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public java.lang.String getNetworkIdentifier() {
			java.lang.Object ref = networkIdentifier_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8()) {
					networkIdentifier_ = s;
				}
				return s;
			}
		}

		public com.google.protobuf.ByteString getNetworkIdentifierBytes() {
			java.lang.Object ref = networkIdentifier_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				networkIdentifier_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}

		public static final int LAST_SEEN_BLOCK_HASH_FIELD_NUMBER = 2;
		private com.google.protobuf.ByteString lastSeenBlockHash_;

		public boolean hasLastSeenBlockHash() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public com.google.protobuf.ByteString getLastSeenBlockHash() {
			return lastSeenBlockHash_;
		}

		public static final int LAST_SEEN_BLOCK_HEIGHT_FIELD_NUMBER = 12;
		private int lastSeenBlockHeight_;

		public boolean hasLastSeenBlockHeight() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public int getLastSeenBlockHeight() {
			return lastSeenBlockHeight_;
		}

		public static final int LAST_SEEN_BLOCK_TIME_SECS_FIELD_NUMBER = 14;
		private long lastSeenBlockTimeSecs_;

		public boolean hasLastSeenBlockTimeSecs() {
			return ((bitField0_ & 0x00000008) == 0x00000008);
		}

		public long getLastSeenBlockTimeSecs() {
			return lastSeenBlockTimeSecs_;
		}

		public static final int KEY_FIELD_NUMBER = 3;
		private java.util.List<cros.mail.chain.wallet.Protos.Key> key_;

		public java.util.List<cros.mail.chain.wallet.Protos.Key> getKeyList() {
			return key_;
		}

		public java.util.List<? extends cros.mail.chain.wallet.Protos.KeyOrBuilder> getKeyOrBuilderList() {
			return key_;
		}

		public int getKeyCount() {
			return key_.size();
		}

		public cros.mail.chain.wallet.Protos.Key getKey(int index) {
			return key_.get(index);
		}

		public cros.mail.chain.wallet.Protos.KeyOrBuilder getKeyOrBuilder(int index) {
			return key_.get(index);
		}

		public static final int TRANSACTION_FIELD_NUMBER = 4;
		private java.util.List<cros.mail.chain.wallet.Protos.Transaction> transaction_;

		public java.util.List<cros.mail.chain.wallet.Protos.Transaction> getTransactionList() {
			return transaction_;
		}

		public java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionOrBuilder> getTransactionOrBuilderList() {
			return transaction_;
		}

		public int getTransactionCount() {
			return transaction_.size();
		}

		public cros.mail.chain.wallet.Protos.Transaction getTransaction(int index) {
			return transaction_.get(index);
		}

		public cros.mail.chain.wallet.Protos.TransactionOrBuilder getTransactionOrBuilder(int index) {
			return transaction_.get(index);
		}

		public static final int WATCHED_SCRIPT_FIELD_NUMBER = 15;
		private java.util.List<cros.mail.chain.wallet.Protos.Script> watchedScript_;

		public java.util.List<cros.mail.chain.wallet.Protos.Script> getWatchedScriptList() {
			return watchedScript_;
		}

		public java.util.List<? extends cros.mail.chain.wallet.Protos.ScriptOrBuilder> getWatchedScriptOrBuilderList() {
			return watchedScript_;
		}

		public int getWatchedScriptCount() {
			return watchedScript_.size();
		}

		public cros.mail.chain.wallet.Protos.Script getWatchedScript(int index) {
			return watchedScript_.get(index);
		}

		public cros.mail.chain.wallet.Protos.ScriptOrBuilder getWatchedScriptOrBuilder(int index) {
			return watchedScript_.get(index);
		}

		public static final int ENCRYPTION_TYPE_FIELD_NUMBER = 5;
		private cros.mail.chain.wallet.Protos.Wallet.EncryptionType encryptionType_;

		public boolean hasEncryptionType() {
			return ((bitField0_ & 0x00000010) == 0x00000010);
		}

		public cros.mail.chain.wallet.Protos.Wallet.EncryptionType getEncryptionType() {
			return encryptionType_;
		}

		public static final int ENCRYPTION_PARAMETERS_FIELD_NUMBER = 6;
		private cros.mail.chain.wallet.Protos.ScryptParameters encryptionParameters_;

		public boolean hasEncryptionParameters() {
			return ((bitField0_ & 0x00000020) == 0x00000020);
		}

		public cros.mail.chain.wallet.Protos.ScryptParameters getEncryptionParameters() {
			return encryptionParameters_;
		}

		public cros.mail.chain.wallet.Protos.ScryptParametersOrBuilder getEncryptionParametersOrBuilder() {
			return encryptionParameters_;
		}

		public static final int VERSION_FIELD_NUMBER = 7;
		private int version_;

		public boolean hasVersion() {
			return ((bitField0_ & 0x00000040) == 0x00000040);
		}

		public int getVersion() {
			return version_;
		}

		public static final int EXTENSION_FIELD_NUMBER = 10;
		private java.util.List<cros.mail.chain.wallet.Protos.Extension> extension_;

		public java.util.List<cros.mail.chain.wallet.Protos.Extension> getExtensionList() {
			return extension_;
		}

		public java.util.List<? extends cros.mail.chain.wallet.Protos.ExtensionOrBuilder> getExtensionOrBuilderList() {
			return extension_;
		}

		public int getExtensionCount() {
			return extension_.size();
		}

		public cros.mail.chain.wallet.Protos.Extension getExtension(int index) {
			return extension_.get(index);
		}

		public cros.mail.chain.wallet.Protos.ExtensionOrBuilder getExtensionOrBuilder(int index) {
			return extension_.get(index);
		}

		public static final int DESCRIPTION_FIELD_NUMBER = 11;
		private java.lang.Object description_;

		public boolean hasDescription() {
			return ((bitField0_ & 0x00000080) == 0x00000080);
		}

		public java.lang.String getDescription() {
			java.lang.Object ref = description_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8()) {
					description_ = s;
				}
				return s;
			}
		}

		public com.google.protobuf.ByteString getDescriptionBytes() {
			java.lang.Object ref = description_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				description_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}

		public static final int KEY_ROTATION_TIME_FIELD_NUMBER = 13;
		private long keyRotationTime_;

		public boolean hasKeyRotationTime() {
			return ((bitField0_ & 0x00000100) == 0x00000100);
		}

		public long getKeyRotationTime() {
			return keyRotationTime_;
		}

		public static final int TAGS_FIELD_NUMBER = 16;
		private java.util.List<cros.mail.chain.wallet.Protos.Tag> tags_;

		public java.util.List<cros.mail.chain.wallet.Protos.Tag> getTagsList() {
			return tags_;
		}

		public java.util.List<? extends cros.mail.chain.wallet.Protos.TagOrBuilder> getTagsOrBuilderList() {
			return tags_;
		}

		public int getTagsCount() {
			return tags_.size();
		}

		public cros.mail.chain.wallet.Protos.Tag getTags(int index) {
			return tags_.get(index);
		}

		public cros.mail.chain.wallet.Protos.TagOrBuilder getTagsOrBuilder(int index) {
			return tags_.get(index);
		}

		public static final int TRANSACTION_SIGNERS_FIELD_NUMBER = 17;
		private java.util.List<cros.mail.chain.wallet.Protos.TransactionSigner> transactionSigners_;

		public java.util.List<cros.mail.chain.wallet.Protos.TransactionSigner> getTransactionSignersList() {
			return transactionSigners_;
		}

		public java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionSignerOrBuilder> getTransactionSignersOrBuilderList() {
			return transactionSigners_;
		}

		public int getTransactionSignersCount() {
			return transactionSigners_.size();
		}

		public cros.mail.chain.wallet.Protos.TransactionSigner getTransactionSigners(int index) {
			return transactionSigners_.get(index);
		}

		public cros.mail.chain.wallet.Protos.TransactionSignerOrBuilder getTransactionSignersOrBuilder(int index) {
			return transactionSigners_.get(index);
		}

		private void initFields() {
			networkIdentifier_ = "";
			lastSeenBlockHash_ = com.google.protobuf.ByteString.EMPTY;
			lastSeenBlockHeight_ = 0;
			lastSeenBlockTimeSecs_ = 0L;
			key_ = java.util.Collections.emptyList();
			transaction_ = java.util.Collections.emptyList();
			watchedScript_ = java.util.Collections.emptyList();
			encryptionType_ = cros.mail.chain.wallet.Protos.Wallet.EncryptionType.UNENCRYPTED;
			encryptionParameters_ = cros.mail.chain.wallet.Protos.ScryptParameters.getDefaultInstance();
			version_ = 1;
			extension_ = java.util.Collections.emptyList();
			description_ = "";
			keyRotationTime_ = 0L;
			tags_ = java.util.Collections.emptyList();
			transactionSigners_ = java.util.Collections.emptyList();
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasNetworkIdentifier()) {
				memoizedIsInitialized = 0;
				return false;
			}
			for (int i = 0; i < getKeyCount(); i++) {
				if (!getKey(i).isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			for (int i = 0; i < getTransactionCount(); i++) {
				if (!getTransaction(i).isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			for (int i = 0; i < getWatchedScriptCount(); i++) {
				if (!getWatchedScript(i).isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			if (hasEncryptionParameters()) {
				if (!getEncryptionParameters().isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			for (int i = 0; i < getExtensionCount(); i++) {
				if (!getExtension(i).isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			for (int i = 0; i < getTagsCount(); i++) {
				if (!getTags(i).isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			for (int i = 0; i < getTransactionSignersCount(); i++) {
				if (!getTransactionSigners(i).isInitialized()) {
					memoizedIsInitialized = 0;
					return false;
				}
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeBytes(1, getNetworkIdentifierBytes());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeBytes(2, lastSeenBlockHash_);
			}
			for (int i = 0; i < key_.size(); i++) {
				output.writeMessage(3, key_.get(i));
			}
			for (int i = 0; i < transaction_.size(); i++) {
				output.writeMessage(4, transaction_.get(i));
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				output.writeEnum(5, encryptionType_.getNumber());
			}
			if (((bitField0_ & 0x00000020) == 0x00000020)) {
				output.writeMessage(6, encryptionParameters_);
			}
			if (((bitField0_ & 0x00000040) == 0x00000040)) {
				output.writeInt32(7, version_);
			}
			for (int i = 0; i < extension_.size(); i++) {
				output.writeMessage(10, extension_.get(i));
			}
			if (((bitField0_ & 0x00000080) == 0x00000080)) {
				output.writeBytes(11, getDescriptionBytes());
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeUInt32(12, lastSeenBlockHeight_);
			}
			if (((bitField0_ & 0x00000100) == 0x00000100)) {
				output.writeUInt64(13, keyRotationTime_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				output.writeInt64(14, lastSeenBlockTimeSecs_);
			}
			for (int i = 0; i < watchedScript_.size(); i++) {
				output.writeMessage(15, watchedScript_.get(i));
			}
			for (int i = 0; i < tags_.size(); i++) {
				output.writeMessage(16, tags_.get(i));
			}
			for (int i = 0; i < transactionSigners_.size(); i++) {
				output.writeMessage(17, transactionSigners_.get(i));
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getNetworkIdentifierBytes());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, lastSeenBlockHash_);
			}
			for (int i = 0; i < key_.size(); i++) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, key_.get(i));
			}
			for (int i = 0; i < transaction_.size(); i++) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(4, transaction_.get(i));
			}
			if (((bitField0_ & 0x00000010) == 0x00000010)) {
				size += com.google.protobuf.CodedOutputStream.computeEnumSize(5, encryptionType_.getNumber());
			}
			if (((bitField0_ & 0x00000020) == 0x00000020)) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(6, encryptionParameters_);
			}
			if (((bitField0_ & 0x00000040) == 0x00000040)) {
				size += com.google.protobuf.CodedOutputStream.computeInt32Size(7, version_);
			}
			for (int i = 0; i < extension_.size(); i++) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(10, extension_.get(i));
			}
			if (((bitField0_ & 0x00000080) == 0x00000080)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(11, getDescriptionBytes());
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeUInt32Size(12, lastSeenBlockHeight_);
			}
			if (((bitField0_ & 0x00000100) == 0x00000100)) {
				size += com.google.protobuf.CodedOutputStream.computeUInt64Size(13, keyRotationTime_);
			}
			if (((bitField0_ & 0x00000008) == 0x00000008)) {
				size += com.google.protobuf.CodedOutputStream.computeInt64Size(14, lastSeenBlockTimeSecs_);
			}
			for (int i = 0; i < watchedScript_.size(); i++) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(15, watchedScript_.get(i));
			}
			for (int i = 0; i < tags_.size(); i++) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(16, tags_.get(i));
			}
			for (int i = 0; i < transactionSigners_.size(); i++) {
				size += com.google.protobuf.CodedOutputStream.computeMessageSize(17, transactionSigners_.get(i));
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.Wallet parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Wallet parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Wallet parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.Wallet parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Wallet parseFrom(java.io.InputStream input) throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Wallet parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Wallet parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Wallet parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.Wallet parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.Wallet parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.Wallet prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

				cros.mail.chain.wallet.Protos.WalletOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Wallet_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Wallet_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.Wallet.class,
								cros.mail.chain.wallet.Protos.Wallet.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
					getKeyFieldBuilder();
					getTransactionFieldBuilder();
					getWatchedScriptFieldBuilder();
					getEncryptionParametersFieldBuilder();
					getExtensionFieldBuilder();
					getTagsFieldBuilder();
					getTransactionSignersFieldBuilder();
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				networkIdentifier_ = "";
				bitField0_ = (bitField0_ & ~0x00000001);
				lastSeenBlockHash_ = com.google.protobuf.ByteString.EMPTY;
				bitField0_ = (bitField0_ & ~0x00000002);
				lastSeenBlockHeight_ = 0;
				bitField0_ = (bitField0_ & ~0x00000004);
				lastSeenBlockTimeSecs_ = 0L;
				bitField0_ = (bitField0_ & ~0x00000008);
				if (keyBuilder_ == null) {
					key_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000010);
				} else {
					keyBuilder_.clear();
				}
				if (transactionBuilder_ == null) {
					transaction_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000020);
				} else {
					transactionBuilder_.clear();
				}
				if (watchedScriptBuilder_ == null) {
					watchedScript_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000040);
				} else {
					watchedScriptBuilder_.clear();
				}
				encryptionType_ = cros.mail.chain.wallet.Protos.Wallet.EncryptionType.UNENCRYPTED;
				bitField0_ = (bitField0_ & ~0x00000080);
				if (encryptionParametersBuilder_ == null) {
					encryptionParameters_ = cros.mail.chain.wallet.Protos.ScryptParameters.getDefaultInstance();
				} else {
					encryptionParametersBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000100);
				version_ = 1;
				bitField0_ = (bitField0_ & ~0x00000200);
				if (extensionBuilder_ == null) {
					extension_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000400);
				} else {
					extensionBuilder_.clear();
				}
				description_ = "";
				bitField0_ = (bitField0_ & ~0x00000800);
				keyRotationTime_ = 0L;
				bitField0_ = (bitField0_ & ~0x00001000);
				if (tagsBuilder_ == null) {
					tags_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00002000);
				} else {
					tagsBuilder_.clear();
				}
				if (transactionSignersBuilder_ == null) {
					transactionSigners_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00004000);
				} else {
					transactionSignersBuilder_.clear();
				}
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_Wallet_descriptor;
			}

			public cros.mail.chain.wallet.Protos.Wallet getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.Wallet.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.Wallet build() {
				cros.mail.chain.wallet.Protos.Wallet result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.Wallet buildPartial() {
				cros.mail.chain.wallet.Protos.Wallet result = new cros.mail.chain.wallet.Protos.Wallet(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.networkIdentifier_ = networkIdentifier_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.lastSeenBlockHash_ = lastSeenBlockHash_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000004;
				}
				result.lastSeenBlockHeight_ = lastSeenBlockHeight_;
				if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
					to_bitField0_ |= 0x00000008;
				}
				result.lastSeenBlockTimeSecs_ = lastSeenBlockTimeSecs_;
				if (keyBuilder_ == null) {
					if (((bitField0_ & 0x00000010) == 0x00000010)) {
						key_ = java.util.Collections.unmodifiableList(key_);
						bitField0_ = (bitField0_ & ~0x00000010);
					}
					result.key_ = key_;
				} else {
					result.key_ = keyBuilder_.build();
				}
				if (transactionBuilder_ == null) {
					if (((bitField0_ & 0x00000020) == 0x00000020)) {
						transaction_ = java.util.Collections.unmodifiableList(transaction_);
						bitField0_ = (bitField0_ & ~0x00000020);
					}
					result.transaction_ = transaction_;
				} else {
					result.transaction_ = transactionBuilder_.build();
				}
				if (watchedScriptBuilder_ == null) {
					if (((bitField0_ & 0x00000040) == 0x00000040)) {
						watchedScript_ = java.util.Collections.unmodifiableList(watchedScript_);
						bitField0_ = (bitField0_ & ~0x00000040);
					}
					result.watchedScript_ = watchedScript_;
				} else {
					result.watchedScript_ = watchedScriptBuilder_.build();
				}
				if (((from_bitField0_ & 0x00000080) == 0x00000080)) {
					to_bitField0_ |= 0x00000010;
				}
				result.encryptionType_ = encryptionType_;
				if (((from_bitField0_ & 0x00000100) == 0x00000100)) {
					to_bitField0_ |= 0x00000020;
				}
				if (encryptionParametersBuilder_ == null) {
					result.encryptionParameters_ = encryptionParameters_;
				} else {
					result.encryptionParameters_ = encryptionParametersBuilder_.build();
				}
				if (((from_bitField0_ & 0x00000200) == 0x00000200)) {
					to_bitField0_ |= 0x00000040;
				}
				result.version_ = version_;
				if (extensionBuilder_ == null) {
					if (((bitField0_ & 0x00000400) == 0x00000400)) {
						extension_ = java.util.Collections.unmodifiableList(extension_);
						bitField0_ = (bitField0_ & ~0x00000400);
					}
					result.extension_ = extension_;
				} else {
					result.extension_ = extensionBuilder_.build();
				}
				if (((from_bitField0_ & 0x00000800) == 0x00000800)) {
					to_bitField0_ |= 0x00000080;
				}
				result.description_ = description_;
				if (((from_bitField0_ & 0x00001000) == 0x00001000)) {
					to_bitField0_ |= 0x00000100;
				}
				result.keyRotationTime_ = keyRotationTime_;
				if (tagsBuilder_ == null) {
					if (((bitField0_ & 0x00002000) == 0x00002000)) {
						tags_ = java.util.Collections.unmodifiableList(tags_);
						bitField0_ = (bitField0_ & ~0x00002000);
					}
					result.tags_ = tags_;
				} else {
					result.tags_ = tagsBuilder_.build();
				}
				if (transactionSignersBuilder_ == null) {
					if (((bitField0_ & 0x00004000) == 0x00004000)) {
						transactionSigners_ = java.util.Collections.unmodifiableList(transactionSigners_);
						bitField0_ = (bitField0_ & ~0x00004000);
					}
					result.transactionSigners_ = transactionSigners_;
				} else {
					result.transactionSigners_ = transactionSignersBuilder_.build();
				}
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.Wallet) {
					return mergeFrom((cros.mail.chain.wallet.Protos.Wallet) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.Wallet other) {
				if (other == cros.mail.chain.wallet.Protos.Wallet.getDefaultInstance())
					return this;
				if (other.hasNetworkIdentifier()) {
					bitField0_ |= 0x00000001;
					networkIdentifier_ = other.networkIdentifier_;
					onChanged();
				}
				if (other.hasLastSeenBlockHash()) {
					setLastSeenBlockHash(other.getLastSeenBlockHash());
				}
				if (other.hasLastSeenBlockHeight()) {
					setLastSeenBlockHeight(other.getLastSeenBlockHeight());
				}
				if (other.hasLastSeenBlockTimeSecs()) {
					setLastSeenBlockTimeSecs(other.getLastSeenBlockTimeSecs());
				}
				if (keyBuilder_ == null) {
					if (!other.key_.isEmpty()) {
						if (key_.isEmpty()) {
							key_ = other.key_;
							bitField0_ = (bitField0_ & ~0x00000010);
						} else {
							ensureKeyIsMutable();
							key_.addAll(other.key_);
						}
						onChanged();
					}
				} else {
					if (!other.key_.isEmpty()) {
						if (keyBuilder_.isEmpty()) {
							keyBuilder_.dispose();
							keyBuilder_ = null;
							key_ = other.key_;
							bitField0_ = (bitField0_ & ~0x00000010);
							keyBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders
									? getKeyFieldBuilder()
									: null;
						} else {
							keyBuilder_.addAllMessages(other.key_);
						}
					}
				}
				if (transactionBuilder_ == null) {
					if (!other.transaction_.isEmpty()) {
						if (transaction_.isEmpty()) {
							transaction_ = other.transaction_;
							bitField0_ = (bitField0_ & ~0x00000020);
						} else {
							ensureTransactionIsMutable();
							transaction_.addAll(other.transaction_);
						}
						onChanged();
					}
				} else {
					if (!other.transaction_.isEmpty()) {
						if (transactionBuilder_.isEmpty()) {
							transactionBuilder_.dispose();
							transactionBuilder_ = null;
							transaction_ = other.transaction_;
							bitField0_ = (bitField0_ & ~0x00000020);
							transactionBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders
									? getTransactionFieldBuilder()
									: null;
						} else {
							transactionBuilder_.addAllMessages(other.transaction_);
						}
					}
				}
				if (watchedScriptBuilder_ == null) {
					if (!other.watchedScript_.isEmpty()) {
						if (watchedScript_.isEmpty()) {
							watchedScript_ = other.watchedScript_;
							bitField0_ = (bitField0_ & ~0x00000040);
						} else {
							ensureWatchedScriptIsMutable();
							watchedScript_.addAll(other.watchedScript_);
						}
						onChanged();
					}
				} else {
					if (!other.watchedScript_.isEmpty()) {
						if (watchedScriptBuilder_.isEmpty()) {
							watchedScriptBuilder_.dispose();
							watchedScriptBuilder_ = null;
							watchedScript_ = other.watchedScript_;
							bitField0_ = (bitField0_ & ~0x00000040);
							watchedScriptBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders
									? getWatchedScriptFieldBuilder()
									: null;
						} else {
							watchedScriptBuilder_.addAllMessages(other.watchedScript_);
						}
					}
				}
				if (other.hasEncryptionType()) {
					setEncryptionType(other.getEncryptionType());
				}
				if (other.hasEncryptionParameters()) {
					mergeEncryptionParameters(other.getEncryptionParameters());
				}
				if (other.hasVersion()) {
					setVersion(other.getVersion());
				}
				if (extensionBuilder_ == null) {
					if (!other.extension_.isEmpty()) {
						if (extension_.isEmpty()) {
							extension_ = other.extension_;
							bitField0_ = (bitField0_ & ~0x00000400);
						} else {
							ensureExtensionIsMutable();
							extension_.addAll(other.extension_);
						}
						onChanged();
					}
				} else {
					if (!other.extension_.isEmpty()) {
						if (extensionBuilder_.isEmpty()) {
							extensionBuilder_.dispose();
							extensionBuilder_ = null;
							extension_ = other.extension_;
							bitField0_ = (bitField0_ & ~0x00000400);
							extensionBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders
									? getExtensionFieldBuilder()
									: null;
						} else {
							extensionBuilder_.addAllMessages(other.extension_);
						}
					}
				}
				if (other.hasDescription()) {
					bitField0_ |= 0x00000800;
					description_ = other.description_;
					onChanged();
				}
				if (other.hasKeyRotationTime()) {
					setKeyRotationTime(other.getKeyRotationTime());
				}
				if (tagsBuilder_ == null) {
					if (!other.tags_.isEmpty()) {
						if (tags_.isEmpty()) {
							tags_ = other.tags_;
							bitField0_ = (bitField0_ & ~0x00002000);
						} else {
							ensureTagsIsMutable();
							tags_.addAll(other.tags_);
						}
						onChanged();
					}
				} else {
					if (!other.tags_.isEmpty()) {
						if (tagsBuilder_.isEmpty()) {
							tagsBuilder_.dispose();
							tagsBuilder_ = null;
							tags_ = other.tags_;
							bitField0_ = (bitField0_ & ~0x00002000);
							tagsBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders
									? getTagsFieldBuilder()
									: null;
						} else {
							tagsBuilder_.addAllMessages(other.tags_);
						}
					}
				}
				if (transactionSignersBuilder_ == null) {
					if (!other.transactionSigners_.isEmpty()) {
						if (transactionSigners_.isEmpty()) {
							transactionSigners_ = other.transactionSigners_;
							bitField0_ = (bitField0_ & ~0x00004000);
						} else {
							ensureTransactionSignersIsMutable();
							transactionSigners_.addAll(other.transactionSigners_);
						}
						onChanged();
					}
				} else {
					if (!other.transactionSigners_.isEmpty()) {
						if (transactionSignersBuilder_.isEmpty()) {
							transactionSignersBuilder_.dispose();
							transactionSignersBuilder_ = null;
							transactionSigners_ = other.transactionSigners_;
							bitField0_ = (bitField0_ & ~0x00004000);
							transactionSignersBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders
									? getTransactionSignersFieldBuilder()
									: null;
						} else {
							transactionSignersBuilder_.addAllMessages(other.transactionSigners_);
						}
					}
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasNetworkIdentifier()) {

					return false;
				}
				for (int i = 0; i < getKeyCount(); i++) {
					if (!getKey(i).isInitialized()) {

						return false;
					}
				}
				for (int i = 0; i < getTransactionCount(); i++) {
					if (!getTransaction(i).isInitialized()) {

						return false;
					}
				}
				for (int i = 0; i < getWatchedScriptCount(); i++) {
					if (!getWatchedScript(i).isInitialized()) {

						return false;
					}
				}
				if (hasEncryptionParameters()) {
					if (!getEncryptionParameters().isInitialized()) {

						return false;
					}
				}
				for (int i = 0; i < getExtensionCount(); i++) {
					if (!getExtension(i).isInitialized()) {

						return false;
					}
				}
				for (int i = 0; i < getTagsCount(); i++) {
					if (!getTags(i).isInitialized()) {

						return false;
					}
				}
				for (int i = 0; i < getTransactionSignersCount(); i++) {
					if (!getTransactionSigners(i).isInitialized()) {

						return false;
					}
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.Wallet parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.Wallet) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private java.lang.Object networkIdentifier_ = "";

			public boolean hasNetworkIdentifier() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public java.lang.String getNetworkIdentifier() {
				java.lang.Object ref = networkIdentifier_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						networkIdentifier_ = s;
					}
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}

			public com.google.protobuf.ByteString getNetworkIdentifierBytes() {
				java.lang.Object ref = networkIdentifier_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					networkIdentifier_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			public Builder setNetworkIdentifier(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				networkIdentifier_ = value;
				onChanged();
				return this;
			}

			public Builder clearNetworkIdentifier() {
				bitField0_ = (bitField0_ & ~0x00000001);
				networkIdentifier_ = getDefaultInstance().getNetworkIdentifier();
				onChanged();
				return this;
			}

			public Builder setNetworkIdentifierBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				networkIdentifier_ = value;
				onChanged();
				return this;
			}

			private com.google.protobuf.ByteString lastSeenBlockHash_ = com.google.protobuf.ByteString.EMPTY;

			public boolean hasLastSeenBlockHash() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public com.google.protobuf.ByteString getLastSeenBlockHash() {
				return lastSeenBlockHash_;
			}

			public Builder setLastSeenBlockHash(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000002;
				lastSeenBlockHash_ = value;
				onChanged();
				return this;
			}

			public Builder clearLastSeenBlockHash() {
				bitField0_ = (bitField0_ & ~0x00000002);
				lastSeenBlockHash_ = getDefaultInstance().getLastSeenBlockHash();
				onChanged();
				return this;
			}

			private int lastSeenBlockHeight_;

			public boolean hasLastSeenBlockHeight() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public int getLastSeenBlockHeight() {
				return lastSeenBlockHeight_;
			}

			public Builder setLastSeenBlockHeight(int value) {
				bitField0_ |= 0x00000004;
				lastSeenBlockHeight_ = value;
				onChanged();
				return this;
			}

			public Builder clearLastSeenBlockHeight() {
				bitField0_ = (bitField0_ & ~0x00000004);
				lastSeenBlockHeight_ = 0;
				onChanged();
				return this;
			}

			private long lastSeenBlockTimeSecs_;

			public boolean hasLastSeenBlockTimeSecs() {
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			public long getLastSeenBlockTimeSecs() {
				return lastSeenBlockTimeSecs_;
			}

			public Builder setLastSeenBlockTimeSecs(long value) {
				bitField0_ |= 0x00000008;
				lastSeenBlockTimeSecs_ = value;
				onChanged();
				return this;
			}

			public Builder clearLastSeenBlockTimeSecs() {
				bitField0_ = (bitField0_ & ~0x00000008);
				lastSeenBlockTimeSecs_ = 0L;
				onChanged();
				return this;
			}

			private java.util.List<cros.mail.chain.wallet.Protos.Key> key_ = java.util.Collections.emptyList();

			private void ensureKeyIsMutable() {
				if (!((bitField0_ & 0x00000010) == 0x00000010)) {
					key_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.Key>(key_);
					bitField0_ |= 0x00000010;
				}
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Key, cros.mail.chain.wallet.Protos.Key.Builder, cros.mail.chain.wallet.Protos.KeyOrBuilder> keyBuilder_;

			public java.util.List<cros.mail.chain.wallet.Protos.Key> getKeyList() {
				if (keyBuilder_ == null) {
					return java.util.Collections.unmodifiableList(key_);
				} else {
					return keyBuilder_.getMessageList();
				}
			}

			public int getKeyCount() {
				if (keyBuilder_ == null) {
					return key_.size();
				} else {
					return keyBuilder_.getCount();
				}
			}

			public cros.mail.chain.wallet.Protos.Key getKey(int index) {
				if (keyBuilder_ == null) {
					return key_.get(index);
				} else {
					return keyBuilder_.getMessage(index);
				}
			}

			public Builder setKey(int index, cros.mail.chain.wallet.Protos.Key value) {
				if (keyBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureKeyIsMutable();
					key_.set(index, value);
					onChanged();
				} else {
					keyBuilder_.setMessage(index, value);
				}
				return this;
			}

			public Builder setKey(int index, cros.mail.chain.wallet.Protos.Key.Builder builderForValue) {
				if (keyBuilder_ == null) {
					ensureKeyIsMutable();
					key_.set(index, builderForValue.build());
					onChanged();
				} else {
					keyBuilder_.setMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addKey(cros.mail.chain.wallet.Protos.Key value) {
				if (keyBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureKeyIsMutable();
					key_.add(value);
					onChanged();
				} else {
					keyBuilder_.addMessage(value);
				}
				return this;
			}

			public Builder addKey(int index, cros.mail.chain.wallet.Protos.Key value) {
				if (keyBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureKeyIsMutable();
					key_.add(index, value);
					onChanged();
				} else {
					keyBuilder_.addMessage(index, value);
				}
				return this;
			}

			public Builder addKey(cros.mail.chain.wallet.Protos.Key.Builder builderForValue) {
				if (keyBuilder_ == null) {
					ensureKeyIsMutable();
					key_.add(builderForValue.build());
					onChanged();
				} else {
					keyBuilder_.addMessage(builderForValue.build());
				}
				return this;
			}

			public Builder addKey(int index, cros.mail.chain.wallet.Protos.Key.Builder builderForValue) {
				if (keyBuilder_ == null) {
					ensureKeyIsMutable();
					key_.add(index, builderForValue.build());
					onChanged();
				} else {
					keyBuilder_.addMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addAllKey(java.lang.Iterable<? extends cros.mail.chain.wallet.Protos.Key> values) {
				if (keyBuilder_ == null) {
					ensureKeyIsMutable();
					com.google.protobuf.AbstractMessageLite.Builder.addAll(values, key_);
					onChanged();
				} else {
					keyBuilder_.addAllMessages(values);
				}
				return this;
			}

			public Builder clearKey() {
				if (keyBuilder_ == null) {
					key_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000010);
					onChanged();
				} else {
					keyBuilder_.clear();
				}
				return this;
			}

			public Builder removeKey(int index) {
				if (keyBuilder_ == null) {
					ensureKeyIsMutable();
					key_.remove(index);
					onChanged();
				} else {
					keyBuilder_.remove(index);
				}
				return this;
			}

			public cros.mail.chain.wallet.Protos.Key.Builder getKeyBuilder(int index) {
				return getKeyFieldBuilder().getBuilder(index);
			}

			public cros.mail.chain.wallet.Protos.KeyOrBuilder getKeyOrBuilder(int index) {
				if (keyBuilder_ == null) {
					return key_.get(index);
				} else {
					return keyBuilder_.getMessageOrBuilder(index);
				}
			}

			public java.util.List<? extends cros.mail.chain.wallet.Protos.KeyOrBuilder> getKeyOrBuilderList() {
				if (keyBuilder_ != null) {
					return keyBuilder_.getMessageOrBuilderList();
				} else {
					return java.util.Collections.unmodifiableList(key_);
				}
			}

			public cros.mail.chain.wallet.Protos.Key.Builder addKeyBuilder() {
				return getKeyFieldBuilder().addBuilder(cros.mail.chain.wallet.Protos.Key.getDefaultInstance());
			}

			public cros.mail.chain.wallet.Protos.Key.Builder addKeyBuilder(int index) {
				return getKeyFieldBuilder().addBuilder(index, cros.mail.chain.wallet.Protos.Key.getDefaultInstance());
			}

			public java.util.List<cros.mail.chain.wallet.Protos.Key.Builder> getKeyBuilderList() {
				return getKeyFieldBuilder().getBuilderList();
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Key, cros.mail.chain.wallet.Protos.Key.Builder, cros.mail.chain.wallet.Protos.KeyOrBuilder> getKeyFieldBuilder() {
				if (keyBuilder_ == null) {
					keyBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Key, cros.mail.chain.wallet.Protos.Key.Builder, cros.mail.chain.wallet.Protos.KeyOrBuilder>(
							key_, ((bitField0_ & 0x00000010) == 0x00000010), getParentForChildren(), isClean());
					key_ = null;
				}
				return keyBuilder_;
			}

			private java.util.List<cros.mail.chain.wallet.Protos.Transaction> transaction_ = java.util.Collections.emptyList();

			private void ensureTransactionIsMutable() {
				if (!((bitField0_ & 0x00000020) == 0x00000020)) {
					transaction_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.Transaction>(transaction_);
					bitField0_ |= 0x00000020;
				}
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Transaction, cros.mail.chain.wallet.Protos.Transaction.Builder, cros.mail.chain.wallet.Protos.TransactionOrBuilder> transactionBuilder_;

			public java.util.List<cros.mail.chain.wallet.Protos.Transaction> getTransactionList() {
				if (transactionBuilder_ == null) {
					return java.util.Collections.unmodifiableList(transaction_);
				} else {
					return transactionBuilder_.getMessageList();
				}
			}

			public int getTransactionCount() {
				if (transactionBuilder_ == null) {
					return transaction_.size();
				} else {
					return transactionBuilder_.getCount();
				}
			}

			public cros.mail.chain.wallet.Protos.Transaction getTransaction(int index) {
				if (transactionBuilder_ == null) {
					return transaction_.get(index);
				} else {
					return transactionBuilder_.getMessage(index);
				}
			}

			public Builder setTransaction(int index, cros.mail.chain.wallet.Protos.Transaction value) {
				if (transactionBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionIsMutable();
					transaction_.set(index, value);
					onChanged();
				} else {
					transactionBuilder_.setMessage(index, value);
				}
				return this;
			}

			public Builder setTransaction(int index, cros.mail.chain.wallet.Protos.Transaction.Builder builderForValue) {
				if (transactionBuilder_ == null) {
					ensureTransactionIsMutable();
					transaction_.set(index, builderForValue.build());
					onChanged();
				} else {
					transactionBuilder_.setMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addTransaction(cros.mail.chain.wallet.Protos.Transaction value) {
				if (transactionBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionIsMutable();
					transaction_.add(value);
					onChanged();
				} else {
					transactionBuilder_.addMessage(value);
				}
				return this;
			}

			public Builder addTransaction(int index, cros.mail.chain.wallet.Protos.Transaction value) {
				if (transactionBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionIsMutable();
					transaction_.add(index, value);
					onChanged();
				} else {
					transactionBuilder_.addMessage(index, value);
				}
				return this;
			}

			public Builder addTransaction(cros.mail.chain.wallet.Protos.Transaction.Builder builderForValue) {
				if (transactionBuilder_ == null) {
					ensureTransactionIsMutable();
					transaction_.add(builderForValue.build());
					onChanged();
				} else {
					transactionBuilder_.addMessage(builderForValue.build());
				}
				return this;
			}

			public Builder addTransaction(int index, cros.mail.chain.wallet.Protos.Transaction.Builder builderForValue) {
				if (transactionBuilder_ == null) {
					ensureTransactionIsMutable();
					transaction_.add(index, builderForValue.build());
					onChanged();
				} else {
					transactionBuilder_.addMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addAllTransaction(java.lang.Iterable<? extends cros.mail.chain.wallet.Protos.Transaction> values) {
				if (transactionBuilder_ == null) {
					ensureTransactionIsMutable();
					com.google.protobuf.AbstractMessageLite.Builder.addAll(values, transaction_);
					onChanged();
				} else {
					transactionBuilder_.addAllMessages(values);
				}
				return this;
			}

			public Builder clearTransaction() {
				if (transactionBuilder_ == null) {
					transaction_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000020);
					onChanged();
				} else {
					transactionBuilder_.clear();
				}
				return this;
			}

			public Builder removeTransaction(int index) {
				if (transactionBuilder_ == null) {
					ensureTransactionIsMutable();
					transaction_.remove(index);
					onChanged();
				} else {
					transactionBuilder_.remove(index);
				}
				return this;
			}

			public cros.mail.chain.wallet.Protos.Transaction.Builder getTransactionBuilder(int index) {
				return getTransactionFieldBuilder().getBuilder(index);
			}

			public cros.mail.chain.wallet.Protos.TransactionOrBuilder getTransactionOrBuilder(int index) {
				if (transactionBuilder_ == null) {
					return transaction_.get(index);
				} else {
					return transactionBuilder_.getMessageOrBuilder(index);
				}
			}

			public java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionOrBuilder> getTransactionOrBuilderList() {
				if (transactionBuilder_ != null) {
					return transactionBuilder_.getMessageOrBuilderList();
				} else {
					return java.util.Collections.unmodifiableList(transaction_);
				}
			}

			public cros.mail.chain.wallet.Protos.Transaction.Builder addTransactionBuilder() {
				return getTransactionFieldBuilder().addBuilder(cros.mail.chain.wallet.Protos.Transaction.getDefaultInstance());
			}

			public cros.mail.chain.wallet.Protos.Transaction.Builder addTransactionBuilder(int index) {
				return getTransactionFieldBuilder().addBuilder(index,
						cros.mail.chain.wallet.Protos.Transaction.getDefaultInstance());
			}

			public java.util.List<cros.mail.chain.wallet.Protos.Transaction.Builder> getTransactionBuilderList() {
				return getTransactionFieldBuilder().getBuilderList();
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Transaction, cros.mail.chain.wallet.Protos.Transaction.Builder, cros.mail.chain.wallet.Protos.TransactionOrBuilder> getTransactionFieldBuilder() {
				if (transactionBuilder_ == null) {
					transactionBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Transaction, cros.mail.chain.wallet.Protos.Transaction.Builder, cros.mail.chain.wallet.Protos.TransactionOrBuilder>(
							transaction_, ((bitField0_ & 0x00000020) == 0x00000020), getParentForChildren(), isClean());
					transaction_ = null;
				}
				return transactionBuilder_;
			}

			private java.util.List<cros.mail.chain.wallet.Protos.Script> watchedScript_ = java.util.Collections.emptyList();

			private void ensureWatchedScriptIsMutable() {
				if (!((bitField0_ & 0x00000040) == 0x00000040)) {
					watchedScript_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.Script>(watchedScript_);
					bitField0_ |= 0x00000040;
				}
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Script, cros.mail.chain.wallet.Protos.Script.Builder, cros.mail.chain.wallet.Protos.ScriptOrBuilder> watchedScriptBuilder_;

			public java.util.List<cros.mail.chain.wallet.Protos.Script> getWatchedScriptList() {
				if (watchedScriptBuilder_ == null) {
					return java.util.Collections.unmodifiableList(watchedScript_);
				} else {
					return watchedScriptBuilder_.getMessageList();
				}
			}

			public int getWatchedScriptCount() {
				if (watchedScriptBuilder_ == null) {
					return watchedScript_.size();
				} else {
					return watchedScriptBuilder_.getCount();
				}
			}

			public cros.mail.chain.wallet.Protos.Script getWatchedScript(int index) {
				if (watchedScriptBuilder_ == null) {
					return watchedScript_.get(index);
				} else {
					return watchedScriptBuilder_.getMessage(index);
				}
			}

			public Builder setWatchedScript(int index, cros.mail.chain.wallet.Protos.Script value) {
				if (watchedScriptBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureWatchedScriptIsMutable();
					watchedScript_.set(index, value);
					onChanged();
				} else {
					watchedScriptBuilder_.setMessage(index, value);
				}
				return this;
			}

			public Builder setWatchedScript(int index, cros.mail.chain.wallet.Protos.Script.Builder builderForValue) {
				if (watchedScriptBuilder_ == null) {
					ensureWatchedScriptIsMutable();
					watchedScript_.set(index, builderForValue.build());
					onChanged();
				} else {
					watchedScriptBuilder_.setMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addWatchedScript(cros.mail.chain.wallet.Protos.Script value) {
				if (watchedScriptBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureWatchedScriptIsMutable();
					watchedScript_.add(value);
					onChanged();
				} else {
					watchedScriptBuilder_.addMessage(value);
				}
				return this;
			}

			public Builder addWatchedScript(int index, cros.mail.chain.wallet.Protos.Script value) {
				if (watchedScriptBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureWatchedScriptIsMutable();
					watchedScript_.add(index, value);
					onChanged();
				} else {
					watchedScriptBuilder_.addMessage(index, value);
				}
				return this;
			}

			public Builder addWatchedScript(cros.mail.chain.wallet.Protos.Script.Builder builderForValue) {
				if (watchedScriptBuilder_ == null) {
					ensureWatchedScriptIsMutable();
					watchedScript_.add(builderForValue.build());
					onChanged();
				} else {
					watchedScriptBuilder_.addMessage(builderForValue.build());
				}
				return this;
			}

			public Builder addWatchedScript(int index, cros.mail.chain.wallet.Protos.Script.Builder builderForValue) {
				if (watchedScriptBuilder_ == null) {
					ensureWatchedScriptIsMutable();
					watchedScript_.add(index, builderForValue.build());
					onChanged();
				} else {
					watchedScriptBuilder_.addMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addAllWatchedScript(java.lang.Iterable<? extends cros.mail.chain.wallet.Protos.Script> values) {
				if (watchedScriptBuilder_ == null) {
					ensureWatchedScriptIsMutable();
					com.google.protobuf.AbstractMessageLite.Builder.addAll(values, watchedScript_);
					onChanged();
				} else {
					watchedScriptBuilder_.addAllMessages(values);
				}
				return this;
			}

			public Builder clearWatchedScript() {
				if (watchedScriptBuilder_ == null) {
					watchedScript_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000040);
					onChanged();
				} else {
					watchedScriptBuilder_.clear();
				}
				return this;
			}

			public Builder removeWatchedScript(int index) {
				if (watchedScriptBuilder_ == null) {
					ensureWatchedScriptIsMutable();
					watchedScript_.remove(index);
					onChanged();
				} else {
					watchedScriptBuilder_.remove(index);
				}
				return this;
			}

			public cros.mail.chain.wallet.Protos.Script.Builder getWatchedScriptBuilder(int index) {
				return getWatchedScriptFieldBuilder().getBuilder(index);
			}

			public cros.mail.chain.wallet.Protos.ScriptOrBuilder getWatchedScriptOrBuilder(int index) {
				if (watchedScriptBuilder_ == null) {
					return watchedScript_.get(index);
				} else {
					return watchedScriptBuilder_.getMessageOrBuilder(index);
				}
			}

			public java.util.List<? extends cros.mail.chain.wallet.Protos.ScriptOrBuilder> getWatchedScriptOrBuilderList() {
				if (watchedScriptBuilder_ != null) {
					return watchedScriptBuilder_.getMessageOrBuilderList();
				} else {
					return java.util.Collections.unmodifiableList(watchedScript_);
				}
			}

			public cros.mail.chain.wallet.Protos.Script.Builder addWatchedScriptBuilder() {
				return getWatchedScriptFieldBuilder().addBuilder(cros.mail.chain.wallet.Protos.Script.getDefaultInstance());
			}

			public cros.mail.chain.wallet.Protos.Script.Builder addWatchedScriptBuilder(int index) {
				return getWatchedScriptFieldBuilder().addBuilder(index,
						cros.mail.chain.wallet.Protos.Script.getDefaultInstance());
			}

			public java.util.List<cros.mail.chain.wallet.Protos.Script.Builder> getWatchedScriptBuilderList() {
				return getWatchedScriptFieldBuilder().getBuilderList();
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Script, cros.mail.chain.wallet.Protos.Script.Builder, cros.mail.chain.wallet.Protos.ScriptOrBuilder> getWatchedScriptFieldBuilder() {
				if (watchedScriptBuilder_ == null) {
					watchedScriptBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Script, cros.mail.chain.wallet.Protos.Script.Builder, cros.mail.chain.wallet.Protos.ScriptOrBuilder>(
							watchedScript_, ((bitField0_ & 0x00000040) == 0x00000040), getParentForChildren(),
							isClean());
					watchedScript_ = null;
				}
				return watchedScriptBuilder_;
			}

			private cros.mail.chain.wallet.Protos.Wallet.EncryptionType encryptionType_ = cros.mail.chain.wallet.Protos.Wallet.EncryptionType.UNENCRYPTED;

			public boolean hasEncryptionType() {
				return ((bitField0_ & 0x00000080) == 0x00000080);
			}

			public cros.mail.chain.wallet.Protos.Wallet.EncryptionType getEncryptionType() {
				return encryptionType_;
			}

			public Builder setEncryptionType(cros.mail.chain.wallet.Protos.Wallet.EncryptionType value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000080;
				encryptionType_ = value;
				onChanged();
				return this;
			}

			public Builder clearEncryptionType() {
				bitField0_ = (bitField0_ & ~0x00000080);
				encryptionType_ = cros.mail.chain.wallet.Protos.Wallet.EncryptionType.UNENCRYPTED;
				onChanged();
				return this;
			}

			private cros.mail.chain.wallet.Protos.ScryptParameters encryptionParameters_ = cros.mail.chain.wallet.Protos.ScryptParameters
					.getDefaultInstance();
			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.ScryptParameters, cros.mail.chain.wallet.Protos.ScryptParameters.Builder, cros.mail.chain.wallet.Protos.ScryptParametersOrBuilder> encryptionParametersBuilder_;

			public boolean hasEncryptionParameters() {
				return ((bitField0_ & 0x00000100) == 0x00000100);
			}

			public cros.mail.chain.wallet.Protos.ScryptParameters getEncryptionParameters() {
				if (encryptionParametersBuilder_ == null) {
					return encryptionParameters_;
				} else {
					return encryptionParametersBuilder_.getMessage();
				}
			}

			public Builder setEncryptionParameters(cros.mail.chain.wallet.Protos.ScryptParameters value) {
				if (encryptionParametersBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					encryptionParameters_ = value;
					onChanged();
				} else {
					encryptionParametersBuilder_.setMessage(value);
				}
				bitField0_ |= 0x00000100;
				return this;
			}

			public Builder setEncryptionParameters(cros.mail.chain.wallet.Protos.ScryptParameters.Builder builderForValue) {
				if (encryptionParametersBuilder_ == null) {
					encryptionParameters_ = builderForValue.build();
					onChanged();
				} else {
					encryptionParametersBuilder_.setMessage(builderForValue.build());
				}
				bitField0_ |= 0x00000100;
				return this;
			}

			public Builder mergeEncryptionParameters(cros.mail.chain.wallet.Protos.ScryptParameters value) {
				if (encryptionParametersBuilder_ == null) {
					if (((bitField0_ & 0x00000100) == 0x00000100)
							&& encryptionParameters_ != cros.mail.chain.wallet.Protos.ScryptParameters.getDefaultInstance()) {
						encryptionParameters_ = cros.mail.chain.wallet.Protos.ScryptParameters
								.newBuilder(encryptionParameters_).mergeFrom(value).buildPartial();
					} else {
						encryptionParameters_ = value;
					}
					onChanged();
				} else {
					encryptionParametersBuilder_.mergeFrom(value);
				}
				bitField0_ |= 0x00000100;
				return this;
			}

			public Builder clearEncryptionParameters() {
				if (encryptionParametersBuilder_ == null) {
					encryptionParameters_ = cros.mail.chain.wallet.Protos.ScryptParameters.getDefaultInstance();
					onChanged();
				} else {
					encryptionParametersBuilder_.clear();
				}
				bitField0_ = (bitField0_ & ~0x00000100);
				return this;
			}

			public cros.mail.chain.wallet.Protos.ScryptParameters.Builder getEncryptionParametersBuilder() {
				bitField0_ |= 0x00000100;
				onChanged();
				return getEncryptionParametersFieldBuilder().getBuilder();
			}

			public cros.mail.chain.wallet.Protos.ScryptParametersOrBuilder getEncryptionParametersOrBuilder() {
				if (encryptionParametersBuilder_ != null) {
					return encryptionParametersBuilder_.getMessageOrBuilder();
				} else {
					return encryptionParameters_;
				}
			}

			private com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.ScryptParameters, cros.mail.chain.wallet.Protos.ScryptParameters.Builder, cros.mail.chain.wallet.Protos.ScryptParametersOrBuilder> getEncryptionParametersFieldBuilder() {
				if (encryptionParametersBuilder_ == null) {
					encryptionParametersBuilder_ = new com.google.protobuf.SingleFieldBuilder<cros.mail.chain.wallet.Protos.ScryptParameters, cros.mail.chain.wallet.Protos.ScryptParameters.Builder, cros.mail.chain.wallet.Protos.ScryptParametersOrBuilder>(
							getEncryptionParameters(), getParentForChildren(), isClean());
					encryptionParameters_ = null;
				}
				return encryptionParametersBuilder_;
			}

			private int version_ = 1;

			public boolean hasVersion() {
				return ((bitField0_ & 0x00000200) == 0x00000200);
			}

			public int getVersion() {
				return version_;
			}

			public Builder setVersion(int value) {
				bitField0_ |= 0x00000200;
				version_ = value;
				onChanged();
				return this;
			}

			public Builder clearVersion() {
				bitField0_ = (bitField0_ & ~0x00000200);
				version_ = 1;
				onChanged();
				return this;
			}

			private java.util.List<cros.mail.chain.wallet.Protos.Extension> extension_ = java.util.Collections.emptyList();

			private void ensureExtensionIsMutable() {
				if (!((bitField0_ & 0x00000400) == 0x00000400)) {
					extension_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.Extension>(extension_);
					bitField0_ |= 0x00000400;
				}
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Extension, cros.mail.chain.wallet.Protos.Extension.Builder, cros.mail.chain.wallet.Protos.ExtensionOrBuilder> extensionBuilder_;

			public java.util.List<cros.mail.chain.wallet.Protos.Extension> getExtensionList() {
				if (extensionBuilder_ == null) {
					return java.util.Collections.unmodifiableList(extension_);
				} else {
					return extensionBuilder_.getMessageList();
				}
			}

			public int getExtensionCount() {
				if (extensionBuilder_ == null) {
					return extension_.size();
				} else {
					return extensionBuilder_.getCount();
				}
			}

			public cros.mail.chain.wallet.Protos.Extension getExtension(int index) {
				if (extensionBuilder_ == null) {
					return extension_.get(index);
				} else {
					return extensionBuilder_.getMessage(index);
				}
			}

			public Builder setExtension(int index, cros.mail.chain.wallet.Protos.Extension value) {
				if (extensionBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureExtensionIsMutable();
					extension_.set(index, value);
					onChanged();
				} else {
					extensionBuilder_.setMessage(index, value);
				}
				return this;
			}

			public Builder setExtension(int index, cros.mail.chain.wallet.Protos.Extension.Builder builderForValue) {
				if (extensionBuilder_ == null) {
					ensureExtensionIsMutable();
					extension_.set(index, builderForValue.build());
					onChanged();
				} else {
					extensionBuilder_.setMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addExtension(cros.mail.chain.wallet.Protos.Extension value) {
				if (extensionBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureExtensionIsMutable();
					extension_.add(value);
					onChanged();
				} else {
					extensionBuilder_.addMessage(value);
				}
				return this;
			}

			public Builder addExtension(int index, cros.mail.chain.wallet.Protos.Extension value) {
				if (extensionBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureExtensionIsMutable();
					extension_.add(index, value);
					onChanged();
				} else {
					extensionBuilder_.addMessage(index, value);
				}
				return this;
			}

			public Builder addExtension(cros.mail.chain.wallet.Protos.Extension.Builder builderForValue) {
				if (extensionBuilder_ == null) {
					ensureExtensionIsMutable();
					extension_.add(builderForValue.build());
					onChanged();
				} else {
					extensionBuilder_.addMessage(builderForValue.build());
				}
				return this;
			}

			public Builder addExtension(int index, cros.mail.chain.wallet.Protos.Extension.Builder builderForValue) {
				if (extensionBuilder_ == null) {
					ensureExtensionIsMutable();
					extension_.add(index, builderForValue.build());
					onChanged();
				} else {
					extensionBuilder_.addMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addAllExtension(java.lang.Iterable<? extends cros.mail.chain.wallet.Protos.Extension> values) {
				if (extensionBuilder_ == null) {
					ensureExtensionIsMutable();
					com.google.protobuf.AbstractMessageLite.Builder.addAll(values, extension_);
					onChanged();
				} else {
					extensionBuilder_.addAllMessages(values);
				}
				return this;
			}

			public Builder clearExtension() {
				if (extensionBuilder_ == null) {
					extension_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00000400);
					onChanged();
				} else {
					extensionBuilder_.clear();
				}
				return this;
			}

			public Builder removeExtension(int index) {
				if (extensionBuilder_ == null) {
					ensureExtensionIsMutable();
					extension_.remove(index);
					onChanged();
				} else {
					extensionBuilder_.remove(index);
				}
				return this;
			}

			public cros.mail.chain.wallet.Protos.Extension.Builder getExtensionBuilder(int index) {
				return getExtensionFieldBuilder().getBuilder(index);
			}

			public cros.mail.chain.wallet.Protos.ExtensionOrBuilder getExtensionOrBuilder(int index) {
				if (extensionBuilder_ == null) {
					return extension_.get(index);
				} else {
					return extensionBuilder_.getMessageOrBuilder(index);
				}
			}

			public java.util.List<? extends cros.mail.chain.wallet.Protos.ExtensionOrBuilder> getExtensionOrBuilderList() {
				if (extensionBuilder_ != null) {
					return extensionBuilder_.getMessageOrBuilderList();
				} else {
					return java.util.Collections.unmodifiableList(extension_);
				}
			}

			public cros.mail.chain.wallet.Protos.Extension.Builder addExtensionBuilder() {
				return getExtensionFieldBuilder().addBuilder(cros.mail.chain.wallet.Protos.Extension.getDefaultInstance());
			}

			public cros.mail.chain.wallet.Protos.Extension.Builder addExtensionBuilder(int index) {
				return getExtensionFieldBuilder().addBuilder(index,
						cros.mail.chain.wallet.Protos.Extension.getDefaultInstance());
			}

			public java.util.List<cros.mail.chain.wallet.Protos.Extension.Builder> getExtensionBuilderList() {
				return getExtensionFieldBuilder().getBuilderList();
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Extension, cros.mail.chain.wallet.Protos.Extension.Builder, cros.mail.chain.wallet.Protos.ExtensionOrBuilder> getExtensionFieldBuilder() {
				if (extensionBuilder_ == null) {
					extensionBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Extension, cros.mail.chain.wallet.Protos.Extension.Builder, cros.mail.chain.wallet.Protos.ExtensionOrBuilder>(
							extension_, ((bitField0_ & 0x00000400) == 0x00000400), getParentForChildren(), isClean());
					extension_ = null;
				}
				return extensionBuilder_;
			}

			private java.lang.Object description_ = "";

			public boolean hasDescription() {
				return ((bitField0_ & 0x00000800) == 0x00000800);
			}

			public java.lang.String getDescription() {
				java.lang.Object ref = description_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						description_ = s;
					}
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}

			public com.google.protobuf.ByteString getDescriptionBytes() {
				java.lang.Object ref = description_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					description_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			public Builder setDescription(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000800;
				description_ = value;
				onChanged();
				return this;
			}

			public Builder clearDescription() {
				bitField0_ = (bitField0_ & ~0x00000800);
				description_ = getDefaultInstance().getDescription();
				onChanged();
				return this;
			}

			public Builder setDescriptionBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000800;
				description_ = value;
				onChanged();
				return this;
			}

			private long keyRotationTime_;

			public boolean hasKeyRotationTime() {
				return ((bitField0_ & 0x00001000) == 0x00001000);
			}

			public long getKeyRotationTime() {
				return keyRotationTime_;
			}

			public Builder setKeyRotationTime(long value) {
				bitField0_ |= 0x00001000;
				keyRotationTime_ = value;
				onChanged();
				return this;
			}

			public Builder clearKeyRotationTime() {
				bitField0_ = (bitField0_ & ~0x00001000);
				keyRotationTime_ = 0L;
				onChanged();
				return this;
			}

			private java.util.List<cros.mail.chain.wallet.Protos.Tag> tags_ = java.util.Collections.emptyList();

			private void ensureTagsIsMutable() {
				if (!((bitField0_ & 0x00002000) == 0x00002000)) {
					tags_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.Tag>(tags_);
					bitField0_ |= 0x00002000;
				}
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Tag, cros.mail.chain.wallet.Protos.Tag.Builder, cros.mail.chain.wallet.Protos.TagOrBuilder> tagsBuilder_;

			public java.util.List<cros.mail.chain.wallet.Protos.Tag> getTagsList() {
				if (tagsBuilder_ == null) {
					return java.util.Collections.unmodifiableList(tags_);
				} else {
					return tagsBuilder_.getMessageList();
				}
			}

			public int getTagsCount() {
				if (tagsBuilder_ == null) {
					return tags_.size();
				} else {
					return tagsBuilder_.getCount();
				}
			}

			public cros.mail.chain.wallet.Protos.Tag getTags(int index) {
				if (tagsBuilder_ == null) {
					return tags_.get(index);
				} else {
					return tagsBuilder_.getMessage(index);
				}
			}

			public Builder setTags(int index, cros.mail.chain.wallet.Protos.Tag value) {
				if (tagsBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTagsIsMutable();
					tags_.set(index, value);
					onChanged();
				} else {
					tagsBuilder_.setMessage(index, value);
				}
				return this;
			}

			public Builder setTags(int index, cros.mail.chain.wallet.Protos.Tag.Builder builderForValue) {
				if (tagsBuilder_ == null) {
					ensureTagsIsMutable();
					tags_.set(index, builderForValue.build());
					onChanged();
				} else {
					tagsBuilder_.setMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addTags(cros.mail.chain.wallet.Protos.Tag value) {
				if (tagsBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTagsIsMutable();
					tags_.add(value);
					onChanged();
				} else {
					tagsBuilder_.addMessage(value);
				}
				return this;
			}

			public Builder addTags(int index, cros.mail.chain.wallet.Protos.Tag value) {
				if (tagsBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTagsIsMutable();
					tags_.add(index, value);
					onChanged();
				} else {
					tagsBuilder_.addMessage(index, value);
				}
				return this;
			}

			public Builder addTags(cros.mail.chain.wallet.Protos.Tag.Builder builderForValue) {
				if (tagsBuilder_ == null) {
					ensureTagsIsMutable();
					tags_.add(builderForValue.build());
					onChanged();
				} else {
					tagsBuilder_.addMessage(builderForValue.build());
				}
				return this;
			}

			public Builder addTags(int index, cros.mail.chain.wallet.Protos.Tag.Builder builderForValue) {
				if (tagsBuilder_ == null) {
					ensureTagsIsMutable();
					tags_.add(index, builderForValue.build());
					onChanged();
				} else {
					tagsBuilder_.addMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addAllTags(java.lang.Iterable<? extends cros.mail.chain.wallet.Protos.Tag> values) {
				if (tagsBuilder_ == null) {
					ensureTagsIsMutable();
					com.google.protobuf.AbstractMessageLite.Builder.addAll(values, tags_);
					onChanged();
				} else {
					tagsBuilder_.addAllMessages(values);
				}
				return this;
			}

			public Builder clearTags() {
				if (tagsBuilder_ == null) {
					tags_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00002000);
					onChanged();
				} else {
					tagsBuilder_.clear();
				}
				return this;
			}

			public Builder removeTags(int index) {
				if (tagsBuilder_ == null) {
					ensureTagsIsMutable();
					tags_.remove(index);
					onChanged();
				} else {
					tagsBuilder_.remove(index);
				}
				return this;
			}

			public cros.mail.chain.wallet.Protos.Tag.Builder getTagsBuilder(int index) {
				return getTagsFieldBuilder().getBuilder(index);
			}

			public cros.mail.chain.wallet.Protos.TagOrBuilder getTagsOrBuilder(int index) {
				if (tagsBuilder_ == null) {
					return tags_.get(index);
				} else {
					return tagsBuilder_.getMessageOrBuilder(index);
				}
			}

			public java.util.List<? extends cros.mail.chain.wallet.Protos.TagOrBuilder> getTagsOrBuilderList() {
				if (tagsBuilder_ != null) {
					return tagsBuilder_.getMessageOrBuilderList();
				} else {
					return java.util.Collections.unmodifiableList(tags_);
				}
			}

			public cros.mail.chain.wallet.Protos.Tag.Builder addTagsBuilder() {
				return getTagsFieldBuilder().addBuilder(cros.mail.chain.wallet.Protos.Tag.getDefaultInstance());
			}

			public cros.mail.chain.wallet.Protos.Tag.Builder addTagsBuilder(int index) {
				return getTagsFieldBuilder().addBuilder(index, cros.mail.chain.wallet.Protos.Tag.getDefaultInstance());
			}

			public java.util.List<cros.mail.chain.wallet.Protos.Tag.Builder> getTagsBuilderList() {
				return getTagsFieldBuilder().getBuilderList();
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Tag, cros.mail.chain.wallet.Protos.Tag.Builder, cros.mail.chain.wallet.Protos.TagOrBuilder> getTagsFieldBuilder() {
				if (tagsBuilder_ == null) {
					tagsBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.Tag, cros.mail.chain.wallet.Protos.Tag.Builder, cros.mail.chain.wallet.Protos.TagOrBuilder>(
							tags_, ((bitField0_ & 0x00002000) == 0x00002000), getParentForChildren(), isClean());
					tags_ = null;
				}
				return tagsBuilder_;
			}

			private java.util.List<cros.mail.chain.wallet.Protos.TransactionSigner> transactionSigners_ = java.util.Collections
					.emptyList();

			private void ensureTransactionSignersIsMutable() {
				if (!((bitField0_ & 0x00004000) == 0x00004000)) {
					transactionSigners_ = new java.util.ArrayList<cros.mail.chain.wallet.Protos.TransactionSigner>(
							transactionSigners_);
					bitField0_ |= 0x00004000;
				}
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.TransactionSigner, cros.mail.chain.wallet.Protos.TransactionSigner.Builder, cros.mail.chain.wallet.Protos.TransactionSignerOrBuilder> transactionSignersBuilder_;

			public java.util.List<cros.mail.chain.wallet.Protos.TransactionSigner> getTransactionSignersList() {
				if (transactionSignersBuilder_ == null) {
					return java.util.Collections.unmodifiableList(transactionSigners_);
				} else {
					return transactionSignersBuilder_.getMessageList();
				}
			}

			public int getTransactionSignersCount() {
				if (transactionSignersBuilder_ == null) {
					return transactionSigners_.size();
				} else {
					return transactionSignersBuilder_.getCount();
				}
			}

			public cros.mail.chain.wallet.Protos.TransactionSigner getTransactionSigners(int index) {
				if (transactionSignersBuilder_ == null) {
					return transactionSigners_.get(index);
				} else {
					return transactionSignersBuilder_.getMessage(index);
				}
			}

			public Builder setTransactionSigners(int index, cros.mail.chain.wallet.Protos.TransactionSigner value) {
				if (transactionSignersBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionSignersIsMutable();
					transactionSigners_.set(index, value);
					onChanged();
				} else {
					transactionSignersBuilder_.setMessage(index, value);
				}
				return this;
			}

			public Builder setTransactionSigners(int index,
					cros.mail.chain.wallet.Protos.TransactionSigner.Builder builderForValue) {
				if (transactionSignersBuilder_ == null) {
					ensureTransactionSignersIsMutable();
					transactionSigners_.set(index, builderForValue.build());
					onChanged();
				} else {
					transactionSignersBuilder_.setMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addTransactionSigners(cros.mail.chain.wallet.Protos.TransactionSigner value) {
				if (transactionSignersBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionSignersIsMutable();
					transactionSigners_.add(value);
					onChanged();
				} else {
					transactionSignersBuilder_.addMessage(value);
				}
				return this;
			}

			public Builder addTransactionSigners(int index, cros.mail.chain.wallet.Protos.TransactionSigner value) {
				if (transactionSignersBuilder_ == null) {
					if (value == null) {
						throw new NullPointerException();
					}
					ensureTransactionSignersIsMutable();
					transactionSigners_.add(index, value);
					onChanged();
				} else {
					transactionSignersBuilder_.addMessage(index, value);
				}
				return this;
			}

			public Builder addTransactionSigners(cros.mail.chain.wallet.Protos.TransactionSigner.Builder builderForValue) {
				if (transactionSignersBuilder_ == null) {
					ensureTransactionSignersIsMutable();
					transactionSigners_.add(builderForValue.build());
					onChanged();
				} else {
					transactionSignersBuilder_.addMessage(builderForValue.build());
				}
				return this;
			}

			public Builder addTransactionSigners(int index,
					cros.mail.chain.wallet.Protos.TransactionSigner.Builder builderForValue) {
				if (transactionSignersBuilder_ == null) {
					ensureTransactionSignersIsMutable();
					transactionSigners_.add(index, builderForValue.build());
					onChanged();
				} else {
					transactionSignersBuilder_.addMessage(index, builderForValue.build());
				}
				return this;
			}

			public Builder addAllTransactionSigners(
					java.lang.Iterable<? extends cros.mail.chain.wallet.Protos.TransactionSigner> values) {
				if (transactionSignersBuilder_ == null) {
					ensureTransactionSignersIsMutable();
					com.google.protobuf.AbstractMessageLite.Builder.addAll(values, transactionSigners_);
					onChanged();
				} else {
					transactionSignersBuilder_.addAllMessages(values);
				}
				return this;
			}

			public Builder clearTransactionSigners() {
				if (transactionSignersBuilder_ == null) {
					transactionSigners_ = java.util.Collections.emptyList();
					bitField0_ = (bitField0_ & ~0x00004000);
					onChanged();
				} else {
					transactionSignersBuilder_.clear();
				}
				return this;
			}

			public Builder removeTransactionSigners(int index) {
				if (transactionSignersBuilder_ == null) {
					ensureTransactionSignersIsMutable();
					transactionSigners_.remove(index);
					onChanged();
				} else {
					transactionSignersBuilder_.remove(index);
				}
				return this;
			}

			public cros.mail.chain.wallet.Protos.TransactionSigner.Builder getTransactionSignersBuilder(int index) {
				return getTransactionSignersFieldBuilder().getBuilder(index);
			}

			public cros.mail.chain.wallet.Protos.TransactionSignerOrBuilder getTransactionSignersOrBuilder(int index) {
				if (transactionSignersBuilder_ == null) {
					return transactionSigners_.get(index);
				} else {
					return transactionSignersBuilder_.getMessageOrBuilder(index);
				}
			}

			public java.util.List<? extends cros.mail.chain.wallet.Protos.TransactionSignerOrBuilder> getTransactionSignersOrBuilderList() {
				if (transactionSignersBuilder_ != null) {
					return transactionSignersBuilder_.getMessageOrBuilderList();
				} else {
					return java.util.Collections.unmodifiableList(transactionSigners_);
				}
			}

			public cros.mail.chain.wallet.Protos.TransactionSigner.Builder addTransactionSignersBuilder() {
				return getTransactionSignersFieldBuilder()
						.addBuilder(cros.mail.chain.wallet.Protos.TransactionSigner.getDefaultInstance());
			}

			public cros.mail.chain.wallet.Protos.TransactionSigner.Builder addTransactionSignersBuilder(int index) {
				return getTransactionSignersFieldBuilder().addBuilder(index,
						cros.mail.chain.wallet.Protos.TransactionSigner.getDefaultInstance());
			}

			public java.util.List<cros.mail.chain.wallet.Protos.TransactionSigner.Builder> getTransactionSignersBuilderList() {
				return getTransactionSignersFieldBuilder().getBuilderList();
			}

			private com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.TransactionSigner, cros.mail.chain.wallet.Protos.TransactionSigner.Builder, cros.mail.chain.wallet.Protos.TransactionSignerOrBuilder> getTransactionSignersFieldBuilder() {
				if (transactionSignersBuilder_ == null) {
					transactionSignersBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<cros.mail.chain.wallet.Protos.TransactionSigner, cros.mail.chain.wallet.Protos.TransactionSigner.Builder, cros.mail.chain.wallet.Protos.TransactionSignerOrBuilder>(
							transactionSigners_, ((bitField0_ & 0x00004000) == 0x00004000), getParentForChildren(),
							isClean());
					transactionSigners_ = null;
				}
				return transactionSignersBuilder_;
			}

		}

		static {
			defaultInstance = new Wallet(true);
			defaultInstance.initFields();
		}

	}

	public interface ExchangeRateOrBuilder extends

			com.google.protobuf.MessageOrBuilder {

		boolean hasCoinValue();

		long getCoinValue();

		boolean hasFiatValue();

		long getFiatValue();

		boolean hasFiatCurrencyCode();

		java.lang.String getFiatCurrencyCode();

		com.google.protobuf.ByteString getFiatCurrencyCodeBytes();
	}

	public static final class ExchangeRate extends com.google.protobuf.GeneratedMessage implements

			ExchangeRateOrBuilder {

		private ExchangeRate(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private ExchangeRate(boolean noInit) {
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final ExchangeRate defaultInstance;

		public static ExchangeRate getDefaultInstance() {
			return defaultInstance;
		}

		public ExchangeRate getDefaultInstanceForType() {
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}

		private ExchangeRate(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet
					.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
					case 0:
						done = true;
						break;
					default: {
						if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
							done = true;
						}
						break;
					}
					case 8: {
						bitField0_ |= 0x00000001;
						coinValue_ = input.readInt64();
						break;
					}
					case 16: {
						bitField0_ |= 0x00000002;
						fiatValue_ = input.readInt64();
						break;
					}
					case 26: {
						com.google.protobuf.ByteString bs = input.readBytes();
						bitField0_ |= 0x00000004;
						fiatCurrencyCode_ = bs;
						break;
					}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_ExchangeRate_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
			return cros.mail.chain.wallet.Protos.internal_static_wallet_ExchangeRate_fieldAccessorTable
					.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.ExchangeRate.class,
							cros.mail.chain.wallet.Protos.ExchangeRate.Builder.class);
		}

		public static com.google.protobuf.Parser<ExchangeRate> PARSER = new com.google.protobuf.AbstractParser<ExchangeRate>() {
			public ExchangeRate parsePartialFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException {
				return new ExchangeRate(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<ExchangeRate> getParserForType() {
			return PARSER;
		}

		private int bitField0_;
		public static final int COIN_VALUE_FIELD_NUMBER = 1;
		private long coinValue_;

		public boolean hasCoinValue() {
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		public long getCoinValue() {
			return coinValue_;
		}

		public static final int FIAT_VALUE_FIELD_NUMBER = 2;
		private long fiatValue_;

		public boolean hasFiatValue() {
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		public long getFiatValue() {
			return fiatValue_;
		}

		public static final int FIAT_CURRENCY_CODE_FIELD_NUMBER = 3;
		private java.lang.Object fiatCurrencyCode_;

		public boolean hasFiatCurrencyCode() {
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		public java.lang.String getFiatCurrencyCode() {
			java.lang.Object ref = fiatCurrencyCode_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8()) {
					fiatCurrencyCode_ = s;
				}
				return s;
			}
		}

		public com.google.protobuf.ByteString getFiatCurrencyCodeBytes() {
			java.lang.Object ref = fiatCurrencyCode_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				fiatCurrencyCode_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}

		private void initFields() {
			coinValue_ = 0L;
			fiatValue_ = 0L;
			fiatCurrencyCode_ = "";
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;

			if (!hasCoinValue()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasFiatValue()) {
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasFiatCurrencyCode()) {
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				output.writeInt64(1, coinValue_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				output.writeInt64(2, fiatValue_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				output.writeBytes(3, getFiatCurrencyCodeBytes());
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize() {
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001)) {
				size += com.google.protobuf.CodedOutputStream.computeInt64Size(1, coinValue_);
			}
			if (((bitField0_ & 0x00000002) == 0x00000002)) {
				size += com.google.protobuf.CodedOutputStream.computeInt64Size(2, fiatValue_);
			}
			if (((bitField0_ & 0x00000004) == 0x00000004)) {
				size += com.google.protobuf.CodedOutputStream.computeBytesSize(3, getFiatCurrencyCodeBytes());
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
			return super.writeReplace();
		}

		public static cros.mail.chain.wallet.Protos.ExchangeRate parseFrom(com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.ExchangeRate parseFrom(com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.ExchangeRate parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}

		public static cros.mail.chain.wallet.Protos.ExchangeRate parseFrom(byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.ExchangeRate parseFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.ExchangeRate parseFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.ExchangeRate parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.ExchangeRate parseDelimitedFrom(java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static cros.mail.chain.wallet.Protos.ExchangeRate parseFrom(com.google.protobuf.CodedInputStream input)
				throws java.io.IOException {
			return PARSER.parseFrom(input);
		}

		public static cros.mail.chain.wallet.Protos.ExchangeRate parseFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder() {
			return Builder.create();
		}

		public Builder newBuilderForType() {
			return newBuilder();
		}

		public static Builder newBuilder(cros.mail.chain.wallet.Protos.ExchangeRate prototype) {
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder() {
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}

		public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements

				cros.mail.chain.wallet.Protos.ExchangeRateOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_ExchangeRate_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_ExchangeRate_fieldAccessorTable
						.ensureFieldAccessorsInitialized(cros.mail.chain.wallet.Protos.ExchangeRate.class,
								cros.mail.chain.wallet.Protos.ExchangeRate.Builder.class);
			}

			private Builder() {
				maybeForceBuilderInitialization();
			}

			private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
				}
			}

			private static Builder create() {
				return new Builder();
			}

			public Builder clear() {
				super.clear();
				coinValue_ = 0L;
				bitField0_ = (bitField0_ & ~0x00000001);
				fiatValue_ = 0L;
				bitField0_ = (bitField0_ & ~0x00000002);
				fiatCurrencyCode_ = "";
				bitField0_ = (bitField0_ & ~0x00000004);
				return this;
			}

			public Builder clone() {
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return cros.mail.chain.wallet.Protos.internal_static_wallet_ExchangeRate_descriptor;
			}

			public cros.mail.chain.wallet.Protos.ExchangeRate getDefaultInstanceForType() {
				return cros.mail.chain.wallet.Protos.ExchangeRate.getDefaultInstance();
			}

			public cros.mail.chain.wallet.Protos.ExchangeRate build() {
				cros.mail.chain.wallet.Protos.ExchangeRate result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public cros.mail.chain.wallet.Protos.ExchangeRate buildPartial() {
				cros.mail.chain.wallet.Protos.ExchangeRate result = new cros.mail.chain.wallet.Protos.ExchangeRate(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
					to_bitField0_ |= 0x00000001;
				}
				result.coinValue_ = coinValue_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
					to_bitField0_ |= 0x00000002;
				}
				result.fiatValue_ = fiatValue_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
					to_bitField0_ |= 0x00000004;
				}
				result.fiatCurrencyCode_ = fiatCurrencyCode_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof cros.mail.chain.wallet.Protos.ExchangeRate) {
					return mergeFrom((cros.mail.chain.wallet.Protos.ExchangeRate) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(cros.mail.chain.wallet.Protos.ExchangeRate other) {
				if (other == cros.mail.chain.wallet.Protos.ExchangeRate.getDefaultInstance())
					return this;
				if (other.hasCoinValue()) {
					setCoinValue(other.getCoinValue());
				}
				if (other.hasFiatValue()) {
					setFiatValue(other.getFiatValue());
				}
				if (other.hasFiatCurrencyCode()) {
					bitField0_ |= 0x00000004;
					fiatCurrencyCode_ = other.fiatCurrencyCode_;
					onChanged();
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized() {
				if (!hasCoinValue()) {

					return false;
				}
				if (!hasFiatValue()) {

					return false;
				}
				if (!hasFiatCurrencyCode()) {

					return false;
				}
				return true;
			}

			public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				cros.mail.chain.wallet.Protos.ExchangeRate parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (cros.mail.chain.wallet.Protos.ExchangeRate) e.getUnfinishedMessage();
					throw e;
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			private long coinValue_;

			public boolean hasCoinValue() {
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			public long getCoinValue() {
				return coinValue_;
			}

			public Builder setCoinValue(long value) {
				bitField0_ |= 0x00000001;
				coinValue_ = value;
				onChanged();
				return this;
			}

			public Builder clearCoinValue() {
				bitField0_ = (bitField0_ & ~0x00000001);
				coinValue_ = 0L;
				onChanged();
				return this;
			}

			private long fiatValue_;

			public boolean hasFiatValue() {
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			public long getFiatValue() {
				return fiatValue_;
			}

			public Builder setFiatValue(long value) {
				bitField0_ |= 0x00000002;
				fiatValue_ = value;
				onChanged();
				return this;
			}

			public Builder clearFiatValue() {
				bitField0_ = (bitField0_ & ~0x00000002);
				fiatValue_ = 0L;
				onChanged();
				return this;
			}

			private java.lang.Object fiatCurrencyCode_ = "";

			public boolean hasFiatCurrencyCode() {
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			public java.lang.String getFiatCurrencyCode() {
				java.lang.Object ref = fiatCurrencyCode_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					if (bs.isValidUtf8()) {
						fiatCurrencyCode_ = s;
					}
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}

			public com.google.protobuf.ByteString getFiatCurrencyCodeBytes() {
				java.lang.Object ref = fiatCurrencyCode_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString
							.copyFromUtf8((java.lang.String) ref);
					fiatCurrencyCode_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}

			public Builder setFiatCurrencyCode(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000004;
				fiatCurrencyCode_ = value;
				onChanged();
				return this;
			}

			public Builder clearFiatCurrencyCode() {
				bitField0_ = (bitField0_ & ~0x00000004);
				fiatCurrencyCode_ = getDefaultInstance().getFiatCurrencyCode();
				onChanged();
				return this;
			}

			public Builder setFiatCurrencyCodeBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000004;
				fiatCurrencyCode_ = value;
				onChanged();
				return this;
			}

		}

		static {
			defaultInstance = new ExchangeRate(true);
			defaultInstance.initFields();
		}

	}

	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_PeerAddress_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_PeerAddress_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_EncryptedData_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_EncryptedData_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_DeterministicKey_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_DeterministicKey_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_Key_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_Key_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_Script_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_Script_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_TransactionInput_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_TransactionInput_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_TransactionOutput_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_TransactionOutput_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_TransactionConfidence_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_TransactionConfidence_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_Transaction_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_Transaction_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_ScryptParameters_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_ScryptParameters_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_Extension_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_Extension_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_Tag_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_Tag_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_TransactionSigner_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_TransactionSigner_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_Wallet_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_Wallet_fieldAccessorTable;
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_wallet_ExchangeRate_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_wallet_ExchangeRate_fieldAccessorTable;

	public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
		return descriptor;
	}

	private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
	static {
		java.lang.String[] descriptorData = {
				"\n\014wallet.proto\022\006wallet\"A\n\013PeerAddress\022\022\n"
						+ "\nip_address\030\001 \002(\014\022\014\n\004port\030\002 \002(\r\022\020\n\010servi"
						+ "ces\030\003 \002(\004\"M\n\rEncryptedData\022\035\n\025initialisa"
						+ "tion_vector\030\001 \002(\014\022\035\n\025encrypted_private_k"
						+ "ey\030\002 \002(\014\"\231\001\n\020DeterministicKey\022\022\n\nchain_c"
						+ "ode\030\001 \002(\014\022\014\n\004path\030\002 \003(\r\022\026\n\016issued_subkey"
						+ "s\030\003 \001(\r\022\026\n\016lookahead_size\030\004 \001(\r\022\023\n\013isFol"
						+ "lowing\030\005 \001(\010\022\036\n\023sigsRequiredToSpend\030\006 \001("
						+ "\r:\0011\"\232\003\n\003Key\022\036\n\004type\030\001 \002(\0162\020.wallet.Key."
						+ "Type\022\024\n\014secret_bytes\030\002 \001(\014\022-\n\016encrypted_",
				"data\030\006 \001(\0132\025.wallet.EncryptedData\022\022\n\npub"
						+ "lic_key\030\003 \001(\014\022\r\n\005label\030\004 \001(\t\022\032\n\022creation"
						+ "_timestamp\030\005 \001(\003\0223\n\021deterministic_key\030\007 "
						+ "\001(\0132\030.wallet.DeterministicKey\022\032\n\022determi"
						+ "nistic_seed\030\010 \001(\014\022;\n\034encrypted_determini"
						+ "stic_seed\030\t \001(\0132\025.wallet.EncryptedData\"a"
						+ "\n\004Type\022\014\n\010ORIGINAL\020\001\022\030\n\024ENCRYPTED_SCRYPT"
						+ "_AES\020\002\022\032\n\026DETERMINISTIC_MNEMONIC\020\003\022\025\n\021DE"
						+ "TERMINISTIC_KEY\020\004\"5\n\006Script\022\017\n\007program\030\001"
						+ " \002(\014\022\032\n\022creation_timestamp\030\002 \002(\003\"\222\001\n\020Tra",
				"nsactionInput\022\"\n\032transaction_out_point_h"
						+ "ash\030\001 \002(\014\022#\n\033transaction_out_point_index"
						+ "\030\002 \002(\r\022\024\n\014script_bytes\030\003 \002(\014\022\020\n\010sequence"
						+ "\030\004 \001(\r\022\r\n\005value\030\005 \001(\003\"\177\n\021TransactionOutp"
						+ "ut\022\r\n\005value\030\001 \002(\003\022\024\n\014script_bytes\030\002 \002(\014\022"
						+ "!\n\031spent_by_transaction_hash\030\003 \001(\014\022\"\n\032sp"
						+ "ent_by_transaction_index\030\004 \001(\005\"\211\003\n\025Trans"
						+ "actionConfidence\0220\n\004type\030\001 \001(\0162\".wallet."
						+ "TransactionDegree.Type\022\032\n\022appeared_a"
						+ "t_height\030\002 \001(\005\022\036\n\026overriding_transaction",
				"\030\003 \001(\014\022\r\n\005depth\030\004 \001(\005\022)\n\014broadcast_by\030\006 "
						+ "\003(\0132\023.wallet.PeerAddress\0224\n\006source\030\007 \001(\016"
						+ "2$.wallet.TransactionConfidence.Source\"O"
						+ "\n\004Type\022\013\n\007UNKNOWN\020\000\022\014\n\010BUILDING\020\001\022\013\n\007PEN"
						+ "DING\020\002\022\025\n\021NOT_IN_BEST_CHAIN\020\003\022\010\n\004DEAD\020\004\""
						+ "A\n\006Source\022\022\n\016SOURCE_UNKNOWN\020\000\022\022\n\016SOURCE_"
						+ "NETWORK\020\001\022\017\n\013SOURCE_SELF\020\002\"\303\005\n\013Transacti"
						+ "on\022\017\n\007version\030\001 \002(\005\022\014\n\004hash\030\002 \002(\014\022&\n\004poo"
						+ "l\030\003 \001(\0162\030.wallet.Transaction.Pool\022\021\n\tloc"
						+ "k_time\030\004 \001(\r\022\022\n\nupdated_at\030\005 \001(\003\0223\n\021tran",
				"saction_input\030\006 \003(\0132\030.wallet.Transaction"
						+ "Input\0225\n\022transaction_output\030\007 \003(\0132\031.wall"
						+ "et.TransactionOutput\022\022\n\nblock_hash\030\010 \003(\014"
						+ "\022 \n\030block_relativity_offsets\030\013 \003(\005\0221\n\nco"
						+ "nfidence\030\t \001(\0132\035.wallet.TransactionConfi"
						+ "dence\0225\n\007purpose\030\n \001(\0162\033.wallet.Transact"
						+ "ion.Purpose:\007UNKNOWN\022+\n\rexchange_rate\030\014 "
						+ "\001(\0132\024.wallet.ExchangeRate\022\014\n\004memo\030\r \001(\t\""
						+ "Y\n\004Pool\022\013\n\007UNSPENT\020\004\022\t\n\005SPENT\020\005\022\014\n\010INACT"
						+ "IVE\020\002\022\010\n\004DEAD\020\n\022\013\n\007PENDING\020\020\022\024\n\020PENDING_",
				"INACTIVE\020\022\"\243\001\n\007Purpose\022\013\n\007UNKNOWN\020\000\022\020\n\014U"
						+ "SER_PAYMENT\020\001\022\020\n\014KEY_ROTATION\020\002\022\034\n\030ASSUR"
						+ "ANCE_CONTRACT_CLAIM\020\003\022\035\n\031ASSURANCE_CONTR"
						+ "ACT_PLEDGE\020\004\022\033\n\027ASSURANCE_CONTRACT_STUB\020"
						+ "\005\022\r\n\tRAISE_FEE\020\006\"N\n\020ScryptParameters\022\014\n\004"
						+ "salt\030\001 \002(\014\022\020\n\001n\030\002 \001(\003:\00516384\022\014\n\001r\030\003 \001(\005:"
						+ "\0018\022\014\n\001p\030\004 \001(\005:\0011\"8\n\tExtension\022\n\n\002id\030\001 \002("
						+ "\t\022\014\n\004data\030\002 \002(\014\022\021\n\tmandatory\030\003 \002(\010\" \n\003Ta"
						+ "g\022\013\n\003tag\030\001 \002(\t\022\014\n\004data\030\002 \002(\014\"5\n\021Transact"
						+ "ionSigner\022\022\n\nclass_name\030\001 \002(\t\022\014\n\004data\030\002 ",
				"\001(\014\"\351\004\n\006Wallet\022\032\n\022network_identifier\030\001 \002"
						+ "(\t\022\034\n\024last_seen_block_hash\030\002 \001(\014\022\036\n\026last"
						+ "_seen_block_height\030\014 \001(\r\022!\n\031last_seen_bl"
						+ "ock_time_secs\030\016 \001(\003\022\030\n\003key\030\003 \003(\0132\013.walle"
						+ "t.Key\022(\n\013transaction\030\004 \003(\0132\023.wallet.Tran"
						+ "saction\022&\n\016watched_script\030\017 \003(\0132\016.wallet"
						+ ".Script\022C\n\017encryption_type\030\005 \001(\0162\035.walle"
						+ "t.Wallet.EncryptionType:\013UNENCRYPTED\0227\n\025"
						+ "encryption_parameters\030\006 \001(\0132\030.wallet.Scr"
						+ "yptParameters\022\022\n\007version\030\007 \001(\005:\0011\022$\n\text",
				"ension\030\n \003(\0132\021.wallet.Extension\022\023\n\013descr"
						+ "iption\030\013 \001(\t\022\031\n\021key_rotation_time\030\r \001(\004\022"
						+ "\031\n\004tags\030\020 \003(\0132\013.wallet.Tag\0226\n\023transactio"
						+ "n_signers\030\021 \003(\0132\031.wallet.TransactionSign"
						+ "er\";\n\016EncryptionType\022\017\n\013UNENCRYPTED\020\001\022\030\n"
						+ "\024ENCRYPTED_SCRYPT_AES\020\002\"R\n\014ExchangeRate\022"
						+ "\022\n\ncoin_value\030\001 \002(\003\022\022\n\nfiat_value\030\002 \002(\003\022"
						+ "\032\n\022fiat_currency_code\030\003 \002(\tB\035\n\023org.bitco"
						+ "inj.walletB\006Protos" };
		com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner = new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
			public com.google.protobuf.ExtensionRegistry assignDescriptors(
					com.google.protobuf.Descriptors.FileDescriptor root) {
				descriptor = root;
				return null;
			}
		};
		com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData,
				new com.google.protobuf.Descriptors.FileDescriptor[] {}, assigner);
		internal_static_wallet_PeerAddress_descriptor = getDescriptor().getMessageTypes().get(0);
		internal_static_wallet_PeerAddress_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_PeerAddress_descriptor,
				new java.lang.String[] { "IpAddress", "Port", "Services", });
		internal_static_wallet_EncryptedData_descriptor = getDescriptor().getMessageTypes().get(1);
		internal_static_wallet_EncryptedData_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_EncryptedData_descriptor,
				new java.lang.String[] { "InitialisationVector", "EncryptedPrivateKey", });
		internal_static_wallet_DeterministicKey_descriptor = getDescriptor().getMessageTypes().get(2);
		internal_static_wallet_DeterministicKey_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_DeterministicKey_descriptor, new java.lang.String[] { "ChainCode", "Path",
						"IssuedSubkeys", "LookaheadSize", "IsFollowing", "SigsRequiredToSpend", });
		internal_static_wallet_Key_descriptor = getDescriptor().getMessageTypes().get(3);
		internal_static_wallet_Key_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_Key_descriptor,
				new java.lang.String[] { "Type", "SecretBytes", "EncryptData", "PublicKey", "Label",
						"CreationTimestamp", "DeterKey", "DeterSeed", "EncryptedDeterministicSeed", });
		internal_static_wallet_Script_descriptor = getDescriptor().getMessageTypes().get(4);
		internal_static_wallet_Script_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_Script_descriptor, new java.lang.String[] { "Program", "CreationTimestamp", });
		internal_static_wallet_TransactionInput_descriptor = getDescriptor().getMessageTypes().get(5);
		internal_static_wallet_TransactionInput_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_TransactionInput_descriptor, new java.lang.String[] { "TransactionOutPointHash",
						"TransactionOutPointIndex", "ScriptBytes", "Sequence", "Value", });
		internal_static_wallet_TransactionOutput_descriptor = getDescriptor().getMessageTypes().get(6);
		internal_static_wallet_TransactionOutput_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_TransactionOutput_descriptor, new java.lang.String[] { "Value", "ScriptBytes",
						"SpentByTransactionHash", "SpentByTransactionIndex", });
		internal_static_wallet_TransactionConfidence_descriptor = getDescriptor().getMessageTypes().get(7);
		internal_static_wallet_TransactionConfidence_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_TransactionConfidence_descriptor, new java.lang.String[] { "Type",
						"AppearedAtHeight", "OverridingTransaction", "Depth", "BroadcastBy", "Source", });
		internal_static_wallet_Transaction_descriptor = getDescriptor().getMessageTypes().get(8);
		internal_static_wallet_Transaction_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_Transaction_descriptor,
				new java.lang.String[] { "Version", "Hash", "Pool", "LockTime", "UpdatedAt", "TxInput", "TxOutput",
						"BlockHash", "BlockRelativityOffsets", "Confidence", "Purpose", "InterchangeRate", "Memo", });
		internal_static_wallet_ScryptParameters_descriptor = getDescriptor().getMessageTypes().get(9);
		internal_static_wallet_ScryptParameters_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_ScryptParameters_descriptor, new java.lang.String[] { "Salt", "N", "R", "P", });
		internal_static_wallet_Extension_descriptor = getDescriptor().getMessageTypes().get(10);
		internal_static_wallet_Extension_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_Extension_descriptor, new java.lang.String[] { "Id", "Data", "Mandatory", });
		internal_static_wallet_Tag_descriptor = getDescriptor().getMessageTypes().get(11);
		internal_static_wallet_Tag_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_Tag_descriptor, new java.lang.String[] { "Tag", "Data", });
		internal_static_wallet_TransactionSigner_descriptor = getDescriptor().getMessageTypes().get(12);
		internal_static_wallet_TransactionSigner_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_TransactionSigner_descriptor, new java.lang.String[] { "ClassName", "Data", });
		internal_static_wallet_Wallet_descriptor = getDescriptor().getMessageTypes().get(13);
		internal_static_wallet_Wallet_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_Wallet_descriptor,
				new java.lang.String[] { "NetworkIdentifier", "LastSeenBlockHash", "LastSeenBlockHeight",
						"LastSeenBlockTimeSecs", "Key", "Transaction", "WatchedScript", "EncryptionType",
						"EncryptionParameters", "Version", "Extension", "Description", "KeyRotationTime", "Tags",
						"TransactionSigners", });
		internal_static_wallet_ExchangeRate_descriptor = getDescriptor().getMessageTypes().get(14);
		internal_static_wallet_ExchangeRate_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
				internal_static_wallet_ExchangeRate_descriptor,
				new java.lang.String[] { "CoinValue", "FiatValue", "FiatCurrencyCode", });
	}

}
