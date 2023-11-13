package dev.openmatch;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * The QueryService service implements helper APIs for Match Function to query Tickets from state storage.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.59.0)",
    comments = "Source: api/query.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class QueryServiceGrpc {

  private QueryServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "openmatch.QueryService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<dev.openmatch.QueryTicketsRequest,
      dev.openmatch.QueryTicketsResponse> getQueryTicketsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "QueryTickets",
      requestType = dev.openmatch.QueryTicketsRequest.class,
      responseType = dev.openmatch.QueryTicketsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<dev.openmatch.QueryTicketsRequest,
      dev.openmatch.QueryTicketsResponse> getQueryTicketsMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.QueryTicketsRequest, dev.openmatch.QueryTicketsResponse> getQueryTicketsMethod;
    if ((getQueryTicketsMethod = QueryServiceGrpc.getQueryTicketsMethod) == null) {
      synchronized (QueryServiceGrpc.class) {
        if ((getQueryTicketsMethod = QueryServiceGrpc.getQueryTicketsMethod) == null) {
          QueryServiceGrpc.getQueryTicketsMethod = getQueryTicketsMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.QueryTicketsRequest, dev.openmatch.QueryTicketsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "QueryTickets"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.QueryTicketsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.QueryTicketsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new QueryServiceMethodDescriptorSupplier("QueryTickets"))
              .build();
        }
      }
    }
    return getQueryTicketsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.QueryTicketIdsRequest,
      dev.openmatch.QueryTicketIdsResponse> getQueryTicketIdsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "QueryTicketIds",
      requestType = dev.openmatch.QueryTicketIdsRequest.class,
      responseType = dev.openmatch.QueryTicketIdsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<dev.openmatch.QueryTicketIdsRequest,
      dev.openmatch.QueryTicketIdsResponse> getQueryTicketIdsMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.QueryTicketIdsRequest, dev.openmatch.QueryTicketIdsResponse> getQueryTicketIdsMethod;
    if ((getQueryTicketIdsMethod = QueryServiceGrpc.getQueryTicketIdsMethod) == null) {
      synchronized (QueryServiceGrpc.class) {
        if ((getQueryTicketIdsMethod = QueryServiceGrpc.getQueryTicketIdsMethod) == null) {
          QueryServiceGrpc.getQueryTicketIdsMethod = getQueryTicketIdsMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.QueryTicketIdsRequest, dev.openmatch.QueryTicketIdsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "QueryTicketIds"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.QueryTicketIdsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.QueryTicketIdsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new QueryServiceMethodDescriptorSupplier("QueryTicketIds"))
              .build();
        }
      }
    }
    return getQueryTicketIdsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.QueryBackfillsRequest,
      dev.openmatch.QueryBackfillsResponse> getQueryBackfillsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "QueryBackfills",
      requestType = dev.openmatch.QueryBackfillsRequest.class,
      responseType = dev.openmatch.QueryBackfillsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<dev.openmatch.QueryBackfillsRequest,
      dev.openmatch.QueryBackfillsResponse> getQueryBackfillsMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.QueryBackfillsRequest, dev.openmatch.QueryBackfillsResponse> getQueryBackfillsMethod;
    if ((getQueryBackfillsMethod = QueryServiceGrpc.getQueryBackfillsMethod) == null) {
      synchronized (QueryServiceGrpc.class) {
        if ((getQueryBackfillsMethod = QueryServiceGrpc.getQueryBackfillsMethod) == null) {
          QueryServiceGrpc.getQueryBackfillsMethod = getQueryBackfillsMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.QueryBackfillsRequest, dev.openmatch.QueryBackfillsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "QueryBackfills"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.QueryBackfillsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.QueryBackfillsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new QueryServiceMethodDescriptorSupplier("QueryBackfills"))
              .build();
        }
      }
    }
    return getQueryBackfillsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static QueryServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<QueryServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<QueryServiceStub>() {
        @java.lang.Override
        public QueryServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new QueryServiceStub(channel, callOptions);
        }
      };
    return QueryServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static QueryServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<QueryServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<QueryServiceBlockingStub>() {
        @java.lang.Override
        public QueryServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new QueryServiceBlockingStub(channel, callOptions);
        }
      };
    return QueryServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static QueryServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<QueryServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<QueryServiceFutureStub>() {
        @java.lang.Override
        public QueryServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new QueryServiceFutureStub(channel, callOptions);
        }
      };
    return QueryServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * The QueryService service implements helper APIs for Match Function to query Tickets from state storage.
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * QueryTickets gets a list of Tickets that match all Filters of the input Pool.
     *   - If the Pool contains no Filters, QueryTickets will return all Tickets in the state storage.
     * QueryTickets pages the Tickets by `queryPageSize` and stream back responses.
     *   - queryPageSize is default to 1000 if not set, and has a minimum of 10 and maximum of 10000.
     * </pre>
     */
    default void queryTickets(dev.openmatch.QueryTicketsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.QueryTicketsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getQueryTicketsMethod(), responseObserver);
    }

    /**
     * <pre>
     * QueryTicketIds gets the list of TicketIDs that meet all the filtering criteria requested by the pool.
     *   - If the Pool contains no Filters, QueryTicketIds will return all TicketIDs in the state storage.
     * QueryTicketIds pages the TicketIDs by `queryPageSize` and stream back responses.
     *   - queryPageSize is default to 1000 if not set, and has a minimum of 10 and maximum of 10000.
     * </pre>
     */
    default void queryTicketIds(dev.openmatch.QueryTicketIdsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.QueryTicketIdsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getQueryTicketIdsMethod(), responseObserver);
    }

    /**
     * <pre>
     * QueryBackfills gets a list of Backfills.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    default void queryBackfills(dev.openmatch.QueryBackfillsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.QueryBackfillsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getQueryBackfillsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service QueryService.
   * <pre>
   * The QueryService service implements helper APIs for Match Function to query Tickets from state storage.
   * </pre>
   */
  public static abstract class QueryServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return QueryServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service QueryService.
   * <pre>
   * The QueryService service implements helper APIs for Match Function to query Tickets from state storage.
   * </pre>
   */
  public static final class QueryServiceStub
      extends io.grpc.stub.AbstractAsyncStub<QueryServiceStub> {
    private QueryServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new QueryServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * QueryTickets gets a list of Tickets that match all Filters of the input Pool.
     *   - If the Pool contains no Filters, QueryTickets will return all Tickets in the state storage.
     * QueryTickets pages the Tickets by `queryPageSize` and stream back responses.
     *   - queryPageSize is default to 1000 if not set, and has a minimum of 10 and maximum of 10000.
     * </pre>
     */
    public void queryTickets(dev.openmatch.QueryTicketsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.QueryTicketsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getQueryTicketsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * QueryTicketIds gets the list of TicketIDs that meet all the filtering criteria requested by the pool.
     *   - If the Pool contains no Filters, QueryTicketIds will return all TicketIDs in the state storage.
     * QueryTicketIds pages the TicketIDs by `queryPageSize` and stream back responses.
     *   - queryPageSize is default to 1000 if not set, and has a minimum of 10 and maximum of 10000.
     * </pre>
     */
    public void queryTicketIds(dev.openmatch.QueryTicketIdsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.QueryTicketIdsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getQueryTicketIdsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * QueryBackfills gets a list of Backfills.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public void queryBackfills(dev.openmatch.QueryBackfillsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.QueryBackfillsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getQueryBackfillsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service QueryService.
   * <pre>
   * The QueryService service implements helper APIs for Match Function to query Tickets from state storage.
   * </pre>
   */
  public static final class QueryServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<QueryServiceBlockingStub> {
    private QueryServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new QueryServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * QueryTickets gets a list of Tickets that match all Filters of the input Pool.
     *   - If the Pool contains no Filters, QueryTickets will return all Tickets in the state storage.
     * QueryTickets pages the Tickets by `queryPageSize` and stream back responses.
     *   - queryPageSize is default to 1000 if not set, and has a minimum of 10 and maximum of 10000.
     * </pre>
     */
    public java.util.Iterator<dev.openmatch.QueryTicketsResponse> queryTickets(
        dev.openmatch.QueryTicketsRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getQueryTicketsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * QueryTicketIds gets the list of TicketIDs that meet all the filtering criteria requested by the pool.
     *   - If the Pool contains no Filters, QueryTicketIds will return all TicketIDs in the state storage.
     * QueryTicketIds pages the TicketIDs by `queryPageSize` and stream back responses.
     *   - queryPageSize is default to 1000 if not set, and has a minimum of 10 and maximum of 10000.
     * </pre>
     */
    public java.util.Iterator<dev.openmatch.QueryTicketIdsResponse> queryTicketIds(
        dev.openmatch.QueryTicketIdsRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getQueryTicketIdsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * QueryBackfills gets a list of Backfills.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public java.util.Iterator<dev.openmatch.QueryBackfillsResponse> queryBackfills(
        dev.openmatch.QueryBackfillsRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getQueryBackfillsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service QueryService.
   * <pre>
   * The QueryService service implements helper APIs for Match Function to query Tickets from state storage.
   * </pre>
   */
  public static final class QueryServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<QueryServiceFutureStub> {
    private QueryServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new QueryServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_QUERY_TICKETS = 0;
  private static final int METHODID_QUERY_TICKET_IDS = 1;
  private static final int METHODID_QUERY_BACKFILLS = 2;

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
        case METHODID_QUERY_TICKETS:
          serviceImpl.queryTickets((dev.openmatch.QueryTicketsRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.QueryTicketsResponse>) responseObserver);
          break;
        case METHODID_QUERY_TICKET_IDS:
          serviceImpl.queryTicketIds((dev.openmatch.QueryTicketIdsRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.QueryTicketIdsResponse>) responseObserver);
          break;
        case METHODID_QUERY_BACKFILLS:
          serviceImpl.queryBackfills((dev.openmatch.QueryBackfillsRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.QueryBackfillsResponse>) responseObserver);
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
          getQueryTicketsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              dev.openmatch.QueryTicketsRequest,
              dev.openmatch.QueryTicketsResponse>(
                service, METHODID_QUERY_TICKETS)))
        .addMethod(
          getQueryTicketIdsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              dev.openmatch.QueryTicketIdsRequest,
              dev.openmatch.QueryTicketIdsResponse>(
                service, METHODID_QUERY_TICKET_IDS)))
        .addMethod(
          getQueryBackfillsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              dev.openmatch.QueryBackfillsRequest,
              dev.openmatch.QueryBackfillsResponse>(
                service, METHODID_QUERY_BACKFILLS)))
        .build();
  }

  private static abstract class QueryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    QueryServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return dev.openmatch.QueryProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("QueryService");
    }
  }

  private static final class QueryServiceFileDescriptorSupplier
      extends QueryServiceBaseDescriptorSupplier {
    QueryServiceFileDescriptorSupplier() {}
  }

  private static final class QueryServiceMethodDescriptorSupplier
      extends QueryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    QueryServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (QueryServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new QueryServiceFileDescriptorSupplier())
              .addMethod(getQueryTicketsMethod())
              .addMethod(getQueryTicketIdsMethod())
              .addMethod(getQueryBackfillsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
