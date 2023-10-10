package dev.agones.dev.sdk.alpha;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * SDK service to be used in the GameServer SDK to the Pod Sidecar.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: proto/agones/v1/alpha/sdk.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class SDKGrpc {

  private SDKGrpc() {}

  public static final java.lang.String SERVICE_NAME = "agones.dev.sdk.alpha.SDK";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.PlayerID,
      dev.agones.dev.sdk.alpha.Bool> getPlayerConnectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PlayerConnect",
      requestType = dev.agones.dev.sdk.alpha.PlayerID.class,
      responseType = dev.agones.dev.sdk.alpha.Bool.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.PlayerID,
      dev.agones.dev.sdk.alpha.Bool> getPlayerConnectMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.PlayerID, dev.agones.dev.sdk.alpha.Bool> getPlayerConnectMethod;
    if ((getPlayerConnectMethod = SDKGrpc.getPlayerConnectMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getPlayerConnectMethod = SDKGrpc.getPlayerConnectMethod) == null) {
          SDKGrpc.getPlayerConnectMethod = getPlayerConnectMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.PlayerID, dev.agones.dev.sdk.alpha.Bool>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PlayerConnect"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.PlayerID.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Bool.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("PlayerConnect"))
              .build();
        }
      }
    }
    return getPlayerConnectMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.PlayerID,
      dev.agones.dev.sdk.alpha.Bool> getPlayerDisconnectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PlayerDisconnect",
      requestType = dev.agones.dev.sdk.alpha.PlayerID.class,
      responseType = dev.agones.dev.sdk.alpha.Bool.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.PlayerID,
      dev.agones.dev.sdk.alpha.Bool> getPlayerDisconnectMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.PlayerID, dev.agones.dev.sdk.alpha.Bool> getPlayerDisconnectMethod;
    if ((getPlayerDisconnectMethod = SDKGrpc.getPlayerDisconnectMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getPlayerDisconnectMethod = SDKGrpc.getPlayerDisconnectMethod) == null) {
          SDKGrpc.getPlayerDisconnectMethod = getPlayerDisconnectMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.PlayerID, dev.agones.dev.sdk.alpha.Bool>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PlayerDisconnect"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.PlayerID.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Bool.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("PlayerDisconnect"))
              .build();
        }
      }
    }
    return getPlayerDisconnectMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Count,
      dev.agones.dev.sdk.alpha.Empty> getSetPlayerCapacityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetPlayerCapacity",
      requestType = dev.agones.dev.sdk.alpha.Count.class,
      responseType = dev.agones.dev.sdk.alpha.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Count,
      dev.agones.dev.sdk.alpha.Empty> getSetPlayerCapacityMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Count, dev.agones.dev.sdk.alpha.Empty> getSetPlayerCapacityMethod;
    if ((getSetPlayerCapacityMethod = SDKGrpc.getSetPlayerCapacityMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getSetPlayerCapacityMethod = SDKGrpc.getSetPlayerCapacityMethod) == null) {
          SDKGrpc.getSetPlayerCapacityMethod = getSetPlayerCapacityMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.Count, dev.agones.dev.sdk.alpha.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetPlayerCapacity"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Count.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("SetPlayerCapacity"))
              .build();
        }
      }
    }
    return getSetPlayerCapacityMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Empty,
      dev.agones.dev.sdk.alpha.Count> getGetPlayerCapacityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPlayerCapacity",
      requestType = dev.agones.dev.sdk.alpha.Empty.class,
      responseType = dev.agones.dev.sdk.alpha.Count.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Empty,
      dev.agones.dev.sdk.alpha.Count> getGetPlayerCapacityMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Empty, dev.agones.dev.sdk.alpha.Count> getGetPlayerCapacityMethod;
    if ((getGetPlayerCapacityMethod = SDKGrpc.getGetPlayerCapacityMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getGetPlayerCapacityMethod = SDKGrpc.getGetPlayerCapacityMethod) == null) {
          SDKGrpc.getGetPlayerCapacityMethod = getGetPlayerCapacityMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.Empty, dev.agones.dev.sdk.alpha.Count>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPlayerCapacity"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Count.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("GetPlayerCapacity"))
              .build();
        }
      }
    }
    return getGetPlayerCapacityMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Empty,
      dev.agones.dev.sdk.alpha.Count> getGetPlayerCountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPlayerCount",
      requestType = dev.agones.dev.sdk.alpha.Empty.class,
      responseType = dev.agones.dev.sdk.alpha.Count.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Empty,
      dev.agones.dev.sdk.alpha.Count> getGetPlayerCountMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Empty, dev.agones.dev.sdk.alpha.Count> getGetPlayerCountMethod;
    if ((getGetPlayerCountMethod = SDKGrpc.getGetPlayerCountMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getGetPlayerCountMethod = SDKGrpc.getGetPlayerCountMethod) == null) {
          SDKGrpc.getGetPlayerCountMethod = getGetPlayerCountMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.Empty, dev.agones.dev.sdk.alpha.Count>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPlayerCount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Count.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("GetPlayerCount"))
              .build();
        }
      }
    }
    return getGetPlayerCountMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.PlayerID,
      dev.agones.dev.sdk.alpha.Bool> getIsPlayerConnectedMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "IsPlayerConnected",
      requestType = dev.agones.dev.sdk.alpha.PlayerID.class,
      responseType = dev.agones.dev.sdk.alpha.Bool.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.PlayerID,
      dev.agones.dev.sdk.alpha.Bool> getIsPlayerConnectedMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.PlayerID, dev.agones.dev.sdk.alpha.Bool> getIsPlayerConnectedMethod;
    if ((getIsPlayerConnectedMethod = SDKGrpc.getIsPlayerConnectedMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getIsPlayerConnectedMethod = SDKGrpc.getIsPlayerConnectedMethod) == null) {
          SDKGrpc.getIsPlayerConnectedMethod = getIsPlayerConnectedMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.PlayerID, dev.agones.dev.sdk.alpha.Bool>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "IsPlayerConnected"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.PlayerID.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Bool.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("IsPlayerConnected"))
              .build();
        }
      }
    }
    return getIsPlayerConnectedMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Empty,
      dev.agones.dev.sdk.alpha.PlayerIDList> getGetConnectedPlayersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetConnectedPlayers",
      requestType = dev.agones.dev.sdk.alpha.Empty.class,
      responseType = dev.agones.dev.sdk.alpha.PlayerIDList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Empty,
      dev.agones.dev.sdk.alpha.PlayerIDList> getGetConnectedPlayersMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.Empty, dev.agones.dev.sdk.alpha.PlayerIDList> getGetConnectedPlayersMethod;
    if ((getGetConnectedPlayersMethod = SDKGrpc.getGetConnectedPlayersMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getGetConnectedPlayersMethod = SDKGrpc.getGetConnectedPlayersMethod) == null) {
          SDKGrpc.getGetConnectedPlayersMethod = getGetConnectedPlayersMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.Empty, dev.agones.dev.sdk.alpha.PlayerIDList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetConnectedPlayers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.PlayerIDList.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("GetConnectedPlayers"))
              .build();
        }
      }
    }
    return getGetConnectedPlayersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.GetCounterRequest,
      dev.agones.dev.sdk.alpha.Counter> getGetCounterMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCounter",
      requestType = dev.agones.dev.sdk.alpha.GetCounterRequest.class,
      responseType = dev.agones.dev.sdk.alpha.Counter.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.GetCounterRequest,
      dev.agones.dev.sdk.alpha.Counter> getGetCounterMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.GetCounterRequest, dev.agones.dev.sdk.alpha.Counter> getGetCounterMethod;
    if ((getGetCounterMethod = SDKGrpc.getGetCounterMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getGetCounterMethod = SDKGrpc.getGetCounterMethod) == null) {
          SDKGrpc.getGetCounterMethod = getGetCounterMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.GetCounterRequest, dev.agones.dev.sdk.alpha.Counter>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCounter"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.GetCounterRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Counter.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("GetCounter"))
              .build();
        }
      }
    }
    return getGetCounterMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.UpdateCounterRequest,
      dev.agones.dev.sdk.alpha.Counter> getUpdateCounterMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateCounter",
      requestType = dev.agones.dev.sdk.alpha.UpdateCounterRequest.class,
      responseType = dev.agones.dev.sdk.alpha.Counter.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.UpdateCounterRequest,
      dev.agones.dev.sdk.alpha.Counter> getUpdateCounterMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.UpdateCounterRequest, dev.agones.dev.sdk.alpha.Counter> getUpdateCounterMethod;
    if ((getUpdateCounterMethod = SDKGrpc.getUpdateCounterMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getUpdateCounterMethod = SDKGrpc.getUpdateCounterMethod) == null) {
          SDKGrpc.getUpdateCounterMethod = getUpdateCounterMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.UpdateCounterRequest, dev.agones.dev.sdk.alpha.Counter>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateCounter"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.UpdateCounterRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.Counter.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("UpdateCounter"))
              .build();
        }
      }
    }
    return getUpdateCounterMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.GetListRequest,
      dev.agones.dev.sdk.alpha.List> getGetListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetList",
      requestType = dev.agones.dev.sdk.alpha.GetListRequest.class,
      responseType = dev.agones.dev.sdk.alpha.List.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.GetListRequest,
      dev.agones.dev.sdk.alpha.List> getGetListMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.GetListRequest, dev.agones.dev.sdk.alpha.List> getGetListMethod;
    if ((getGetListMethod = SDKGrpc.getGetListMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getGetListMethod = SDKGrpc.getGetListMethod) == null) {
          SDKGrpc.getGetListMethod = getGetListMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.GetListRequest, dev.agones.dev.sdk.alpha.List>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetList"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.GetListRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.List.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("GetList"))
              .build();
        }
      }
    }
    return getGetListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.UpdateListRequest,
      dev.agones.dev.sdk.alpha.List> getUpdateListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateList",
      requestType = dev.agones.dev.sdk.alpha.UpdateListRequest.class,
      responseType = dev.agones.dev.sdk.alpha.List.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.UpdateListRequest,
      dev.agones.dev.sdk.alpha.List> getUpdateListMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.UpdateListRequest, dev.agones.dev.sdk.alpha.List> getUpdateListMethod;
    if ((getUpdateListMethod = SDKGrpc.getUpdateListMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getUpdateListMethod = SDKGrpc.getUpdateListMethod) == null) {
          SDKGrpc.getUpdateListMethod = getUpdateListMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.UpdateListRequest, dev.agones.dev.sdk.alpha.List>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateList"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.UpdateListRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.List.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("UpdateList"))
              .build();
        }
      }
    }
    return getUpdateListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.AddListValueRequest,
      dev.agones.dev.sdk.alpha.List> getAddListValueMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddListValue",
      requestType = dev.agones.dev.sdk.alpha.AddListValueRequest.class,
      responseType = dev.agones.dev.sdk.alpha.List.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.AddListValueRequest,
      dev.agones.dev.sdk.alpha.List> getAddListValueMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.AddListValueRequest, dev.agones.dev.sdk.alpha.List> getAddListValueMethod;
    if ((getAddListValueMethod = SDKGrpc.getAddListValueMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getAddListValueMethod = SDKGrpc.getAddListValueMethod) == null) {
          SDKGrpc.getAddListValueMethod = getAddListValueMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.AddListValueRequest, dev.agones.dev.sdk.alpha.List>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddListValue"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.AddListValueRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.List.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("AddListValue"))
              .build();
        }
      }
    }
    return getAddListValueMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.RemoveListValueRequest,
      dev.agones.dev.sdk.alpha.List> getRemoveListValueMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RemoveListValue",
      requestType = dev.agones.dev.sdk.alpha.RemoveListValueRequest.class,
      responseType = dev.agones.dev.sdk.alpha.List.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.RemoveListValueRequest,
      dev.agones.dev.sdk.alpha.List> getRemoveListValueMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.alpha.RemoveListValueRequest, dev.agones.dev.sdk.alpha.List> getRemoveListValueMethod;
    if ((getRemoveListValueMethod = SDKGrpc.getRemoveListValueMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getRemoveListValueMethod = SDKGrpc.getRemoveListValueMethod) == null) {
          SDKGrpc.getRemoveListValueMethod = getRemoveListValueMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.alpha.RemoveListValueRequest, dev.agones.dev.sdk.alpha.List>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RemoveListValue"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.RemoveListValueRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.alpha.List.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("RemoveListValue"))
              .build();
        }
      }
    }
    return getRemoveListValueMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SDKStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SDKStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SDKStub>() {
        @java.lang.Override
        public SDKStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SDKStub(channel, callOptions);
        }
      };
    return SDKStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SDKBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SDKBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SDKBlockingStub>() {
        @java.lang.Override
        public SDKBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SDKBlockingStub(channel, callOptions);
        }
      };
    return SDKBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SDKFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SDKFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SDKFutureStub>() {
        @java.lang.Override
        public SDKFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SDKFutureStub(channel, callOptions);
        }
      };
    return SDKFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * SDK service to be used in the GameServer SDK to the Pod Sidecar.
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * PlayerConnect increases the SDK’s stored player count by one, and appends this playerID to GameServer.Status.Players.IDs.
     * GameServer.Status.Players.Count and GameServer.Status.Players.IDs are then set to update the player count and id list a second from now,
     * unless there is already an update pending, in which case the update joins that batch operation.
     * PlayerConnect returns true and adds the playerID to the list of playerIDs if this playerID was not already in the
     * list of connected playerIDs.
     * If the playerID exists within the list of connected playerIDs, PlayerConnect will return false, and the list of
     * connected playerIDs will be left unchanged.
     * An error will be returned if the playerID was not already in the list of connected playerIDs but the player capacity for
     * the server has been reached. The playerID will not be added to the list of playerIDs.
     * Warning: Do not use this method if you are manually managing GameServer.Status.Players.IDs and GameServer.Status.Players.Count
     * through the Kubernetes API, as indeterminate results will occur.
     * </pre>
     */
    default void playerConnect(dev.agones.dev.sdk.alpha.PlayerID request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Bool> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPlayerConnectMethod(), responseObserver);
    }

    /**
     * <pre>
     * Decreases the SDK’s stored player count by one, and removes the playerID from GameServer.Status.Players.IDs.
     * GameServer.Status.Players.Count and GameServer.Status.Players.IDs are then set to update the player count and id list a second from now,
     * unless there is already an update pending, in which case the update joins that batch operation.
     * PlayerDisconnect will return true and remove the supplied playerID from the list of connected playerIDs if the
     * playerID value exists within the list.
     * If the playerID was not in the list of connected playerIDs, the call will return false, and the connected playerID list
     * will be left unchanged.
     * Warning: Do not use this method if you are manually managing GameServer.status.players.IDs and GameServer.status.players.Count
     * through the Kubernetes API, as indeterminate results will occur.
     * </pre>
     */
    default void playerDisconnect(dev.agones.dev.sdk.alpha.PlayerID request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Bool> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPlayerDisconnectMethod(), responseObserver);
    }

    /**
     * <pre>
     * Update the GameServer.Status.Players.Capacity value with a new capacity.
     * </pre>
     */
    default void setPlayerCapacity(dev.agones.dev.sdk.alpha.Count request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetPlayerCapacityMethod(), responseObserver);
    }

    /**
     * <pre>
     * Retrieves the current player capacity. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.Capacity is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    default void getPlayerCapacity(dev.agones.dev.sdk.alpha.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Count> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPlayerCapacityMethod(), responseObserver);
    }

    /**
     * <pre>
     * Retrieves the current player count. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.Count is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    default void getPlayerCount(dev.agones.dev.sdk.alpha.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Count> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPlayerCountMethod(), responseObserver);
    }

    /**
     * <pre>
     * Returns if the playerID is currently connected to the GameServer. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.IDs is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to determine connected status.
     * </pre>
     */
    default void isPlayerConnected(dev.agones.dev.sdk.alpha.PlayerID request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Bool> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getIsPlayerConnectedMethod(), responseObserver);
    }

    /**
     * <pre>
     * Returns the list of the currently connected player ids. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.IDs is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    default void getConnectedPlayers(dev.agones.dev.sdk.alpha.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.PlayerIDList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetConnectedPlayersMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets a Counter. Returns NOT_FOUND if the Counter does not exist.
     * </pre>
     */
    default void getCounter(dev.agones.dev.sdk.alpha.GetCounterRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Counter> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetCounterMethod(), responseObserver);
    }

    /**
     * <pre>
     * UpdateCounter returns the updated Counter. Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     * Returns OUT_OF_RANGE if the Count is out of range [0,Capacity].
     * Returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the Counter.
     * If a field mask path(s) is specified, but the value is not set in the request Counter object,
     * then the default value for the variable will be set (i.e. 0 for "capacity" or "count").
     * </pre>
     */
    default void updateCounter(dev.agones.dev.sdk.alpha.UpdateCounterRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Counter> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateCounterMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets a List. Returns NOT_FOUND if the List does not exist.
     * </pre>
     */
    default void getList(dev.agones.dev.sdk.alpha.GetListRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetListMethod(), responseObserver);
    }

    /**
     * <pre>
     * UpdateList returns the updated List. Returns NOT_FOUND if the List does not exist (name cannot be updated).
     * **THIS WILL OVERWRITE ALL EXISTING LIST.VALUES WITH ANY REQUEST LIST.VALUES**
     * Use AddListValue() or RemoveListValue() for modifying the List.Values field.
     * Returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the List.
     * If a field mask path(s) is specified, but the value is not set in the request List object,
     * then the default value for the variable will be set (i.e. 0 for "capacity", empty list for "values").
     * </pre>
     */
    default void updateList(dev.agones.dev.sdk.alpha.UpdateListRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateListMethod(), responseObserver);
    }

    /**
     * <pre>
     * Adds a value to a List and returns updated List. Returns NOT_FOUND if the List does not exist.
     * Returns ALREADY_EXISTS if the value is already in the List.
     * Returns OUT_OF_RANGE if the List is already at Capacity.
     * </pre>
     */
    default void addListValue(dev.agones.dev.sdk.alpha.AddListValueRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAddListValueMethod(), responseObserver);
    }

    /**
     * <pre>
     * Removes a value from a List and returns updated List. Returns NOT_FOUND if the List does not exist.
     * Returns NOT_FOUND if the value is not in the List.
     * </pre>
     */
    default void removeListValue(dev.agones.dev.sdk.alpha.RemoveListValueRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRemoveListValueMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service SDK.
   * <pre>
   * SDK service to be used in the GameServer SDK to the Pod Sidecar.
   * </pre>
   */
  public static abstract class SDKImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return SDKGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service SDK.
   * <pre>
   * SDK service to be used in the GameServer SDK to the Pod Sidecar.
   * </pre>
   */
  public static final class SDKStub
      extends io.grpc.stub.AbstractAsyncStub<SDKStub> {
    private SDKStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SDKStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SDKStub(channel, callOptions);
    }

    /**
     * <pre>
     * PlayerConnect increases the SDK’s stored player count by one, and appends this playerID to GameServer.Status.Players.IDs.
     * GameServer.Status.Players.Count and GameServer.Status.Players.IDs are then set to update the player count and id list a second from now,
     * unless there is already an update pending, in which case the update joins that batch operation.
     * PlayerConnect returns true and adds the playerID to the list of playerIDs if this playerID was not already in the
     * list of connected playerIDs.
     * If the playerID exists within the list of connected playerIDs, PlayerConnect will return false, and the list of
     * connected playerIDs will be left unchanged.
     * An error will be returned if the playerID was not already in the list of connected playerIDs but the player capacity for
     * the server has been reached. The playerID will not be added to the list of playerIDs.
     * Warning: Do not use this method if you are manually managing GameServer.Status.Players.IDs and GameServer.Status.Players.Count
     * through the Kubernetes API, as indeterminate results will occur.
     * </pre>
     */
    public void playerConnect(dev.agones.dev.sdk.alpha.PlayerID request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Bool> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPlayerConnectMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Decreases the SDK’s stored player count by one, and removes the playerID from GameServer.Status.Players.IDs.
     * GameServer.Status.Players.Count and GameServer.Status.Players.IDs are then set to update the player count and id list a second from now,
     * unless there is already an update pending, in which case the update joins that batch operation.
     * PlayerDisconnect will return true and remove the supplied playerID from the list of connected playerIDs if the
     * playerID value exists within the list.
     * If the playerID was not in the list of connected playerIDs, the call will return false, and the connected playerID list
     * will be left unchanged.
     * Warning: Do not use this method if you are manually managing GameServer.status.players.IDs and GameServer.status.players.Count
     * through the Kubernetes API, as indeterminate results will occur.
     * </pre>
     */
    public void playerDisconnect(dev.agones.dev.sdk.alpha.PlayerID request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Bool> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPlayerDisconnectMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Update the GameServer.Status.Players.Capacity value with a new capacity.
     * </pre>
     */
    public void setPlayerCapacity(dev.agones.dev.sdk.alpha.Count request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetPlayerCapacityMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Retrieves the current player capacity. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.Capacity is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    public void getPlayerCapacity(dev.agones.dev.sdk.alpha.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Count> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPlayerCapacityMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Retrieves the current player count. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.Count is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    public void getPlayerCount(dev.agones.dev.sdk.alpha.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Count> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPlayerCountMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns if the playerID is currently connected to the GameServer. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.IDs is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to determine connected status.
     * </pre>
     */
    public void isPlayerConnected(dev.agones.dev.sdk.alpha.PlayerID request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Bool> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getIsPlayerConnectedMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns the list of the currently connected player ids. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.IDs is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    public void getConnectedPlayers(dev.agones.dev.sdk.alpha.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.PlayerIDList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetConnectedPlayersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets a Counter. Returns NOT_FOUND if the Counter does not exist.
     * </pre>
     */
    public void getCounter(dev.agones.dev.sdk.alpha.GetCounterRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Counter> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetCounterMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * UpdateCounter returns the updated Counter. Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     * Returns OUT_OF_RANGE if the Count is out of range [0,Capacity].
     * Returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the Counter.
     * If a field mask path(s) is specified, but the value is not set in the request Counter object,
     * then the default value for the variable will be set (i.e. 0 for "capacity" or "count").
     * </pre>
     */
    public void updateCounter(dev.agones.dev.sdk.alpha.UpdateCounterRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Counter> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateCounterMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets a List. Returns NOT_FOUND if the List does not exist.
     * </pre>
     */
    public void getList(dev.agones.dev.sdk.alpha.GetListRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * UpdateList returns the updated List. Returns NOT_FOUND if the List does not exist (name cannot be updated).
     * **THIS WILL OVERWRITE ALL EXISTING LIST.VALUES WITH ANY REQUEST LIST.VALUES**
     * Use AddListValue() or RemoveListValue() for modifying the List.Values field.
     * Returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the List.
     * If a field mask path(s) is specified, but the value is not set in the request List object,
     * then the default value for the variable will be set (i.e. 0 for "capacity", empty list for "values").
     * </pre>
     */
    public void updateList(dev.agones.dev.sdk.alpha.UpdateListRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Adds a value to a List and returns updated List. Returns NOT_FOUND if the List does not exist.
     * Returns ALREADY_EXISTS if the value is already in the List.
     * Returns OUT_OF_RANGE if the List is already at Capacity.
     * </pre>
     */
    public void addListValue(dev.agones.dev.sdk.alpha.AddListValueRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAddListValueMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Removes a value from a List and returns updated List. Returns NOT_FOUND if the List does not exist.
     * Returns NOT_FOUND if the value is not in the List.
     * </pre>
     */
    public void removeListValue(dev.agones.dev.sdk.alpha.RemoveListValueRequest request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRemoveListValueMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service SDK.
   * <pre>
   * SDK service to be used in the GameServer SDK to the Pod Sidecar.
   * </pre>
   */
  public static final class SDKBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<SDKBlockingStub> {
    private SDKBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SDKBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SDKBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * PlayerConnect increases the SDK’s stored player count by one, and appends this playerID to GameServer.Status.Players.IDs.
     * GameServer.Status.Players.Count and GameServer.Status.Players.IDs are then set to update the player count and id list a second from now,
     * unless there is already an update pending, in which case the update joins that batch operation.
     * PlayerConnect returns true and adds the playerID to the list of playerIDs if this playerID was not already in the
     * list of connected playerIDs.
     * If the playerID exists within the list of connected playerIDs, PlayerConnect will return false, and the list of
     * connected playerIDs will be left unchanged.
     * An error will be returned if the playerID was not already in the list of connected playerIDs but the player capacity for
     * the server has been reached. The playerID will not be added to the list of playerIDs.
     * Warning: Do not use this method if you are manually managing GameServer.Status.Players.IDs and GameServer.Status.Players.Count
     * through the Kubernetes API, as indeterminate results will occur.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.Bool playerConnect(dev.agones.dev.sdk.alpha.PlayerID request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPlayerConnectMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Decreases the SDK’s stored player count by one, and removes the playerID from GameServer.Status.Players.IDs.
     * GameServer.Status.Players.Count and GameServer.Status.Players.IDs are then set to update the player count and id list a second from now,
     * unless there is already an update pending, in which case the update joins that batch operation.
     * PlayerDisconnect will return true and remove the supplied playerID from the list of connected playerIDs if the
     * playerID value exists within the list.
     * If the playerID was not in the list of connected playerIDs, the call will return false, and the connected playerID list
     * will be left unchanged.
     * Warning: Do not use this method if you are manually managing GameServer.status.players.IDs and GameServer.status.players.Count
     * through the Kubernetes API, as indeterminate results will occur.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.Bool playerDisconnect(dev.agones.dev.sdk.alpha.PlayerID request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPlayerDisconnectMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Update the GameServer.Status.Players.Capacity value with a new capacity.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.Empty setPlayerCapacity(dev.agones.dev.sdk.alpha.Count request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetPlayerCapacityMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Retrieves the current player capacity. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.Capacity is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.Count getPlayerCapacity(dev.agones.dev.sdk.alpha.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPlayerCapacityMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Retrieves the current player count. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.Count is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.Count getPlayerCount(dev.agones.dev.sdk.alpha.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPlayerCountMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns if the playerID is currently connected to the GameServer. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.IDs is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to determine connected status.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.Bool isPlayerConnected(dev.agones.dev.sdk.alpha.PlayerID request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getIsPlayerConnectedMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns the list of the currently connected player ids. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.IDs is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.PlayerIDList getConnectedPlayers(dev.agones.dev.sdk.alpha.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetConnectedPlayersMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets a Counter. Returns NOT_FOUND if the Counter does not exist.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.Counter getCounter(dev.agones.dev.sdk.alpha.GetCounterRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetCounterMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * UpdateCounter returns the updated Counter. Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     * Returns OUT_OF_RANGE if the Count is out of range [0,Capacity].
     * Returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the Counter.
     * If a field mask path(s) is specified, but the value is not set in the request Counter object,
     * then the default value for the variable will be set (i.e. 0 for "capacity" or "count").
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.Counter updateCounter(dev.agones.dev.sdk.alpha.UpdateCounterRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateCounterMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets a List. Returns NOT_FOUND if the List does not exist.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.List getList(dev.agones.dev.sdk.alpha.GetListRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * UpdateList returns the updated List. Returns NOT_FOUND if the List does not exist (name cannot be updated).
     * **THIS WILL OVERWRITE ALL EXISTING LIST.VALUES WITH ANY REQUEST LIST.VALUES**
     * Use AddListValue() or RemoveListValue() for modifying the List.Values field.
     * Returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the List.
     * If a field mask path(s) is specified, but the value is not set in the request List object,
     * then the default value for the variable will be set (i.e. 0 for "capacity", empty list for "values").
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.List updateList(dev.agones.dev.sdk.alpha.UpdateListRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Adds a value to a List and returns updated List. Returns NOT_FOUND if the List does not exist.
     * Returns ALREADY_EXISTS if the value is already in the List.
     * Returns OUT_OF_RANGE if the List is already at Capacity.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.List addListValue(dev.agones.dev.sdk.alpha.AddListValueRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAddListValueMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Removes a value from a List and returns updated List. Returns NOT_FOUND if the List does not exist.
     * Returns NOT_FOUND if the value is not in the List.
     * </pre>
     */
    public dev.agones.dev.sdk.alpha.List removeListValue(dev.agones.dev.sdk.alpha.RemoveListValueRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRemoveListValueMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service SDK.
   * <pre>
   * SDK service to be used in the GameServer SDK to the Pod Sidecar.
   * </pre>
   */
  public static final class SDKFutureStub
      extends io.grpc.stub.AbstractFutureStub<SDKFutureStub> {
    private SDKFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SDKFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SDKFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * PlayerConnect increases the SDK’s stored player count by one, and appends this playerID to GameServer.Status.Players.IDs.
     * GameServer.Status.Players.Count and GameServer.Status.Players.IDs are then set to update the player count and id list a second from now,
     * unless there is already an update pending, in which case the update joins that batch operation.
     * PlayerConnect returns true and adds the playerID to the list of playerIDs if this playerID was not already in the
     * list of connected playerIDs.
     * If the playerID exists within the list of connected playerIDs, PlayerConnect will return false, and the list of
     * connected playerIDs will be left unchanged.
     * An error will be returned if the playerID was not already in the list of connected playerIDs but the player capacity for
     * the server has been reached. The playerID will not be added to the list of playerIDs.
     * Warning: Do not use this method if you are manually managing GameServer.Status.Players.IDs and GameServer.Status.Players.Count
     * through the Kubernetes API, as indeterminate results will occur.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.Bool> playerConnect(
        dev.agones.dev.sdk.alpha.PlayerID request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPlayerConnectMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Decreases the SDK’s stored player count by one, and removes the playerID from GameServer.Status.Players.IDs.
     * GameServer.Status.Players.Count and GameServer.Status.Players.IDs are then set to update the player count and id list a second from now,
     * unless there is already an update pending, in which case the update joins that batch operation.
     * PlayerDisconnect will return true and remove the supplied playerID from the list of connected playerIDs if the
     * playerID value exists within the list.
     * If the playerID was not in the list of connected playerIDs, the call will return false, and the connected playerID list
     * will be left unchanged.
     * Warning: Do not use this method if you are manually managing GameServer.status.players.IDs and GameServer.status.players.Count
     * through the Kubernetes API, as indeterminate results will occur.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.Bool> playerDisconnect(
        dev.agones.dev.sdk.alpha.PlayerID request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPlayerDisconnectMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Update the GameServer.Status.Players.Capacity value with a new capacity.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.Empty> setPlayerCapacity(
        dev.agones.dev.sdk.alpha.Count request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetPlayerCapacityMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Retrieves the current player capacity. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.Capacity is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.Count> getPlayerCapacity(
        dev.agones.dev.sdk.alpha.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPlayerCapacityMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Retrieves the current player count. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.Count is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.Count> getPlayerCount(
        dev.agones.dev.sdk.alpha.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPlayerCountMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns if the playerID is currently connected to the GameServer. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.IDs is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to determine connected status.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.Bool> isPlayerConnected(
        dev.agones.dev.sdk.alpha.PlayerID request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getIsPlayerConnectedMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns the list of the currently connected player ids. This is always accurate from what has been set through this SDK,
     * even if the value has yet to be updated on the GameServer status resource.
     * If GameServer.Status.Players.IDs is set manually through the Kubernetes API, use SDK.GameServer() or SDK.WatchGameServer() instead to view this value.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.PlayerIDList> getConnectedPlayers(
        dev.agones.dev.sdk.alpha.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetConnectedPlayersMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets a Counter. Returns NOT_FOUND if the Counter does not exist.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.Counter> getCounter(
        dev.agones.dev.sdk.alpha.GetCounterRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetCounterMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * UpdateCounter returns the updated Counter. Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     * Returns OUT_OF_RANGE if the Count is out of range [0,Capacity].
     * Returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the Counter.
     * If a field mask path(s) is specified, but the value is not set in the request Counter object,
     * then the default value for the variable will be set (i.e. 0 for "capacity" or "count").
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.Counter> updateCounter(
        dev.agones.dev.sdk.alpha.UpdateCounterRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateCounterMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets a List. Returns NOT_FOUND if the List does not exist.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.List> getList(
        dev.agones.dev.sdk.alpha.GetListRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetListMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * UpdateList returns the updated List. Returns NOT_FOUND if the List does not exist (name cannot be updated).
     * **THIS WILL OVERWRITE ALL EXISTING LIST.VALUES WITH ANY REQUEST LIST.VALUES**
     * Use AddListValue() or RemoveListValue() for modifying the List.Values field.
     * Returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the List.
     * If a field mask path(s) is specified, but the value is not set in the request List object,
     * then the default value for the variable will be set (i.e. 0 for "capacity", empty list for "values").
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.List> updateList(
        dev.agones.dev.sdk.alpha.UpdateListRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateListMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Adds a value to a List and returns updated List. Returns NOT_FOUND if the List does not exist.
     * Returns ALREADY_EXISTS if the value is already in the List.
     * Returns OUT_OF_RANGE if the List is already at Capacity.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.List> addListValue(
        dev.agones.dev.sdk.alpha.AddListValueRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAddListValueMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Removes a value from a List and returns updated List. Returns NOT_FOUND if the List does not exist.
     * Returns NOT_FOUND if the value is not in the List.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.alpha.List> removeListValue(
        dev.agones.dev.sdk.alpha.RemoveListValueRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRemoveListValueMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PLAYER_CONNECT = 0;
  private static final int METHODID_PLAYER_DISCONNECT = 1;
  private static final int METHODID_SET_PLAYER_CAPACITY = 2;
  private static final int METHODID_GET_PLAYER_CAPACITY = 3;
  private static final int METHODID_GET_PLAYER_COUNT = 4;
  private static final int METHODID_IS_PLAYER_CONNECTED = 5;
  private static final int METHODID_GET_CONNECTED_PLAYERS = 6;
  private static final int METHODID_GET_COUNTER = 7;
  private static final int METHODID_UPDATE_COUNTER = 8;
  private static final int METHODID_GET_LIST = 9;
  private static final int METHODID_UPDATE_LIST = 10;
  private static final int METHODID_ADD_LIST_VALUE = 11;
  private static final int METHODID_REMOVE_LIST_VALUE = 12;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PLAYER_CONNECT:
          serviceImpl.playerConnect((dev.agones.dev.sdk.alpha.PlayerID) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Bool>) responseObserver);
          break;
        case METHODID_PLAYER_DISCONNECT:
          serviceImpl.playerDisconnect((dev.agones.dev.sdk.alpha.PlayerID) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Bool>) responseObserver);
          break;
        case METHODID_SET_PLAYER_CAPACITY:
          serviceImpl.setPlayerCapacity((dev.agones.dev.sdk.alpha.Count) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Empty>) responseObserver);
          break;
        case METHODID_GET_PLAYER_CAPACITY:
          serviceImpl.getPlayerCapacity((dev.agones.dev.sdk.alpha.Empty) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Count>) responseObserver);
          break;
        case METHODID_GET_PLAYER_COUNT:
          serviceImpl.getPlayerCount((dev.agones.dev.sdk.alpha.Empty) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Count>) responseObserver);
          break;
        case METHODID_IS_PLAYER_CONNECTED:
          serviceImpl.isPlayerConnected((dev.agones.dev.sdk.alpha.PlayerID) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Bool>) responseObserver);
          break;
        case METHODID_GET_CONNECTED_PLAYERS:
          serviceImpl.getConnectedPlayers((dev.agones.dev.sdk.alpha.Empty) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.PlayerIDList>) responseObserver);
          break;
        case METHODID_GET_COUNTER:
          serviceImpl.getCounter((dev.agones.dev.sdk.alpha.GetCounterRequest) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Counter>) responseObserver);
          break;
        case METHODID_UPDATE_COUNTER:
          serviceImpl.updateCounter((dev.agones.dev.sdk.alpha.UpdateCounterRequest) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.Counter>) responseObserver);
          break;
        case METHODID_GET_LIST:
          serviceImpl.getList((dev.agones.dev.sdk.alpha.GetListRequest) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List>) responseObserver);
          break;
        case METHODID_UPDATE_LIST:
          serviceImpl.updateList((dev.agones.dev.sdk.alpha.UpdateListRequest) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List>) responseObserver);
          break;
        case METHODID_ADD_LIST_VALUE:
          serviceImpl.addListValue((dev.agones.dev.sdk.alpha.AddListValueRequest) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List>) responseObserver);
          break;
        case METHODID_REMOVE_LIST_VALUE:
          serviceImpl.removeListValue((dev.agones.dev.sdk.alpha.RemoveListValueRequest) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.alpha.List>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getPlayerConnectMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.PlayerID,
              dev.agones.dev.sdk.alpha.Bool>(
                service, METHODID_PLAYER_CONNECT)))
        .addMethod(
          getPlayerDisconnectMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.PlayerID,
              dev.agones.dev.sdk.alpha.Bool>(
                service, METHODID_PLAYER_DISCONNECT)))
        .addMethod(
          getSetPlayerCapacityMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.Count,
              dev.agones.dev.sdk.alpha.Empty>(
                service, METHODID_SET_PLAYER_CAPACITY)))
        .addMethod(
          getGetPlayerCapacityMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.Empty,
              dev.agones.dev.sdk.alpha.Count>(
                service, METHODID_GET_PLAYER_CAPACITY)))
        .addMethod(
          getGetPlayerCountMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.Empty,
              dev.agones.dev.sdk.alpha.Count>(
                service, METHODID_GET_PLAYER_COUNT)))
        .addMethod(
          getIsPlayerConnectedMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.PlayerID,
              dev.agones.dev.sdk.alpha.Bool>(
                service, METHODID_IS_PLAYER_CONNECTED)))
        .addMethod(
          getGetConnectedPlayersMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.Empty,
              dev.agones.dev.sdk.alpha.PlayerIDList>(
                service, METHODID_GET_CONNECTED_PLAYERS)))
        .addMethod(
          getGetCounterMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.GetCounterRequest,
              dev.agones.dev.sdk.alpha.Counter>(
                service, METHODID_GET_COUNTER)))
        .addMethod(
          getUpdateCounterMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.UpdateCounterRequest,
              dev.agones.dev.sdk.alpha.Counter>(
                service, METHODID_UPDATE_COUNTER)))
        .addMethod(
          getGetListMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.GetListRequest,
              dev.agones.dev.sdk.alpha.List>(
                service, METHODID_GET_LIST)))
        .addMethod(
          getUpdateListMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.UpdateListRequest,
              dev.agones.dev.sdk.alpha.List>(
                service, METHODID_UPDATE_LIST)))
        .addMethod(
          getAddListValueMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.AddListValueRequest,
              dev.agones.dev.sdk.alpha.List>(
                service, METHODID_ADD_LIST_VALUE)))
        .addMethod(
          getRemoveListValueMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.alpha.RemoveListValueRequest,
              dev.agones.dev.sdk.alpha.List>(
                service, METHODID_REMOVE_LIST_VALUE)))
        .build();
  }

  private static abstract class SDKBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SDKBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return dev.agones.dev.sdk.alpha.SdkProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("SDK");
    }
  }

  private static final class SDKFileDescriptorSupplier
      extends SDKBaseDescriptorSupplier {
    SDKFileDescriptorSupplier() {}
  }

  private static final class SDKMethodDescriptorSupplier
      extends SDKBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    SDKMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (SDKGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SDKFileDescriptorSupplier())
              .addMethod(getPlayerConnectMethod())
              .addMethod(getPlayerDisconnectMethod())
              .addMethod(getSetPlayerCapacityMethod())
              .addMethod(getGetPlayerCapacityMethod())
              .addMethod(getGetPlayerCountMethod())
              .addMethod(getIsPlayerConnectedMethod())
              .addMethod(getGetConnectedPlayersMethod())
              .addMethod(getGetCounterMethod())
              .addMethod(getUpdateCounterMethod())
              .addMethod(getGetListMethod())
              .addMethod(getUpdateListMethod())
              .addMethod(getAddListValueMethod())
              .addMethod(getRemoveListValueMethod())
              .build();
        }
      }
    }
    return result;
  }
}
