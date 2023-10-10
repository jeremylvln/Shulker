package io.shulkermc.sdk.v1alpha1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: proto/shulkermc/sdk/v1alpha1/minecraftserverfleet.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class MinecraftServerFleetServiceGrpc {

  private MinecraftServerFleetServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "shulkermc.sdk.v1alpha1.MinecraftServerFleetService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest,
      io.shulkermc.sdk.v1alpha1.SummonFromFleetReply> getSummonFromFleetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SummonFromFleet",
      requestType = io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest.class,
      responseType = io.shulkermc.sdk.v1alpha1.SummonFromFleetReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest,
      io.shulkermc.sdk.v1alpha1.SummonFromFleetReply> getSummonFromFleetMethod() {
    io.grpc.MethodDescriptor<io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest, io.shulkermc.sdk.v1alpha1.SummonFromFleetReply> getSummonFromFleetMethod;
    if ((getSummonFromFleetMethod = MinecraftServerFleetServiceGrpc.getSummonFromFleetMethod) == null) {
      synchronized (MinecraftServerFleetServiceGrpc.class) {
        if ((getSummonFromFleetMethod = MinecraftServerFleetServiceGrpc.getSummonFromFleetMethod) == null) {
          MinecraftServerFleetServiceGrpc.getSummonFromFleetMethod = getSummonFromFleetMethod =
              io.grpc.MethodDescriptor.<io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest, io.shulkermc.sdk.v1alpha1.SummonFromFleetReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SummonFromFleet"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.shulkermc.sdk.v1alpha1.SummonFromFleetReply.getDefaultInstance()))
              .setSchemaDescriptor(new MinecraftServerFleetServiceMethodDescriptorSupplier("SummonFromFleet"))
              .build();
        }
      }
    }
    return getSummonFromFleetMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MinecraftServerFleetServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MinecraftServerFleetServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MinecraftServerFleetServiceStub>() {
        @java.lang.Override
        public MinecraftServerFleetServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MinecraftServerFleetServiceStub(channel, callOptions);
        }
      };
    return MinecraftServerFleetServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MinecraftServerFleetServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MinecraftServerFleetServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MinecraftServerFleetServiceBlockingStub>() {
        @java.lang.Override
        public MinecraftServerFleetServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MinecraftServerFleetServiceBlockingStub(channel, callOptions);
        }
      };
    return MinecraftServerFleetServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MinecraftServerFleetServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MinecraftServerFleetServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MinecraftServerFleetServiceFutureStub>() {
        @java.lang.Override
        public MinecraftServerFleetServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MinecraftServerFleetServiceFutureStub(channel, callOptions);
        }
      };
    return MinecraftServerFleetServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void summonFromFleet(io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest request,
        io.grpc.stub.StreamObserver<io.shulkermc.sdk.v1alpha1.SummonFromFleetReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSummonFromFleetMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MinecraftServerFleetService.
   */
  public static abstract class MinecraftServerFleetServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MinecraftServerFleetServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MinecraftServerFleetService.
   */
  public static final class MinecraftServerFleetServiceStub
      extends io.grpc.stub.AbstractAsyncStub<MinecraftServerFleetServiceStub> {
    private MinecraftServerFleetServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MinecraftServerFleetServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MinecraftServerFleetServiceStub(channel, callOptions);
    }

    /**
     */
    public void summonFromFleet(io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest request,
        io.grpc.stub.StreamObserver<io.shulkermc.sdk.v1alpha1.SummonFromFleetReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSummonFromFleetMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MinecraftServerFleetService.
   */
  public static final class MinecraftServerFleetServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MinecraftServerFleetServiceBlockingStub> {
    private MinecraftServerFleetServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MinecraftServerFleetServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MinecraftServerFleetServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public io.shulkermc.sdk.v1alpha1.SummonFromFleetReply summonFromFleet(io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSummonFromFleetMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MinecraftServerFleetService.
   */
  public static final class MinecraftServerFleetServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<MinecraftServerFleetServiceFutureStub> {
    private MinecraftServerFleetServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MinecraftServerFleetServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MinecraftServerFleetServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shulkermc.sdk.v1alpha1.SummonFromFleetReply> summonFromFleet(
        io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSummonFromFleetMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SUMMON_FROM_FLEET = 0;

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
        case METHODID_SUMMON_FROM_FLEET:
          serviceImpl.summonFromFleet((io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest) request,
              (io.grpc.stub.StreamObserver<io.shulkermc.sdk.v1alpha1.SummonFromFleetReply>) responseObserver);
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
          getSummonFromFleetMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest,
              io.shulkermc.sdk.v1alpha1.SummonFromFleetReply>(
                service, METHODID_SUMMON_FROM_FLEET)))
        .build();
  }

  private static abstract class MinecraftServerFleetServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MinecraftServerFleetServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.shulkermc.sdk.v1alpha1.MinecraftserverfleetProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MinecraftServerFleetService");
    }
  }

  private static final class MinecraftServerFleetServiceFileDescriptorSupplier
      extends MinecraftServerFleetServiceBaseDescriptorSupplier {
    MinecraftServerFleetServiceFileDescriptorSupplier() {}
  }

  private static final class MinecraftServerFleetServiceMethodDescriptorSupplier
      extends MinecraftServerFleetServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MinecraftServerFleetServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (MinecraftServerFleetServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MinecraftServerFleetServiceFileDescriptorSupplier())
              .addMethod(getSummonFromFleetMethod())
              .build();
        }
      }
    }
    return result;
  }
}
