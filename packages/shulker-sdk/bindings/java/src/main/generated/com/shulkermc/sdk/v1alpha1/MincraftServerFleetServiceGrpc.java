package com.shulkermc.sdk.v1alpha1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: proto/shulkermc/sdk/v1alpha1/minecraftserverfleet.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class MincraftServerFleetServiceGrpc {

  private MincraftServerFleetServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "shulkermc.sdk.v1alpha1.MincraftServerFleetService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest,
      com.shulkermc.sdk.v1alpha1.SummonFromFleetReply> getSummonFromFleetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SummonFromFleet",
      requestType = com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest.class,
      responseType = com.shulkermc.sdk.v1alpha1.SummonFromFleetReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest,
      com.shulkermc.sdk.v1alpha1.SummonFromFleetReply> getSummonFromFleetMethod() {
    io.grpc.MethodDescriptor<com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest, com.shulkermc.sdk.v1alpha1.SummonFromFleetReply> getSummonFromFleetMethod;
    if ((getSummonFromFleetMethod = MincraftServerFleetServiceGrpc.getSummonFromFleetMethod) == null) {
      synchronized (MincraftServerFleetServiceGrpc.class) {
        if ((getSummonFromFleetMethod = MincraftServerFleetServiceGrpc.getSummonFromFleetMethod) == null) {
          MincraftServerFleetServiceGrpc.getSummonFromFleetMethod = getSummonFromFleetMethod =
              io.grpc.MethodDescriptor.<com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest, com.shulkermc.sdk.v1alpha1.SummonFromFleetReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SummonFromFleet"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shulkermc.sdk.v1alpha1.SummonFromFleetReply.getDefaultInstance()))
              .setSchemaDescriptor(new MincraftServerFleetServiceMethodDescriptorSupplier("SummonFromFleet"))
              .build();
        }
      }
    }
    return getSummonFromFleetMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MincraftServerFleetServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MincraftServerFleetServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MincraftServerFleetServiceStub>() {
        @java.lang.Override
        public MincraftServerFleetServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MincraftServerFleetServiceStub(channel, callOptions);
        }
      };
    return MincraftServerFleetServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MincraftServerFleetServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MincraftServerFleetServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MincraftServerFleetServiceBlockingStub>() {
        @java.lang.Override
        public MincraftServerFleetServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MincraftServerFleetServiceBlockingStub(channel, callOptions);
        }
      };
    return MincraftServerFleetServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MincraftServerFleetServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MincraftServerFleetServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MincraftServerFleetServiceFutureStub>() {
        @java.lang.Override
        public MincraftServerFleetServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MincraftServerFleetServiceFutureStub(channel, callOptions);
        }
      };
    return MincraftServerFleetServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void summonFromFleet(com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest request,
        io.grpc.stub.StreamObserver<com.shulkermc.sdk.v1alpha1.SummonFromFleetReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSummonFromFleetMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MincraftServerFleetService.
   */
  public static abstract class MincraftServerFleetServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MincraftServerFleetServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MincraftServerFleetService.
   */
  public static final class MincraftServerFleetServiceStub
      extends io.grpc.stub.AbstractAsyncStub<MincraftServerFleetServiceStub> {
    private MincraftServerFleetServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MincraftServerFleetServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MincraftServerFleetServiceStub(channel, callOptions);
    }

    /**
     */
    public void summonFromFleet(com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest request,
        io.grpc.stub.StreamObserver<com.shulkermc.sdk.v1alpha1.SummonFromFleetReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSummonFromFleetMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MincraftServerFleetService.
   */
  public static final class MincraftServerFleetServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MincraftServerFleetServiceBlockingStub> {
    private MincraftServerFleetServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MincraftServerFleetServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MincraftServerFleetServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.shulkermc.sdk.v1alpha1.SummonFromFleetReply summonFromFleet(com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSummonFromFleetMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MincraftServerFleetService.
   */
  public static final class MincraftServerFleetServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<MincraftServerFleetServiceFutureStub> {
    private MincraftServerFleetServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MincraftServerFleetServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MincraftServerFleetServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shulkermc.sdk.v1alpha1.SummonFromFleetReply> summonFromFleet(
        com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest request) {
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
          serviceImpl.summonFromFleet((com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest) request,
              (io.grpc.stub.StreamObserver<com.shulkermc.sdk.v1alpha1.SummonFromFleetReply>) responseObserver);
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
              com.shulkermc.sdk.v1alpha1.SummonFromFleetRequest,
              com.shulkermc.sdk.v1alpha1.SummonFromFleetReply>(
                service, METHODID_SUMMON_FROM_FLEET)))
        .build();
  }

  private static abstract class MincraftServerFleetServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MincraftServerFleetServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.shulkermc.sdk.v1alpha1.MinecraftserverfleetProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MincraftServerFleetService");
    }
  }

  private static final class MincraftServerFleetServiceFileDescriptorSupplier
      extends MincraftServerFleetServiceBaseDescriptorSupplier {
    MincraftServerFleetServiceFileDescriptorSupplier() {}
  }

  private static final class MincraftServerFleetServiceMethodDescriptorSupplier
      extends MincraftServerFleetServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MincraftServerFleetServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (MincraftServerFleetServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MincraftServerFleetServiceFileDescriptorSupplier())
              .addMethod(getSummonFromFleetMethod())
              .build();
        }
      }
    }
    return result;
  }
}
