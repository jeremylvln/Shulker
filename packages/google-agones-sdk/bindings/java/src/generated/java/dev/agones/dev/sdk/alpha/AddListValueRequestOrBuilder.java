// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/agones/v1/alpha/sdk.proto

package dev.agones.dev.sdk.alpha;

public interface AddListValueRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:agones.dev.sdk.alpha.AddListValueRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the List to add a value to.
   * </pre>
   *
   * <code>string name = 1 [json_name = "name"];</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * The name of the List to add a value to.
   * </pre>
   *
   * <code>string name = 1 [json_name = "name"];</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string value = 2 [json_name = "value"];</code>
   * @return The value.
   */
  java.lang.String getValue();
  /**
   * <code>string value = 2 [json_name = "value"];</code>
   * @return The bytes for value.
   */
  com.google.protobuf.ByteString
      getValueBytes();
}
