// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/agones/v1/alpha/sdk.proto

package dev.agones.dev.sdk.alpha;

/**
 * <pre>
 * A representation of a List.
 * </pre>
 *
 * Protobuf type {@code agones.dev.sdk.alpha.List}
 */
public final class List extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:agones.dev.sdk.alpha.List)
    ListOrBuilder {
private static final long serialVersionUID = 0L;
  // Use List.newBuilder() to construct.
  private List(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private List() {
    name_ = "";
    values_ =
        com.google.protobuf.LazyStringArrayList.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new List();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return dev.agones.dev.sdk.alpha.SdkProto.internal_static_agones_dev_sdk_alpha_List_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return dev.agones.dev.sdk.alpha.SdkProto.internal_static_agones_dev_sdk_alpha_List_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            dev.agones.dev.sdk.alpha.List.class, dev.agones.dev.sdk.alpha.List.Builder.class);
  }

  public static final int NAME_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object name_ = "";
  /**
   * <pre>
   * The name of the List
   * </pre>
   *
   * <code>string name = 1 [json_name = "name"];</code>
   * @return The name.
   */
  @java.lang.Override
  public java.lang.String getName() {
    java.lang.Object ref = name_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      name_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The name of the List
   * </pre>
   *
   * <code>string name = 1 [json_name = "name"];</code>
   * @return The bytes for name.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNameBytes() {
    java.lang.Object ref = name_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      name_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int CAPACITY_FIELD_NUMBER = 2;
  private long capacity_ = 0L;
  /**
   * <pre>
   * The maximum capacity of the List
   * </pre>
   *
   * <code>int64 capacity = 2 [json_name = "capacity"];</code>
   * @return The capacity.
   */
  @java.lang.Override
  public long getCapacity() {
    return capacity_;
  }

  public static final int VALUES_FIELD_NUMBER = 3;
  @SuppressWarnings("serial")
  private com.google.protobuf.LazyStringArrayList values_ =
      com.google.protobuf.LazyStringArrayList.emptyList();
  /**
   * <pre>
   * The array of items in the List ["v1", "v2", …]
   * </pre>
   *
   * <code>repeated string values = 3 [json_name = "values"];</code>
   * @return A list containing the values.
   */
  public com.google.protobuf.ProtocolStringList
      getValuesList() {
    return values_;
  }
  /**
   * <pre>
   * The array of items in the List ["v1", "v2", …]
   * </pre>
   *
   * <code>repeated string values = 3 [json_name = "values"];</code>
   * @return The count of values.
   */
  public int getValuesCount() {
    return values_.size();
  }
  /**
   * <pre>
   * The array of items in the List ["v1", "v2", …]
   * </pre>
   *
   * <code>repeated string values = 3 [json_name = "values"];</code>
   * @param index The index of the element to return.
   * @return The values at the given index.
   */
  public java.lang.String getValues(int index) {
    return values_.get(index);
  }
  /**
   * <pre>
   * The array of items in the List ["v1", "v2", …]
   * </pre>
   *
   * <code>repeated string values = 3 [json_name = "values"];</code>
   * @param index The index of the value to return.
   * @return The bytes of the values at the given index.
   */
  public com.google.protobuf.ByteString
      getValuesBytes(int index) {
    return values_.getByteString(index);
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(name_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, name_);
    }
    if (capacity_ != 0L) {
      output.writeInt64(2, capacity_);
    }
    for (int i = 0; i < values_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, values_.getRaw(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(name_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, name_);
    }
    if (capacity_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(2, capacity_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < values_.size(); i++) {
        dataSize += computeStringSizeNoTag(values_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getValuesList().size();
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
    if (!(obj instanceof dev.agones.dev.sdk.alpha.List)) {
      return super.equals(obj);
    }
    dev.agones.dev.sdk.alpha.List other = (dev.agones.dev.sdk.alpha.List) obj;

    if (!getName()
        .equals(other.getName())) return false;
    if (getCapacity()
        != other.getCapacity()) return false;
    if (!getValuesList()
        .equals(other.getValuesList())) return false;
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
    hash = (37 * hash) + NAME_FIELD_NUMBER;
    hash = (53 * hash) + getName().hashCode();
    hash = (37 * hash) + CAPACITY_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getCapacity());
    if (getValuesCount() > 0) {
      hash = (37 * hash) + VALUES_FIELD_NUMBER;
      hash = (53 * hash) + getValuesList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static dev.agones.dev.sdk.alpha.List parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static dev.agones.dev.sdk.alpha.List parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static dev.agones.dev.sdk.alpha.List parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static dev.agones.dev.sdk.alpha.List parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static dev.agones.dev.sdk.alpha.List parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static dev.agones.dev.sdk.alpha.List parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static dev.agones.dev.sdk.alpha.List parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static dev.agones.dev.sdk.alpha.List parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static dev.agones.dev.sdk.alpha.List parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static dev.agones.dev.sdk.alpha.List parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static dev.agones.dev.sdk.alpha.List parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static dev.agones.dev.sdk.alpha.List parseFrom(
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
  public static Builder newBuilder(dev.agones.dev.sdk.alpha.List prototype) {
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
   * A representation of a List.
   * </pre>
   *
   * Protobuf type {@code agones.dev.sdk.alpha.List}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:agones.dev.sdk.alpha.List)
      dev.agones.dev.sdk.alpha.ListOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return dev.agones.dev.sdk.alpha.SdkProto.internal_static_agones_dev_sdk_alpha_List_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return dev.agones.dev.sdk.alpha.SdkProto.internal_static_agones_dev_sdk_alpha_List_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              dev.agones.dev.sdk.alpha.List.class, dev.agones.dev.sdk.alpha.List.Builder.class);
    }

    // Construct using dev.agones.dev.sdk.alpha.List.newBuilder()
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
      name_ = "";
      capacity_ = 0L;
      values_ =
          com.google.protobuf.LazyStringArrayList.emptyList();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return dev.agones.dev.sdk.alpha.SdkProto.internal_static_agones_dev_sdk_alpha_List_descriptor;
    }

    @java.lang.Override
    public dev.agones.dev.sdk.alpha.List getDefaultInstanceForType() {
      return dev.agones.dev.sdk.alpha.List.getDefaultInstance();
    }

    @java.lang.Override
    public dev.agones.dev.sdk.alpha.List build() {
      dev.agones.dev.sdk.alpha.List result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public dev.agones.dev.sdk.alpha.List buildPartial() {
      dev.agones.dev.sdk.alpha.List result = new dev.agones.dev.sdk.alpha.List(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(dev.agones.dev.sdk.alpha.List result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.name_ = name_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.capacity_ = capacity_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        values_.makeImmutable();
        result.values_ = values_;
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
      if (other instanceof dev.agones.dev.sdk.alpha.List) {
        return mergeFrom((dev.agones.dev.sdk.alpha.List)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(dev.agones.dev.sdk.alpha.List other) {
      if (other == dev.agones.dev.sdk.alpha.List.getDefaultInstance()) return this;
      if (!other.getName().isEmpty()) {
        name_ = other.name_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.getCapacity() != 0L) {
        setCapacity(other.getCapacity());
      }
      if (!other.values_.isEmpty()) {
        if (values_.isEmpty()) {
          values_ = other.values_;
          bitField0_ |= 0x00000004;
        } else {
          ensureValuesIsMutable();
          values_.addAll(other.values_);
        }
        onChanged();
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
            case 10: {
              name_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              capacity_ = input.readInt64();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 26: {
              java.lang.String s = input.readStringRequireUtf8();
              ensureValuesIsMutable();
              values_.add(s);
              break;
            } // case 26
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

    private java.lang.Object name_ = "";
    /**
     * <pre>
     * The name of the List
     * </pre>
     *
     * <code>string name = 1 [json_name = "name"];</code>
     * @return The name.
     */
    public java.lang.String getName() {
      java.lang.Object ref = name_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        name_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The name of the List
     * </pre>
     *
     * <code>string name = 1 [json_name = "name"];</code>
     * @return The bytes for name.
     */
    public com.google.protobuf.ByteString
        getNameBytes() {
      java.lang.Object ref = name_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        name_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The name of the List
     * </pre>
     *
     * <code>string name = 1 [json_name = "name"];</code>
     * @param value The name to set.
     * @return This builder for chaining.
     */
    public Builder setName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      name_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The name of the List
     * </pre>
     *
     * <code>string name = 1 [json_name = "name"];</code>
     * @return This builder for chaining.
     */
    public Builder clearName() {
      name_ = getDefaultInstance().getName();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The name of the List
     * </pre>
     *
     * <code>string name = 1 [json_name = "name"];</code>
     * @param value The bytes for name to set.
     * @return This builder for chaining.
     */
    public Builder setNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      name_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private long capacity_ ;
    /**
     * <pre>
     * The maximum capacity of the List
     * </pre>
     *
     * <code>int64 capacity = 2 [json_name = "capacity"];</code>
     * @return The capacity.
     */
    @java.lang.Override
    public long getCapacity() {
      return capacity_;
    }
    /**
     * <pre>
     * The maximum capacity of the List
     * </pre>
     *
     * <code>int64 capacity = 2 [json_name = "capacity"];</code>
     * @param value The capacity to set.
     * @return This builder for chaining.
     */
    public Builder setCapacity(long value) {

      capacity_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The maximum capacity of the List
     * </pre>
     *
     * <code>int64 capacity = 2 [json_name = "capacity"];</code>
     * @return This builder for chaining.
     */
    public Builder clearCapacity() {
      bitField0_ = (bitField0_ & ~0x00000002);
      capacity_ = 0L;
      onChanged();
      return this;
    }

    private com.google.protobuf.LazyStringArrayList values_ =
        com.google.protobuf.LazyStringArrayList.emptyList();
    private void ensureValuesIsMutable() {
      if (!values_.isModifiable()) {
        values_ = new com.google.protobuf.LazyStringArrayList(values_);
      }
      bitField0_ |= 0x00000004;
    }
    /**
     * <pre>
     * The array of items in the List ["v1", "v2", …]
     * </pre>
     *
     * <code>repeated string values = 3 [json_name = "values"];</code>
     * @return A list containing the values.
     */
    public com.google.protobuf.ProtocolStringList
        getValuesList() {
      values_.makeImmutable();
      return values_;
    }
    /**
     * <pre>
     * The array of items in the List ["v1", "v2", …]
     * </pre>
     *
     * <code>repeated string values = 3 [json_name = "values"];</code>
     * @return The count of values.
     */
    public int getValuesCount() {
      return values_.size();
    }
    /**
     * <pre>
     * The array of items in the List ["v1", "v2", …]
     * </pre>
     *
     * <code>repeated string values = 3 [json_name = "values"];</code>
     * @param index The index of the element to return.
     * @return The values at the given index.
     */
    public java.lang.String getValues(int index) {
      return values_.get(index);
    }
    /**
     * <pre>
     * The array of items in the List ["v1", "v2", …]
     * </pre>
     *
     * <code>repeated string values = 3 [json_name = "values"];</code>
     * @param index The index of the value to return.
     * @return The bytes of the values at the given index.
     */
    public com.google.protobuf.ByteString
        getValuesBytes(int index) {
      return values_.getByteString(index);
    }
    /**
     * <pre>
     * The array of items in the List ["v1", "v2", …]
     * </pre>
     *
     * <code>repeated string values = 3 [json_name = "values"];</code>
     * @param index The index to set the value at.
     * @param value The values to set.
     * @return This builder for chaining.
     */
    public Builder setValues(
        int index, java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      ensureValuesIsMutable();
      values_.set(index, value);
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The array of items in the List ["v1", "v2", …]
     * </pre>
     *
     * <code>repeated string values = 3 [json_name = "values"];</code>
     * @param value The values to add.
     * @return This builder for chaining.
     */
    public Builder addValues(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      ensureValuesIsMutable();
      values_.add(value);
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The array of items in the List ["v1", "v2", …]
     * </pre>
     *
     * <code>repeated string values = 3 [json_name = "values"];</code>
     * @param values The values to add.
     * @return This builder for chaining.
     */
    public Builder addAllValues(
        java.lang.Iterable<java.lang.String> values) {
      ensureValuesIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, values_);
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The array of items in the List ["v1", "v2", …]
     * </pre>
     *
     * <code>repeated string values = 3 [json_name = "values"];</code>
     * @return This builder for chaining.
     */
    public Builder clearValues() {
      values_ =
        com.google.protobuf.LazyStringArrayList.emptyList();
      bitField0_ = (bitField0_ & ~0x00000004);;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The array of items in the List ["v1", "v2", …]
     * </pre>
     *
     * <code>repeated string values = 3 [json_name = "values"];</code>
     * @param value The bytes of the values to add.
     * @return This builder for chaining.
     */
    public Builder addValuesBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      ensureValuesIsMutable();
      values_.add(value);
      bitField0_ |= 0x00000004;
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


    // @@protoc_insertion_point(builder_scope:agones.dev.sdk.alpha.List)
  }

  // @@protoc_insertion_point(class_scope:agones.dev.sdk.alpha.List)
  private static final dev.agones.dev.sdk.alpha.List DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new dev.agones.dev.sdk.alpha.List();
  }

  public static dev.agones.dev.sdk.alpha.List getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<List>
      PARSER = new com.google.protobuf.AbstractParser<List>() {
    @java.lang.Override
    public List parsePartialFrom(
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

  public static com.google.protobuf.Parser<List> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<List> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public dev.agones.dev.sdk.alpha.List getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

