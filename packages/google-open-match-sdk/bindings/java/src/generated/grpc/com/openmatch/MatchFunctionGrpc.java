package com.openmatch;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * The MatchFunction service implements APIs to run user-defined matchmaking logics.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.59.0)",
    comments = "Source: api/match-function.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class MatchFunctionGrpc {

  private MatchFunctionGrpc() {}

  public static final java.lang.String SERVICE_NAME = "openmatch.MatchFunction";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.openmatch.RunRequest,
      com.openmatch.RunResponse> getRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Run",
      requestType = com.openmatch.RunRequest.class,
      responseType = com.openmatch.RunResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.openmatch.RunRequest,
      com.openmatch.RunResponse> getRunMethod() {
    io.grpc.MethodDescriptor<com.openmatch.RunRequest, com.openmatch.RunResponse> getRunMethod;
    if ((getRunMethod = MatchFunctionGrpc.getRunMethod) == null) {
      synchronized (MatchFunctionGrpc.class) {
        if ((getRunMethod = MatchFunctionGrpc.getRunMethod) == null) {
          MatchFunctionGrpc.getRunMethod = getRunMethod =
              io.grpc.MethodDescriptor.<com.openmatch.RunRequest, com.openmatch.RunResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Run"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.RunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.RunResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MatchFunctionMethodDescriptorSupplier("Run"))
              .build();
        }
      }
    }
    return getRunMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MatchFunctionStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MatchFunctionStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MatchFunctionStub>() {
        @java.lang.Override
        public MatchFunctionStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MatchFunctionStub(channel, callOptions);
        }
      };
    return MatchFunctionStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MatchFunctionBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MatchFunctionBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MatchFunctionBlockingStub>() {
        @java.lang.Override
        public MatchFunctionBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MatchFunctionBlockingStub(channel, callOptions);
        }
      };
    return MatchFunctionBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MatchFunctionFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MatchFunctionFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MatchFunctionFutureStub>() {
        @java.lang.Override
        public MatchFunctionFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MatchFunctionFutureStub(channel, callOptions);
        }
      };
    return MatchFunctionFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * The MatchFunction service implements APIs to run user-defined matchmaking logics.
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * DO NOT CALL THIS FUNCTION MANUALLY. USE backend.FetchMatches INSTEAD.
     * Run pulls Tickets that satisfy Profile constraints from QueryService,
     * runs matchmaking logic against them, then constructs and streams back
     * match candidates to the Backend service.
     * </pre>
     */
    default void run(com.openmatch.RunRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.RunResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRunMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MatchFunction.
   * <pre>
   * The MatchFunction service implements APIs to run user-defined matchmaking logics.
   * </pre>
   */
  public static abstract class MatchFunctionImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MatchFunctionGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MatchFunction.
   * <pre>
   * The MatchFunction service implements APIs to run user-defined matchmaking logics.
   * </pre>
   */
  public static final class MatchFunctionStub
      extends io.grpc.stub.AbstractAsyncStub<MatchFunctionStub> {
    private MatchFunctionStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MatchFunctionStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MatchFunctionStub(channel, callOptions);
    }

    /**
     * <pre>
     * DO NOT CALL THIS FUNCTION MANUALLY. USE backend.FetchMatches INSTEAD.
     * Run pulls Tickets that satisfy Profile constraints from QueryService,
     * runs matchmaking logic against them, then constructs and streams back
     * match candidates to the Backend service.
     * </pre>
     */
    public void run(com.openmatch.RunRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.RunResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getRunMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MatchFunction.
   * <pre>
   * The MatchFunction service implements APIs to run user-defined matchmaking logics.
   * </pre>
   */
  public static final class MatchFunctionBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MatchFunctionBlockingStub> {
    private MatchFunctionBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MatchFunctionBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MatchFunctionBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * DO NOT CALL THIS FUNCTION MANUALLY. USE backend.FetchMatches INSTEAD.
     * Run pulls Tickets that satisfy Profile constraints from QueryService,
     * runs matchmaking logic against them, then constructs and streams back
     * match candidates to the Backend service.
     * </pre>
     */
    public java.util.Iterator<com.openmatch.RunResponse> run(
        com.openmatch.RunRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getRunMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MatchFunction.
   * <pre>
   * The MatchFunction service implements APIs to run user-defined matchmaking logics.
   * </pre>
   */
  public static final class MatchFunctionFutureStub
      extends io.grpc.stub.AbstractFutureStub<MatchFunctionFutureStub> {
    private MatchFunctionFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MatchFunctionFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MatchFunctionFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_RUN = 0;

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
        case METHODID_RUN:
          serviceImpl.run((com.openmatch.RunRequest) request,
              (io.grpc.stub.StreamObserver<com.openmatch.RunResponse>) responseObserver);
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
          getRunMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              com.openmatch.RunRequest,
              com.openmatch.RunResponse>(
                service, METHODID_RUN)))
        .build();
  }

  private static abstract class MatchFunctionBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MatchFunctionBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.openmatch.MatchFunctionProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MatchFunction");
    }
  }

  private static final class MatchFunctionFileDescriptorSupplier
      extends MatchFunctionBaseDescriptorSupplier {
    MatchFunctionFileDescriptorSupplier() {}
  }

  private static final class MatchFunctionMethodDescriptorSupplier
      extends MatchFunctionBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MatchFunctionMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (MatchFunctionGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MatchFunctionFileDescriptorSupplier())
              .addMethod(getRunMethod())
              .build();
        }
      }
    }
    return result;
  }
}
