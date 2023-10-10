package dev.agones.dev.sdk;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * SDK service to be used in the GameServer SDK to the Pod Sidecar
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: proto/agones/v1/sdk.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class SDKGrpc {

  private SDKGrpc() {}

  public static final java.lang.String SERVICE_NAME = "agones.dev.sdk.SDK";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.Empty> getReadyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Ready",
      requestType = dev.agones.dev.sdk.Empty.class,
      responseType = dev.agones.dev.sdk.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.Empty> getReadyMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.Empty> getReadyMethod;
    if ((getReadyMethod = SDKGrpc.getReadyMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getReadyMethod = SDKGrpc.getReadyMethod) == null) {
          SDKGrpc.getReadyMethod = getReadyMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Ready"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("Ready"))
              .build();
        }
      }
    }
    return getReadyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.Empty> getAllocateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Allocate",
      requestType = dev.agones.dev.sdk.Empty.class,
      responseType = dev.agones.dev.sdk.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.Empty> getAllocateMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.Empty> getAllocateMethod;
    if ((getAllocateMethod = SDKGrpc.getAllocateMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getAllocateMethod = SDKGrpc.getAllocateMethod) == null) {
          SDKGrpc.getAllocateMethod = getAllocateMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Allocate"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("Allocate"))
              .build();
        }
      }
    }
    return getAllocateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.Empty> getShutdownMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Shutdown",
      requestType = dev.agones.dev.sdk.Empty.class,
      responseType = dev.agones.dev.sdk.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.Empty> getShutdownMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.Empty> getShutdownMethod;
    if ((getShutdownMethod = SDKGrpc.getShutdownMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getShutdownMethod = SDKGrpc.getShutdownMethod) == null) {
          SDKGrpc.getShutdownMethod = getShutdownMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Shutdown"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("Shutdown"))
              .build();
        }
      }
    }
    return getShutdownMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.Empty> getHealthMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Health",
      requestType = dev.agones.dev.sdk.Empty.class,
      responseType = dev.agones.dev.sdk.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.Empty> getHealthMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.Empty> getHealthMethod;
    if ((getHealthMethod = SDKGrpc.getHealthMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getHealthMethod = SDKGrpc.getHealthMethod) == null) {
          SDKGrpc.getHealthMethod = getHealthMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Health"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("Health"))
              .build();
        }
      }
    }
    return getHealthMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.GameServer> getGetGameServerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetGameServer",
      requestType = dev.agones.dev.sdk.Empty.class,
      responseType = dev.agones.dev.sdk.GameServer.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.GameServer> getGetGameServerMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.GameServer> getGetGameServerMethod;
    if ((getGetGameServerMethod = SDKGrpc.getGetGameServerMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getGetGameServerMethod = SDKGrpc.getGetGameServerMethod) == null) {
          SDKGrpc.getGetGameServerMethod = getGetGameServerMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.GameServer>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetGameServer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.GameServer.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("GetGameServer"))
              .build();
        }
      }
    }
    return getGetGameServerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.GameServer> getWatchGameServerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WatchGameServer",
      requestType = dev.agones.dev.sdk.Empty.class,
      responseType = dev.agones.dev.sdk.GameServer.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty,
      dev.agones.dev.sdk.GameServer> getWatchGameServerMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.GameServer> getWatchGameServerMethod;
    if ((getWatchGameServerMethod = SDKGrpc.getWatchGameServerMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getWatchGameServerMethod = SDKGrpc.getWatchGameServerMethod) == null) {
          SDKGrpc.getWatchGameServerMethod = getWatchGameServerMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.Empty, dev.agones.dev.sdk.GameServer>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WatchGameServer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.GameServer.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("WatchGameServer"))
              .build();
        }
      }
    }
    return getWatchGameServerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.KeyValue,
      dev.agones.dev.sdk.Empty> getSetLabelMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetLabel",
      requestType = dev.agones.dev.sdk.KeyValue.class,
      responseType = dev.agones.dev.sdk.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.KeyValue,
      dev.agones.dev.sdk.Empty> getSetLabelMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.KeyValue, dev.agones.dev.sdk.Empty> getSetLabelMethod;
    if ((getSetLabelMethod = SDKGrpc.getSetLabelMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getSetLabelMethod = SDKGrpc.getSetLabelMethod) == null) {
          SDKGrpc.getSetLabelMethod = getSetLabelMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.KeyValue, dev.agones.dev.sdk.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetLabel"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.KeyValue.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("SetLabel"))
              .build();
        }
      }
    }
    return getSetLabelMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.KeyValue,
      dev.agones.dev.sdk.Empty> getSetAnnotationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetAnnotation",
      requestType = dev.agones.dev.sdk.KeyValue.class,
      responseType = dev.agones.dev.sdk.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.KeyValue,
      dev.agones.dev.sdk.Empty> getSetAnnotationMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.KeyValue, dev.agones.dev.sdk.Empty> getSetAnnotationMethod;
    if ((getSetAnnotationMethod = SDKGrpc.getSetAnnotationMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getSetAnnotationMethod = SDKGrpc.getSetAnnotationMethod) == null) {
          SDKGrpc.getSetAnnotationMethod = getSetAnnotationMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.KeyValue, dev.agones.dev.sdk.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetAnnotation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.KeyValue.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("SetAnnotation"))
              .build();
        }
      }
    }
    return getSetAnnotationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.agones.dev.sdk.Duration,
      dev.agones.dev.sdk.Empty> getReserveMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Reserve",
      requestType = dev.agones.dev.sdk.Duration.class,
      responseType = dev.agones.dev.sdk.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.agones.dev.sdk.Duration,
      dev.agones.dev.sdk.Empty> getReserveMethod() {
    io.grpc.MethodDescriptor<dev.agones.dev.sdk.Duration, dev.agones.dev.sdk.Empty> getReserveMethod;
    if ((getReserveMethod = SDKGrpc.getReserveMethod) == null) {
      synchronized (SDKGrpc.class) {
        if ((getReserveMethod = SDKGrpc.getReserveMethod) == null) {
          SDKGrpc.getReserveMethod = getReserveMethod =
              io.grpc.MethodDescriptor.<dev.agones.dev.sdk.Duration, dev.agones.dev.sdk.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Reserve"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Duration.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.agones.dev.sdk.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new SDKMethodDescriptorSupplier("Reserve"))
              .build();
        }
      }
    }
    return getReserveMethod;
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
   * SDK service to be used in the GameServer SDK to the Pod Sidecar
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Call when the GameServer is ready
     * </pre>
     */
    default void ready(dev.agones.dev.sdk.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReadyMethod(), responseObserver);
    }

    /**
     * <pre>
     * Call to self Allocation the GameServer
     * </pre>
     */
    default void allocate(dev.agones.dev.sdk.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAllocateMethod(), responseObserver);
    }

    /**
     * <pre>
     * Call when the GameServer is shutting down
     * </pre>
     */
    default void shutdown(dev.agones.dev.sdk.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getShutdownMethod(), responseObserver);
    }

    /**
     * <pre>
     * Send a Empty every d Duration to declare that this GameSever is healthy
     * </pre>
     */
    default io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> health(
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getHealthMethod(), responseObserver);
    }

    /**
     * <pre>
     * Retrieve the current GameServer data
     * </pre>
     */
    default void getGameServer(dev.agones.dev.sdk.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.GameServer> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetGameServerMethod(), responseObserver);
    }

    /**
     * <pre>
     * Send GameServer details whenever the GameServer is updated
     * </pre>
     */
    default void watchGameServer(dev.agones.dev.sdk.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.GameServer> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWatchGameServerMethod(), responseObserver);
    }

    /**
     * <pre>
     * Apply a Label to the backing GameServer metadata
     * </pre>
     */
    default void setLabel(dev.agones.dev.sdk.KeyValue request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetLabelMethod(), responseObserver);
    }

    /**
     * <pre>
     * Apply a Annotation to the backing GameServer metadata
     * </pre>
     */
    default void setAnnotation(dev.agones.dev.sdk.KeyValue request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetAnnotationMethod(), responseObserver);
    }

    /**
     * <pre>
     * Marks the GameServer as the Reserved state for Duration
     * </pre>
     */
    default void reserve(dev.agones.dev.sdk.Duration request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReserveMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service SDK.
   * <pre>
   * SDK service to be used in the GameServer SDK to the Pod Sidecar
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
   * SDK service to be used in the GameServer SDK to the Pod Sidecar
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
     * Call when the GameServer is ready
     * </pre>
     */
    public void ready(dev.agones.dev.sdk.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReadyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Call to self Allocation the GameServer
     * </pre>
     */
    public void allocate(dev.agones.dev.sdk.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAllocateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Call when the GameServer is shutting down
     * </pre>
     */
    public void shutdown(dev.agones.dev.sdk.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getShutdownMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Send a Empty every d Duration to declare that this GameSever is healthy
     * </pre>
     */
    public io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> health(
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getHealthMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * Retrieve the current GameServer data
     * </pre>
     */
    public void getGameServer(dev.agones.dev.sdk.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.GameServer> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetGameServerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Send GameServer details whenever the GameServer is updated
     * </pre>
     */
    public void watchGameServer(dev.agones.dev.sdk.Empty request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.GameServer> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getWatchGameServerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Apply a Label to the backing GameServer metadata
     * </pre>
     */
    public void setLabel(dev.agones.dev.sdk.KeyValue request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetLabelMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Apply a Annotation to the backing GameServer metadata
     * </pre>
     */
    public void setAnnotation(dev.agones.dev.sdk.KeyValue request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetAnnotationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Marks the GameServer as the Reserved state for Duration
     * </pre>
     */
    public void reserve(dev.agones.dev.sdk.Duration request,
        io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReserveMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service SDK.
   * <pre>
   * SDK service to be used in the GameServer SDK to the Pod Sidecar
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
     * Call when the GameServer is ready
     * </pre>
     */
    public dev.agones.dev.sdk.Empty ready(dev.agones.dev.sdk.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReadyMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Call to self Allocation the GameServer
     * </pre>
     */
    public dev.agones.dev.sdk.Empty allocate(dev.agones.dev.sdk.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAllocateMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Call when the GameServer is shutting down
     * </pre>
     */
    public dev.agones.dev.sdk.Empty shutdown(dev.agones.dev.sdk.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getShutdownMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Retrieve the current GameServer data
     * </pre>
     */
    public dev.agones.dev.sdk.GameServer getGameServer(dev.agones.dev.sdk.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetGameServerMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Send GameServer details whenever the GameServer is updated
     * </pre>
     */
    public java.util.Iterator<dev.agones.dev.sdk.GameServer> watchGameServer(
        dev.agones.dev.sdk.Empty request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getWatchGameServerMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Apply a Label to the backing GameServer metadata
     * </pre>
     */
    public dev.agones.dev.sdk.Empty setLabel(dev.agones.dev.sdk.KeyValue request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetLabelMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Apply a Annotation to the backing GameServer metadata
     * </pre>
     */
    public dev.agones.dev.sdk.Empty setAnnotation(dev.agones.dev.sdk.KeyValue request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetAnnotationMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Marks the GameServer as the Reserved state for Duration
     * </pre>
     */
    public dev.agones.dev.sdk.Empty reserve(dev.agones.dev.sdk.Duration request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReserveMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service SDK.
   * <pre>
   * SDK service to be used in the GameServer SDK to the Pod Sidecar
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
     * Call when the GameServer is ready
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.Empty> ready(
        dev.agones.dev.sdk.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReadyMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Call to self Allocation the GameServer
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.Empty> allocate(
        dev.agones.dev.sdk.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAllocateMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Call when the GameServer is shutting down
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.Empty> shutdown(
        dev.agones.dev.sdk.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getShutdownMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Retrieve the current GameServer data
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.GameServer> getGameServer(
        dev.agones.dev.sdk.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetGameServerMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Apply a Label to the backing GameServer metadata
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.Empty> setLabel(
        dev.agones.dev.sdk.KeyValue request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetLabelMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Apply a Annotation to the backing GameServer metadata
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.Empty> setAnnotation(
        dev.agones.dev.sdk.KeyValue request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetAnnotationMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Marks the GameServer as the Reserved state for Duration
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.agones.dev.sdk.Empty> reserve(
        dev.agones.dev.sdk.Duration request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReserveMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_READY = 0;
  private static final int METHODID_ALLOCATE = 1;
  private static final int METHODID_SHUTDOWN = 2;
  private static final int METHODID_GET_GAME_SERVER = 3;
  private static final int METHODID_WATCH_GAME_SERVER = 4;
  private static final int METHODID_SET_LABEL = 5;
  private static final int METHODID_SET_ANNOTATION = 6;
  private static final int METHODID_RESERVE = 7;
  private static final int METHODID_HEALTH = 8;

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
        case METHODID_READY:
          serviceImpl.ready((dev.agones.dev.sdk.Empty) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty>) responseObserver);
          break;
        case METHODID_ALLOCATE:
          serviceImpl.allocate((dev.agones.dev.sdk.Empty) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty>) responseObserver);
          break;
        case METHODID_SHUTDOWN:
          serviceImpl.shutdown((dev.agones.dev.sdk.Empty) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty>) responseObserver);
          break;
        case METHODID_GET_GAME_SERVER:
          serviceImpl.getGameServer((dev.agones.dev.sdk.Empty) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.GameServer>) responseObserver);
          break;
        case METHODID_WATCH_GAME_SERVER:
          serviceImpl.watchGameServer((dev.agones.dev.sdk.Empty) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.GameServer>) responseObserver);
          break;
        case METHODID_SET_LABEL:
          serviceImpl.setLabel((dev.agones.dev.sdk.KeyValue) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty>) responseObserver);
          break;
        case METHODID_SET_ANNOTATION:
          serviceImpl.setAnnotation((dev.agones.dev.sdk.KeyValue) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty>) responseObserver);
          break;
        case METHODID_RESERVE:
          serviceImpl.reserve((dev.agones.dev.sdk.Duration) request,
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty>) responseObserver);
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
        case METHODID_HEALTH:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.health(
              (io.grpc.stub.StreamObserver<dev.agones.dev.sdk.Empty>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getReadyMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.Empty,
              dev.agones.dev.sdk.Empty>(
                service, METHODID_READY)))
        .addMethod(
          getAllocateMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.Empty,
              dev.agones.dev.sdk.Empty>(
                service, METHODID_ALLOCATE)))
        .addMethod(
          getShutdownMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.Empty,
              dev.agones.dev.sdk.Empty>(
                service, METHODID_SHUTDOWN)))
        .addMethod(
          getHealthMethod(),
          io.grpc.stub.ServerCalls.asyncClientStreamingCall(
            new MethodHandlers<
              dev.agones.dev.sdk.Empty,
              dev.agones.dev.sdk.Empty>(
                service, METHODID_HEALTH)))
        .addMethod(
          getGetGameServerMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.Empty,
              dev.agones.dev.sdk.GameServer>(
                service, METHODID_GET_GAME_SERVER)))
        .addMethod(
          getWatchGameServerMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              dev.agones.dev.sdk.Empty,
              dev.agones.dev.sdk.GameServer>(
                service, METHODID_WATCH_GAME_SERVER)))
        .addMethod(
          getSetLabelMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.KeyValue,
              dev.agones.dev.sdk.Empty>(
                service, METHODID_SET_LABEL)))
        .addMethod(
          getSetAnnotationMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.KeyValue,
              dev.agones.dev.sdk.Empty>(
                service, METHODID_SET_ANNOTATION)))
        .addMethod(
          getReserveMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.agones.dev.sdk.Duration,
              dev.agones.dev.sdk.Empty>(
                service, METHODID_RESERVE)))
        .build();
  }

  private static abstract class SDKBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SDKBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return dev.agones.dev.sdk.SdkProto.getDescriptor();
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
              .addMethod(getReadyMethod())
              .addMethod(getAllocateMethod())
              .addMethod(getShutdownMethod())
              .addMethod(getHealthMethod())
              .addMethod(getGetGameServerMethod())
              .addMethod(getWatchGameServerMethod())
              .addMethod(getSetLabelMethod())
              .addMethod(getSetAnnotationMethod())
              .addMethod(getReserveMethod())
              .build();
        }
      }
    }
    return result;
  }
}
