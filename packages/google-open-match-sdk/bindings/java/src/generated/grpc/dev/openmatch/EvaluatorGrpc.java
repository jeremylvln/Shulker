package dev.openmatch;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * The Evaluator service implements APIs used to evaluate and shortlist matches proposed by MMFs.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.59.0)",
    comments = "Source: api/evaluator.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class EvaluatorGrpc {

  private EvaluatorGrpc() {}

  public static final java.lang.String SERVICE_NAME = "openmatch.Evaluator";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<dev.openmatch.EvaluateRequest,
      dev.openmatch.EvaluateResponse> getEvaluateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Evaluate",
      requestType = dev.openmatch.EvaluateRequest.class,
      responseType = dev.openmatch.EvaluateResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<dev.openmatch.EvaluateRequest,
      dev.openmatch.EvaluateResponse> getEvaluateMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.EvaluateRequest, dev.openmatch.EvaluateResponse> getEvaluateMethod;
    if ((getEvaluateMethod = EvaluatorGrpc.getEvaluateMethod) == null) {
      synchronized (EvaluatorGrpc.class) {
        if ((getEvaluateMethod = EvaluatorGrpc.getEvaluateMethod) == null) {
          EvaluatorGrpc.getEvaluateMethod = getEvaluateMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.EvaluateRequest, dev.openmatch.EvaluateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Evaluate"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.EvaluateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.EvaluateResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EvaluatorMethodDescriptorSupplier("Evaluate"))
              .build();
        }
      }
    }
    return getEvaluateMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EvaluatorStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EvaluatorStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EvaluatorStub>() {
        @java.lang.Override
        public EvaluatorStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EvaluatorStub(channel, callOptions);
        }
      };
    return EvaluatorStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EvaluatorBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EvaluatorBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EvaluatorBlockingStub>() {
        @java.lang.Override
        public EvaluatorBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EvaluatorBlockingStub(channel, callOptions);
        }
      };
    return EvaluatorBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EvaluatorFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EvaluatorFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EvaluatorFutureStub>() {
        @java.lang.Override
        public EvaluatorFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EvaluatorFutureStub(channel, callOptions);
        }
      };
    return EvaluatorFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * The Evaluator service implements APIs used to evaluate and shortlist matches proposed by MMFs.
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Evaluate evaluates a list of proposed matches based on quality, collision status, and etc, then shortlist the matches and returns the final results.
     * </pre>
     */
    default io.grpc.stub.StreamObserver<dev.openmatch.EvaluateRequest> evaluate(
        io.grpc.stub.StreamObserver<dev.openmatch.EvaluateResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getEvaluateMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service Evaluator.
   * <pre>
   * The Evaluator service implements APIs used to evaluate and shortlist matches proposed by MMFs.
   * </pre>
   */
  public static abstract class EvaluatorImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return EvaluatorGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service Evaluator.
   * <pre>
   * The Evaluator service implements APIs used to evaluate and shortlist matches proposed by MMFs.
   * </pre>
   */
  public static final class EvaluatorStub
      extends io.grpc.stub.AbstractAsyncStub<EvaluatorStub> {
    private EvaluatorStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EvaluatorStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EvaluatorStub(channel, callOptions);
    }

    /**
     * <pre>
     * Evaluate evaluates a list of proposed matches based on quality, collision status, and etc, then shortlist the matches and returns the final results.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<dev.openmatch.EvaluateRequest> evaluate(
        io.grpc.stub.StreamObserver<dev.openmatch.EvaluateResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getEvaluateMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service Evaluator.
   * <pre>
   * The Evaluator service implements APIs used to evaluate and shortlist matches proposed by MMFs.
   * </pre>
   */
  public static final class EvaluatorBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<EvaluatorBlockingStub> {
    private EvaluatorBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EvaluatorBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EvaluatorBlockingStub(channel, callOptions);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service Evaluator.
   * <pre>
   * The Evaluator service implements APIs used to evaluate and shortlist matches proposed by MMFs.
   * </pre>
   */
  public static final class EvaluatorFutureStub
      extends io.grpc.stub.AbstractFutureStub<EvaluatorFutureStub> {
    private EvaluatorFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EvaluatorFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EvaluatorFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_EVALUATE = 0;

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
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_EVALUATE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.evaluate(
              (io.grpc.stub.StreamObserver<dev.openmatch.EvaluateResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getEvaluateMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              dev.openmatch.EvaluateRequest,
              dev.openmatch.EvaluateResponse>(
                service, METHODID_EVALUATE)))
        .build();
  }

  private static abstract class EvaluatorBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EvaluatorBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return dev.openmatch.EvaluatorProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Evaluator");
    }
  }

  private static final class EvaluatorFileDescriptorSupplier
      extends EvaluatorBaseDescriptorSupplier {
    EvaluatorFileDescriptorSupplier() {}
  }

  private static final class EvaluatorMethodDescriptorSupplier
      extends EvaluatorBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    EvaluatorMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (EvaluatorGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EvaluatorFileDescriptorSupplier())
              .addMethod(getEvaluateMethod())
              .build();
        }
      }
    }
    return result;
  }
}
