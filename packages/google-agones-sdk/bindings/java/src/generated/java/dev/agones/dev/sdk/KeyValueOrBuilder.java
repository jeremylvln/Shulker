// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/agones/v1/sdk.proto

package dev.agones.dev.sdk;

public interface KeyValueOrBuilder extends
    // @@protoc_insertion_point(interface_extends:agones.dev.sdk.KeyValue)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string key = 1 [json_name = "key"];</code>
   * @return The key.
   */
  java.lang.String getKey();
  /**
   * <code>string key = 1 [json_name = "key"];</code>
   * @return The bytes for key.
   */
  com.google.protobuf.ByteString
      getKeyBytes();

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
