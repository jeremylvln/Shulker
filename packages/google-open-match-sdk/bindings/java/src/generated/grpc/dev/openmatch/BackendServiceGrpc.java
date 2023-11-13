package dev.openmatch;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * The BackendService implements APIs to generate matches and handle ticket assignments.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.59.0)",
    comments = "Source: api/backend.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class BackendServiceGrpc {

  private BackendServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "openmatch.BackendService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<dev.openmatch.FetchMatchesRequest,
      dev.openmatch.FetchMatchesResponse> getFetchMatchesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FetchMatches",
      requestType = dev.openmatch.FetchMatchesRequest.class,
      responseType = dev.openmatch.FetchMatchesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<dev.openmatch.FetchMatchesRequest,
      dev.openmatch.FetchMatchesResponse> getFetchMatchesMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.FetchMatchesRequest, dev.openmatch.FetchMatchesResponse> getFetchMatchesMethod;
    if ((getFetchMatchesMethod = BackendServiceGrpc.getFetchMatchesMethod) == null) {
      synchronized (BackendServiceGrpc.class) {
        if ((getFetchMatchesMethod = BackendServiceGrpc.getFetchMatchesMethod) == null) {
          BackendServiceGrpc.getFetchMatchesMethod = getFetchMatchesMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.FetchMatchesRequest, dev.openmatch.FetchMatchesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "FetchMatches"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.FetchMatchesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.FetchMatchesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BackendServiceMethodDescriptorSupplier("FetchMatches"))
              .build();
        }
      }
    }
    return getFetchMatchesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.AssignTicketsRequest,
      dev.openmatch.AssignTicketsResponse> getAssignTicketsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AssignTickets",
      requestType = dev.openmatch.AssignTicketsRequest.class,
      responseType = dev.openmatch.AssignTicketsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.AssignTicketsRequest,
      dev.openmatch.AssignTicketsResponse> getAssignTicketsMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.AssignTicketsRequest, dev.openmatch.AssignTicketsResponse> getAssignTicketsMethod;
    if ((getAssignTicketsMethod = BackendServiceGrpc.getAssignTicketsMethod) == null) {
      synchronized (BackendServiceGrpc.class) {
        if ((getAssignTicketsMethod = BackendServiceGrpc.getAssignTicketsMethod) == null) {
          BackendServiceGrpc.getAssignTicketsMethod = getAssignTicketsMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.AssignTicketsRequest, dev.openmatch.AssignTicketsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AssignTickets"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.AssignTicketsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.AssignTicketsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BackendServiceMethodDescriptorSupplier("AssignTickets"))
              .build();
        }
      }
    }
    return getAssignTicketsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.ReleaseTicketsRequest,
      dev.openmatch.ReleaseTicketsResponse> getReleaseTicketsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ReleaseTickets",
      requestType = dev.openmatch.ReleaseTicketsRequest.class,
      responseType = dev.openmatch.ReleaseTicketsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.ReleaseTicketsRequest,
      dev.openmatch.ReleaseTicketsResponse> getReleaseTicketsMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.ReleaseTicketsRequest, dev.openmatch.ReleaseTicketsResponse> getReleaseTicketsMethod;
    if ((getReleaseTicketsMethod = BackendServiceGrpc.getReleaseTicketsMethod) == null) {
      synchronized (BackendServiceGrpc.class) {
        if ((getReleaseTicketsMethod = BackendServiceGrpc.getReleaseTicketsMethod) == null) {
          BackendServiceGrpc.getReleaseTicketsMethod = getReleaseTicketsMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.ReleaseTicketsRequest, dev.openmatch.ReleaseTicketsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReleaseTickets"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.ReleaseTicketsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.ReleaseTicketsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BackendServiceMethodDescriptorSupplier("ReleaseTickets"))
              .build();
        }
      }
    }
    return getReleaseTicketsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.ReleaseAllTicketsRequest,
      dev.openmatch.ReleaseAllTicketsResponse> getReleaseAllTicketsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ReleaseAllTickets",
      requestType = dev.openmatch.ReleaseAllTicketsRequest.class,
      responseType = dev.openmatch.ReleaseAllTicketsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.ReleaseAllTicketsRequest,
      dev.openmatch.ReleaseAllTicketsResponse> getReleaseAllTicketsMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.ReleaseAllTicketsRequest, dev.openmatch.ReleaseAllTicketsResponse> getReleaseAllTicketsMethod;
    if ((getReleaseAllTicketsMethod = BackendServiceGrpc.getReleaseAllTicketsMethod) == null) {
      synchronized (BackendServiceGrpc.class) {
        if ((getReleaseAllTicketsMethod = BackendServiceGrpc.getReleaseAllTicketsMethod) == null) {
          BackendServiceGrpc.getReleaseAllTicketsMethod = getReleaseAllTicketsMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.ReleaseAllTicketsRequest, dev.openmatch.ReleaseAllTicketsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReleaseAllTickets"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.ReleaseAllTicketsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.ReleaseAllTicketsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BackendServiceMethodDescriptorSupplier("ReleaseAllTickets"))
              .build();
        }
      }
    }
    return getReleaseAllTicketsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BackendServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BackendServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BackendServiceStub>() {
        @java.lang.Override
        public BackendServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BackendServiceStub(channel, callOptions);
        }
      };
    return BackendServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BackendServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BackendServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BackendServiceBlockingStub>() {
        @java.lang.Override
        public BackendServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BackendServiceBlockingStub(channel, callOptions);
        }
      };
    return BackendServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BackendServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BackendServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BackendServiceFutureStub>() {
        @java.lang.Override
        public BackendServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BackendServiceFutureStub(channel, callOptions);
        }
      };
    return BackendServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * The BackendService implements APIs to generate matches and handle ticket assignments.
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * FetchMatches triggers a MatchFunction with the specified MatchProfile and
     * returns a set of matches generated by the Match Making Function, and
     * accepted by the evaluator.
     * Tickets in matches returned by FetchMatches are moved from active to
     * pending, and will not be returned by query.
     * </pre>
     */
    default void fetchMatches(dev.openmatch.FetchMatchesRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.FetchMatchesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFetchMatchesMethod(), responseObserver);
    }

    /**
     * <pre>
     * AssignTickets overwrites the Assignment field of the input TicketIds.
     * </pre>
     */
    default void assignTickets(dev.openmatch.AssignTicketsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.AssignTicketsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAssignTicketsMethod(), responseObserver);
    }

    /**
     * <pre>
     * ReleaseTickets moves tickets from the pending state, to the active state.
     * This enables them to be returned by query, and find different matches.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    default void releaseTickets(dev.openmatch.ReleaseTicketsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.ReleaseTicketsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReleaseTicketsMethod(), responseObserver);
    }

    /**
     * <pre>
     * ReleaseAllTickets moves all tickets from the pending state, to the active
     * state. This enables them to be returned by query, and find different
     * matches.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    default void releaseAllTickets(dev.openmatch.ReleaseAllTicketsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.ReleaseAllTicketsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReleaseAllTicketsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service BackendService.
   * <pre>
   * The BackendService implements APIs to generate matches and handle ticket assignments.
   * </pre>
   */
  public static abstract class BackendServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return BackendServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service BackendService.
   * <pre>
   * The BackendService implements APIs to generate matches and handle ticket assignments.
   * </pre>
   */
  public static final class BackendServiceStub
      extends io.grpc.stub.AbstractAsyncStub<BackendServiceStub> {
    private BackendServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BackendServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BackendServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * FetchMatches triggers a MatchFunction with the specified MatchProfile and
     * returns a set of matches generated by the Match Making Function, and
     * accepted by the evaluator.
     * Tickets in matches returned by FetchMatches are moved from active to
     * pending, and will not be returned by query.
     * </pre>
     */
    public void fetchMatches(dev.openmatch.FetchMatchesRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.FetchMatchesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getFetchMatchesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * AssignTickets overwrites the Assignment field of the input TicketIds.
     * </pre>
     */
    public void assignTickets(dev.openmatch.AssignTicketsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.AssignTicketsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAssignTicketsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * ReleaseTickets moves tickets from the pending state, to the active state.
     * This enables them to be returned by query, and find different matches.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public void releaseTickets(dev.openmatch.ReleaseTicketsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.ReleaseTicketsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReleaseTicketsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * ReleaseAllTickets moves all tickets from the pending state, to the active
     * state. This enables them to be returned by query, and find different
     * matches.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public void releaseAllTickets(dev.openmatch.ReleaseAllTicketsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.ReleaseAllTicketsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReleaseAllTicketsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service BackendService.
   * <pre>
   * The BackendService implements APIs to generate matches and handle ticket assignments.
   * </pre>
   */
  public static final class BackendServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<BackendServiceBlockingStub> {
    private BackendServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BackendServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BackendServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * FetchMatches triggers a MatchFunction with the specified MatchProfile and
     * returns a set of matches generated by the Match Making Function, and
     * accepted by the evaluator.
     * Tickets in matches returned by FetchMatches are moved from active to
     * pending, and will not be returned by query.
     * </pre>
     */
    public java.util.Iterator<dev.openmatch.FetchMatchesResponse> fetchMatches(
        dev.openmatch.FetchMatchesRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getFetchMatchesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * AssignTickets overwrites the Assignment field of the input TicketIds.
     * </pre>
     */
    public dev.openmatch.AssignTicketsResponse assignTickets(dev.openmatch.AssignTicketsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAssignTicketsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * ReleaseTickets moves tickets from the pending state, to the active state.
     * This enables them to be returned by query, and find different matches.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public dev.openmatch.ReleaseTicketsResponse releaseTickets(dev.openmatch.ReleaseTicketsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReleaseTicketsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * ReleaseAllTickets moves all tickets from the pending state, to the active
     * state. This enables them to be returned by query, and find different
     * matches.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public dev.openmatch.ReleaseAllTicketsResponse releaseAllTickets(dev.openmatch.ReleaseAllTicketsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReleaseAllTicketsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service BackendService.
   * <pre>
   * The BackendService implements APIs to generate matches and handle ticket assignments.
   * </pre>
   */
  public static final class BackendServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<BackendServiceFutureStub> {
    private BackendServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BackendServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BackendServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * AssignTickets overwrites the Assignment field of the input TicketIds.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.openmatch.AssignTicketsResponse> assignTickets(
        dev.openmatch.AssignTicketsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAssignTicketsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * ReleaseTickets moves tickets from the pending state, to the active state.
     * This enables them to be returned by query, and find different matches.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.openmatch.ReleaseTicketsResponse> releaseTickets(
        dev.openmatch.ReleaseTicketsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReleaseTicketsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * ReleaseAllTickets moves all tickets from the pending state, to the active
     * state. This enables them to be returned by query, and find different
     * matches.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.openmatch.ReleaseAllTicketsResponse> releaseAllTickets(
        dev.openmatch.ReleaseAllTicketsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReleaseAllTicketsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_FETCH_MATCHES = 0;
  private static final int METHODID_ASSIGN_TICKETS = 1;
  private static final int METHODID_RELEASE_TICKETS = 2;
  private static final int METHODID_RELEASE_ALL_TICKETS = 3;

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
        case METHODID_FETCH_MATCHES:
          serviceImpl.fetchMatches((dev.openmatch.FetchMatchesRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.FetchMatchesResponse>) responseObserver);
          break;
        case METHODID_ASSIGN_TICKETS:
          serviceImpl.assignTickets((dev.openmatch.AssignTicketsRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.AssignTicketsResponse>) responseObserver);
          break;
        case METHODID_RELEASE_TICKETS:
          serviceImpl.releaseTickets((dev.openmatch.ReleaseTicketsRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.ReleaseTicketsResponse>) responseObserver);
          break;
        case METHODID_RELEASE_ALL_TICKETS:
          serviceImpl.releaseAllTickets((dev.openmatch.ReleaseAllTicketsRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.ReleaseAllTicketsResponse>) responseObserver);
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
          getFetchMatchesMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              dev.openmatch.FetchMatchesRequest,
              dev.openmatch.FetchMatchesResponse>(
                service, METHODID_FETCH_MATCHES)))
        .addMethod(
          getAssignTicketsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.AssignTicketsRequest,
              dev.openmatch.AssignTicketsResponse>(
                service, METHODID_ASSIGN_TICKETS)))
        .addMethod(
          getReleaseTicketsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.ReleaseTicketsRequest,
              dev.openmatch.ReleaseTicketsResponse>(
                service, METHODID_RELEASE_TICKETS)))
        .addMethod(
          getReleaseAllTicketsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.ReleaseAllTicketsRequest,
              dev.openmatch.ReleaseAllTicketsResponse>(
                service, METHODID_RELEASE_ALL_TICKETS)))
        .build();
  }

  private static abstract class BackendServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    BackendServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return dev.openmatch.BackendProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("BackendService");
    }
  }

  private static final class BackendServiceFileDescriptorSupplier
      extends BackendServiceBaseDescriptorSupplier {
    BackendServiceFileDescriptorSupplier() {}
  }

  private static final class BackendServiceMethodDescriptorSupplier
      extends BackendServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    BackendServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (BackendServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BackendServiceFileDescriptorSupplier())
              .addMethod(getFetchMatchesMethod())
              .addMethod(getAssignTicketsMethod())
              .addMethod(getReleaseTicketsMethod())
              .addMethod(getReleaseAllTicketsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
