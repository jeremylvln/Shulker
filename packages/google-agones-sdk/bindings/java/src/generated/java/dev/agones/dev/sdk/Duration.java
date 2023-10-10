// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/agones/v1/sdk.proto

package dev.agones.dev.sdk;

/**
 * <pre>
 * time duration, in seconds
 * </pre>
 *
 * Protobuf type {@code agones.dev.sdk.Duration}
 */
public final class Duration extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:agones.dev.sdk.Duration)
    DurationOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Duration.newBuilder() to construct.
  private Duration(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Duration() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Duration();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return dev.agones.dev.sdk.SdkProto.internal_static_agones_dev_sdk_Duration_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return dev.agones.dev.sdk.SdkProto.internal_static_agones_dev_sdk_Duration_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            dev.agones.dev.sdk.Duration.class, dev.agones.dev.sdk.Duration.Builder.class);
  }

  public static final int SECONDS_FIELD_NUMBER = 1;
  private long seconds_ = 0L;
  /**
   * <code>int64 seconds = 1 [json_name = "seconds"];</code>
   * @return The seconds.
   */
  @java.lang.Override
  public long getSeconds() {
    return seconds_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (seconds_ != 0L) {
      output.writeInt64(1, seconds_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (seconds_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(1, seconds_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof dev.agones.dev.sdk.Duration)) {
      return super.equals(obj);
    }
    dev.agones.dev.sdk.Duration other = (dev.agones.dev.sdk.Duration) obj;

    if (getSeconds()
        != other.getSeconds()) return false;
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + SECONDS_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getSeconds());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static dev.agones.dev.sdk.Duration parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static dev.agones.dev.sdk.Duration parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static dev.agones.dev.sdk.Duration parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static dev.agones.dev.sdk.Duration parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static dev.agones.dev.sdk.Duration parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static dev.agones.dev.sdk.Duration parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static dev.agones.dev.sdk.Duration parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static dev.agones.dev.sdk.Duration parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static dev.agones.dev.sdk.Duration parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static dev.agones.dev.sdk.Duration parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static dev.agones.dev.sdk.Duration parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static dev.agones.dev.sdk.Duration parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(dev.agones.dev.sdk.Duration prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * time duration, in seconds
   * </pre>
   *
   * Protobuf type {@code agones.dev.sdk.Duration}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:agones.dev.sdk.Duration)
      dev.agones.dev.sdk.DurationOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return dev.agones.dev.sdk.SdkProto.internal_static_agones_dev_sdk_Duration_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return dev.agones.dev.sdk.SdkProto.internal_static_agones_dev_sdk_Duration_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              dev.agones.dev.sdk.Duration.class, dev.agones.dev.sdk.Duration.Builder.class);
    }

    // Construct using dev.agones.dev.sdk.Duration.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      seconds_ = 0L;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return dev.agones.dev.sdk.SdkProto.internal_static_agones_dev_sdk_Duration_descriptor;
    }

    @java.lang.Override
    public dev.agones.dev.sdk.Duration getDefaultInstanceForType() {
      return dev.agones.dev.sdk.Duration.getDefaultInstance();
    }

    @java.lang.Override
    public dev.agones.dev.sdk.Duration build() {
      dev.agones.dev.sdk.Duration result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public dev.agones.dev.sdk.Duration buildPartial() {
      dev.agones.dev.sdk.Duration result = new dev.agones.dev.sdk.Duration(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(dev.agones.dev.sdk.Duration result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.seconds_ = seconds_;
      }
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof dev.agones.dev.sdk.Duration) {
        return mergeFrom((dev.agones.dev.sdk.Duration)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(dev.agones.dev.sdk.Duration other) {
      if (other == dev.agones.dev.sdk.Duration.getDefaultInstance()) return this;
      if (other.getSeconds() != 0L) {
        setSeconds(other.getSeconds());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {
              seconds_ = input.readInt64();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private long seconds_ ;
    /**
     * <code>int64 seconds = 1 [json_name = "seconds"];</code>
     * @return The seconds.
     */
    @java.lang.Override
    public long getSeconds() {
      return seconds_;
    }
    /**
     * <code>int64 seconds = 1 [json_name = "seconds"];</code>
     * @param value The seconds to set.
     * @return This builder for chaining.
     */
    public Builder setSeconds(long value) {

      seconds_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>int64 seconds = 1 [json_name = "seconds"];</code>
     * @return This builder for chaining.
     */
    public Builder clearSeconds() {
      bitField0_ = (bitField0_ & ~0x00000001);
      seconds_ = 0L;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:agones.dev.sdk.Duration)
  }

  // @@protoc_insertion_point(class_scope:agones.dev.sdk.Duration)
  private static final dev.agones.dev.sdk.Duration DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new dev.agones.dev.sdk.Duration();
  }

  public static dev.agones.dev.sdk.Duration getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Duration>
      PARSER = new com.google.protobuf.AbstractParser<Duration>() {
    @java.lang.Override
    public Duration parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<Duration> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Duration> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public dev.agones.dev.sdk.Duration getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

