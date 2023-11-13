package dev.openmatch;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * The FrontendService implements APIs to manage and query status of a Tickets.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.59.0)",
    comments = "Source: api/frontend.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class FrontendServiceGrpc {

  private FrontendServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "openmatch.FrontendService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<dev.openmatch.CreateTicketRequest,
      dev.openmatch.Ticket> getCreateTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateTicket",
      requestType = dev.openmatch.CreateTicketRequest.class,
      responseType = dev.openmatch.Ticket.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.CreateTicketRequest,
      dev.openmatch.Ticket> getCreateTicketMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.CreateTicketRequest, dev.openmatch.Ticket> getCreateTicketMethod;
    if ((getCreateTicketMethod = FrontendServiceGrpc.getCreateTicketMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getCreateTicketMethod = FrontendServiceGrpc.getCreateTicketMethod) == null) {
          FrontendServiceGrpc.getCreateTicketMethod = getCreateTicketMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.CreateTicketRequest, dev.openmatch.Ticket>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.CreateTicketRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.Ticket.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("CreateTicket"))
              .build();
        }
      }
    }
    return getCreateTicketMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.DeleteTicketRequest,
      com.google.protobuf.Empty> getDeleteTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteTicket",
      requestType = dev.openmatch.DeleteTicketRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.DeleteTicketRequest,
      com.google.protobuf.Empty> getDeleteTicketMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.DeleteTicketRequest, com.google.protobuf.Empty> getDeleteTicketMethod;
    if ((getDeleteTicketMethod = FrontendServiceGrpc.getDeleteTicketMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getDeleteTicketMethod = FrontendServiceGrpc.getDeleteTicketMethod) == null) {
          FrontendServiceGrpc.getDeleteTicketMethod = getDeleteTicketMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.DeleteTicketRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.DeleteTicketRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("DeleteTicket"))
              .build();
        }
      }
    }
    return getDeleteTicketMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.GetTicketRequest,
      dev.openmatch.Ticket> getGetTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTicket",
      requestType = dev.openmatch.GetTicketRequest.class,
      responseType = dev.openmatch.Ticket.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.GetTicketRequest,
      dev.openmatch.Ticket> getGetTicketMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.GetTicketRequest, dev.openmatch.Ticket> getGetTicketMethod;
    if ((getGetTicketMethod = FrontendServiceGrpc.getGetTicketMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getGetTicketMethod = FrontendServiceGrpc.getGetTicketMethod) == null) {
          FrontendServiceGrpc.getGetTicketMethod = getGetTicketMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.GetTicketRequest, dev.openmatch.Ticket>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.GetTicketRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.Ticket.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("GetTicket"))
              .build();
        }
      }
    }
    return getGetTicketMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.WatchAssignmentsRequest,
      dev.openmatch.WatchAssignmentsResponse> getWatchAssignmentsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WatchAssignments",
      requestType = dev.openmatch.WatchAssignmentsRequest.class,
      responseType = dev.openmatch.WatchAssignmentsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<dev.openmatch.WatchAssignmentsRequest,
      dev.openmatch.WatchAssignmentsResponse> getWatchAssignmentsMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.WatchAssignmentsRequest, dev.openmatch.WatchAssignmentsResponse> getWatchAssignmentsMethod;
    if ((getWatchAssignmentsMethod = FrontendServiceGrpc.getWatchAssignmentsMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getWatchAssignmentsMethod = FrontendServiceGrpc.getWatchAssignmentsMethod) == null) {
          FrontendServiceGrpc.getWatchAssignmentsMethod = getWatchAssignmentsMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.WatchAssignmentsRequest, dev.openmatch.WatchAssignmentsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WatchAssignments"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.WatchAssignmentsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.WatchAssignmentsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("WatchAssignments"))
              .build();
        }
      }
    }
    return getWatchAssignmentsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.AcknowledgeBackfillRequest,
      dev.openmatch.AcknowledgeBackfillResponse> getAcknowledgeBackfillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AcknowledgeBackfill",
      requestType = dev.openmatch.AcknowledgeBackfillRequest.class,
      responseType = dev.openmatch.AcknowledgeBackfillResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.AcknowledgeBackfillRequest,
      dev.openmatch.AcknowledgeBackfillResponse> getAcknowledgeBackfillMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.AcknowledgeBackfillRequest, dev.openmatch.AcknowledgeBackfillResponse> getAcknowledgeBackfillMethod;
    if ((getAcknowledgeBackfillMethod = FrontendServiceGrpc.getAcknowledgeBackfillMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getAcknowledgeBackfillMethod = FrontendServiceGrpc.getAcknowledgeBackfillMethod) == null) {
          FrontendServiceGrpc.getAcknowledgeBackfillMethod = getAcknowledgeBackfillMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.AcknowledgeBackfillRequest, dev.openmatch.AcknowledgeBackfillResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AcknowledgeBackfill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.AcknowledgeBackfillRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.AcknowledgeBackfillResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("AcknowledgeBackfill"))
              .build();
        }
      }
    }
    return getAcknowledgeBackfillMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.CreateBackfillRequest,
      dev.openmatch.Backfill> getCreateBackfillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateBackfill",
      requestType = dev.openmatch.CreateBackfillRequest.class,
      responseType = dev.openmatch.Backfill.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.CreateBackfillRequest,
      dev.openmatch.Backfill> getCreateBackfillMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.CreateBackfillRequest, dev.openmatch.Backfill> getCreateBackfillMethod;
    if ((getCreateBackfillMethod = FrontendServiceGrpc.getCreateBackfillMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getCreateBackfillMethod = FrontendServiceGrpc.getCreateBackfillMethod) == null) {
          FrontendServiceGrpc.getCreateBackfillMethod = getCreateBackfillMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.CreateBackfillRequest, dev.openmatch.Backfill>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateBackfill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.CreateBackfillRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.Backfill.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("CreateBackfill"))
              .build();
        }
      }
    }
    return getCreateBackfillMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.DeleteBackfillRequest,
      com.google.protobuf.Empty> getDeleteBackfillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteBackfill",
      requestType = dev.openmatch.DeleteBackfillRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.DeleteBackfillRequest,
      com.google.protobuf.Empty> getDeleteBackfillMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.DeleteBackfillRequest, com.google.protobuf.Empty> getDeleteBackfillMethod;
    if ((getDeleteBackfillMethod = FrontendServiceGrpc.getDeleteBackfillMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getDeleteBackfillMethod = FrontendServiceGrpc.getDeleteBackfillMethod) == null) {
          FrontendServiceGrpc.getDeleteBackfillMethod = getDeleteBackfillMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.DeleteBackfillRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteBackfill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.DeleteBackfillRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("DeleteBackfill"))
              .build();
        }
      }
    }
    return getDeleteBackfillMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.GetBackfillRequest,
      dev.openmatch.Backfill> getGetBackfillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetBackfill",
      requestType = dev.openmatch.GetBackfillRequest.class,
      responseType = dev.openmatch.Backfill.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.GetBackfillRequest,
      dev.openmatch.Backfill> getGetBackfillMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.GetBackfillRequest, dev.openmatch.Backfill> getGetBackfillMethod;
    if ((getGetBackfillMethod = FrontendServiceGrpc.getGetBackfillMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getGetBackfillMethod = FrontendServiceGrpc.getGetBackfillMethod) == null) {
          FrontendServiceGrpc.getGetBackfillMethod = getGetBackfillMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.GetBackfillRequest, dev.openmatch.Backfill>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetBackfill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.GetBackfillRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.Backfill.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("GetBackfill"))
              .build();
        }
      }
    }
    return getGetBackfillMethod;
  }

  private static volatile io.grpc.MethodDescriptor<dev.openmatch.UpdateBackfillRequest,
      dev.openmatch.Backfill> getUpdateBackfillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateBackfill",
      requestType = dev.openmatch.UpdateBackfillRequest.class,
      responseType = dev.openmatch.Backfill.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dev.openmatch.UpdateBackfillRequest,
      dev.openmatch.Backfill> getUpdateBackfillMethod() {
    io.grpc.MethodDescriptor<dev.openmatch.UpdateBackfillRequest, dev.openmatch.Backfill> getUpdateBackfillMethod;
    if ((getUpdateBackfillMethod = FrontendServiceGrpc.getUpdateBackfillMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getUpdateBackfillMethod = FrontendServiceGrpc.getUpdateBackfillMethod) == null) {
          FrontendServiceGrpc.getUpdateBackfillMethod = getUpdateBackfillMethod =
              io.grpc.MethodDescriptor.<dev.openmatch.UpdateBackfillRequest, dev.openmatch.Backfill>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateBackfill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.UpdateBackfillRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dev.openmatch.Backfill.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("UpdateBackfill"))
              .build();
        }
      }
    }
    return getUpdateBackfillMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static FrontendServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FrontendServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FrontendServiceStub>() {
        @java.lang.Override
        public FrontendServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FrontendServiceStub(channel, callOptions);
        }
      };
    return FrontendServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static FrontendServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FrontendServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FrontendServiceBlockingStub>() {
        @java.lang.Override
        public FrontendServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FrontendServiceBlockingStub(channel, callOptions);
        }
      };
    return FrontendServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static FrontendServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FrontendServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FrontendServiceFutureStub>() {
        @java.lang.Override
        public FrontendServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FrontendServiceFutureStub(channel, callOptions);
        }
      };
    return FrontendServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * The FrontendService implements APIs to manage and query status of a Tickets.
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * CreateTicket assigns an unique TicketId to the input Ticket and record it in state storage.
     * A ticket is considered as ready for matchmaking once it is created.
     *   - If a TicketId exists in a Ticket request, an auto-generated TicketId will override this field.
     *   - If SearchFields exist in a Ticket, CreateTicket will also index these fields such that one can query the ticket with query.QueryTickets function.
     * </pre>
     */
    default void createTicket(dev.openmatch.CreateTicketRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.Ticket> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateTicketMethod(), responseObserver);
    }

    /**
     * <pre>
     * DeleteTicket immediately stops Open Match from using the Ticket for matchmaking and removes the Ticket from state storage.
     * The client should delete the Ticket when finished matchmaking with it. 
     * </pre>
     */
    default void deleteTicket(dev.openmatch.DeleteTicketRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteTicketMethod(), responseObserver);
    }

    /**
     * <pre>
     * GetTicket get the Ticket associated with the specified TicketId.
     * </pre>
     */
    default void getTicket(dev.openmatch.GetTicketRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.Ticket> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTicketMethod(), responseObserver);
    }

    /**
     * <pre>
     * WatchAssignments stream back Assignment of the specified TicketId if it is updated.
     *   - If the Assignment is not updated, GetAssignment will retry using the configured backoff strategy. 
     * </pre>
     */
    default void watchAssignments(dev.openmatch.WatchAssignmentsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.WatchAssignmentsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWatchAssignmentsMethod(), responseObserver);
    }

    /**
     * <pre>
     * AcknowledgeBackfill is used to notify OpenMatch about GameServer connection info
     * This triggers an assignment process.
     * BETA FEATURE WARNING: This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    default void acknowledgeBackfill(dev.openmatch.AcknowledgeBackfillRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.AcknowledgeBackfillResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAcknowledgeBackfillMethod(), responseObserver);
    }

    /**
     * <pre>
     * CreateBackfill creates a new Backfill object.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    default void createBackfill(dev.openmatch.CreateBackfillRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.Backfill> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateBackfillMethod(), responseObserver);
    }

    /**
     * <pre>
     * DeleteBackfill receives a backfill ID and deletes its resource.
     * Any tickets waiting for this backfill will be returned to the active pool, no longer pending.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    default void deleteBackfill(dev.openmatch.DeleteBackfillRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteBackfillMethod(), responseObserver);
    }

    /**
     * <pre>
     * GetBackfill returns a backfill object by its ID.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    default void getBackfill(dev.openmatch.GetBackfillRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.Backfill> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetBackfillMethod(), responseObserver);
    }

    /**
     * <pre>
     * UpdateBackfill updates search_fields and extensions for the backfill with the provided id.
     * Any tickets waiting for this backfill will be returned to the active pool, no longer pending.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    default void updateBackfill(dev.openmatch.UpdateBackfillRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.Backfill> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateBackfillMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service FrontendService.
   * <pre>
   * The FrontendService implements APIs to manage and query status of a Tickets.
   * </pre>
   */
  public static abstract class FrontendServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return FrontendServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service FrontendService.
   * <pre>
   * The FrontendService implements APIs to manage and query status of a Tickets.
   * </pre>
   */
  public static final class FrontendServiceStub
      extends io.grpc.stub.AbstractAsyncStub<FrontendServiceStub> {
    private FrontendServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FrontendServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FrontendServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * CreateTicket assigns an unique TicketId to the input Ticket and record it in state storage.
     * A ticket is considered as ready for matchmaking once it is created.
     *   - If a TicketId exists in a Ticket request, an auto-generated TicketId will override this field.
     *   - If SearchFields exist in a Ticket, CreateTicket will also index these fields such that one can query the ticket with query.QueryTickets function.
     * </pre>
     */
    public void createTicket(dev.openmatch.CreateTicketRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.Ticket> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateTicketMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * DeleteTicket immediately stops Open Match from using the Ticket for matchmaking and removes the Ticket from state storage.
     * The client should delete the Ticket when finished matchmaking with it. 
     * </pre>
     */
    public void deleteTicket(dev.openmatch.DeleteTicketRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteTicketMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * GetTicket get the Ticket associated with the specified TicketId.
     * </pre>
     */
    public void getTicket(dev.openmatch.GetTicketRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.Ticket> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTicketMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * WatchAssignments stream back Assignment of the specified TicketId if it is updated.
     *   - If the Assignment is not updated, GetAssignment will retry using the configured backoff strategy. 
     * </pre>
     */
    public void watchAssignments(dev.openmatch.WatchAssignmentsRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.WatchAssignmentsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getWatchAssignmentsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * AcknowledgeBackfill is used to notify OpenMatch about GameServer connection info
     * This triggers an assignment process.
     * BETA FEATURE WARNING: This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public void acknowledgeBackfill(dev.openmatch.AcknowledgeBackfillRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.AcknowledgeBackfillResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAcknowledgeBackfillMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * CreateBackfill creates a new Backfill object.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public void createBackfill(dev.openmatch.CreateBackfillRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.Backfill> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateBackfillMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * DeleteBackfill receives a backfill ID and deletes its resource.
     * Any tickets waiting for this backfill will be returned to the active pool, no longer pending.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public void deleteBackfill(dev.openmatch.DeleteBackfillRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteBackfillMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * GetBackfill returns a backfill object by its ID.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public void getBackfill(dev.openmatch.GetBackfillRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.Backfill> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetBackfillMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * UpdateBackfill updates search_fields and extensions for the backfill with the provided id.
     * Any tickets waiting for this backfill will be returned to the active pool, no longer pending.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public void updateBackfill(dev.openmatch.UpdateBackfillRequest request,
        io.grpc.stub.StreamObserver<dev.openmatch.Backfill> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateBackfillMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service FrontendService.
   * <pre>
   * The FrontendService implements APIs to manage and query status of a Tickets.
   * </pre>
   */
  public static final class FrontendServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<FrontendServiceBlockingStub> {
    private FrontendServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FrontendServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FrontendServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * CreateTicket assigns an unique TicketId to the input Ticket and record it in state storage.
     * A ticket is considered as ready for matchmaking once it is created.
     *   - If a TicketId exists in a Ticket request, an auto-generated TicketId will override this field.
     *   - If SearchFields exist in a Ticket, CreateTicket will also index these fields such that one can query the ticket with query.QueryTickets function.
     * </pre>
     */
    public dev.openmatch.Ticket createTicket(dev.openmatch.CreateTicketRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateTicketMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * DeleteTicket immediately stops Open Match from using the Ticket for matchmaking and removes the Ticket from state storage.
     * The client should delete the Ticket when finished matchmaking with it. 
     * </pre>
     */
    public com.google.protobuf.Empty deleteTicket(dev.openmatch.DeleteTicketRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteTicketMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * GetTicket get the Ticket associated with the specified TicketId.
     * </pre>
     */
    public dev.openmatch.Ticket getTicket(dev.openmatch.GetTicketRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTicketMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * WatchAssignments stream back Assignment of the specified TicketId if it is updated.
     *   - If the Assignment is not updated, GetAssignment will retry using the configured backoff strategy. 
     * </pre>
     */
    public java.util.Iterator<dev.openmatch.WatchAssignmentsResponse> watchAssignments(
        dev.openmatch.WatchAssignmentsRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getWatchAssignmentsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * AcknowledgeBackfill is used to notify OpenMatch about GameServer connection info
     * This triggers an assignment process.
     * BETA FEATURE WARNING: This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public dev.openmatch.AcknowledgeBackfillResponse acknowledgeBackfill(dev.openmatch.AcknowledgeBackfillRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAcknowledgeBackfillMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * CreateBackfill creates a new Backfill object.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public dev.openmatch.Backfill createBackfill(dev.openmatch.CreateBackfillRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateBackfillMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * DeleteBackfill receives a backfill ID and deletes its resource.
     * Any tickets waiting for this backfill will be returned to the active pool, no longer pending.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public com.google.protobuf.Empty deleteBackfill(dev.openmatch.DeleteBackfillRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteBackfillMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * GetBackfill returns a backfill object by its ID.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public dev.openmatch.Backfill getBackfill(dev.openmatch.GetBackfillRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetBackfillMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * UpdateBackfill updates search_fields and extensions for the backfill with the provided id.
     * Any tickets waiting for this backfill will be returned to the active pool, no longer pending.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public dev.openmatch.Backfill updateBackfill(dev.openmatch.UpdateBackfillRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateBackfillMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service FrontendService.
   * <pre>
   * The FrontendService implements APIs to manage and query status of a Tickets.
   * </pre>
   */
  public static final class FrontendServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<FrontendServiceFutureStub> {
    private FrontendServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FrontendServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FrontendServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * CreateTicket assigns an unique TicketId to the input Ticket and record it in state storage.
     * A ticket is considered as ready for matchmaking once it is created.
     *   - If a TicketId exists in a Ticket request, an auto-generated TicketId will override this field.
     *   - If SearchFields exist in a Ticket, CreateTicket will also index these fields such that one can query the ticket with query.QueryTickets function.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.openmatch.Ticket> createTicket(
        dev.openmatch.CreateTicketRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateTicketMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * DeleteTicket immediately stops Open Match from using the Ticket for matchmaking and removes the Ticket from state storage.
     * The client should delete the Ticket when finished matchmaking with it. 
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteTicket(
        dev.openmatch.DeleteTicketRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteTicketMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * GetTicket get the Ticket associated with the specified TicketId.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.openmatch.Ticket> getTicket(
        dev.openmatch.GetTicketRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTicketMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * AcknowledgeBackfill is used to notify OpenMatch about GameServer connection info
     * This triggers an assignment process.
     * BETA FEATURE WARNING: This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.openmatch.AcknowledgeBackfillResponse> acknowledgeBackfill(
        dev.openmatch.AcknowledgeBackfillRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAcknowledgeBackfillMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * CreateBackfill creates a new Backfill object.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.openmatch.Backfill> createBackfill(
        dev.openmatch.CreateBackfillRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateBackfillMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * DeleteBackfill receives a backfill ID and deletes its resource.
     * Any tickets waiting for this backfill will be returned to the active pool, no longer pending.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteBackfill(
        dev.openmatch.DeleteBackfillRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteBackfillMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * GetBackfill returns a backfill object by its ID.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.openmatch.Backfill> getBackfill(
        dev.openmatch.GetBackfillRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetBackfillMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * UpdateBackfill updates search_fields and extensions for the backfill with the provided id.
     * Any tickets waiting for this backfill will be returned to the active pool, no longer pending.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<dev.openmatch.Backfill> updateBackfill(
        dev.openmatch.UpdateBackfillRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateBackfillMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_TICKET = 0;
  private static final int METHODID_DELETE_TICKET = 1;
  private static final int METHODID_GET_TICKET = 2;
  private static final int METHODID_WATCH_ASSIGNMENTS = 3;
  private static final int METHODID_ACKNOWLEDGE_BACKFILL = 4;
  private static final int METHODID_CREATE_BACKFILL = 5;
  private static final int METHODID_DELETE_BACKFILL = 6;
  private static final int METHODID_GET_BACKFILL = 7;
  private static final int METHODID_UPDATE_BACKFILL = 8;

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
        case METHODID_CREATE_TICKET:
          serviceImpl.createTicket((dev.openmatch.CreateTicketRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.Ticket>) responseObserver);
          break;
        case METHODID_DELETE_TICKET:
          serviceImpl.deleteTicket((dev.openmatch.DeleteTicketRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_GET_TICKET:
          serviceImpl.getTicket((dev.openmatch.GetTicketRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.Ticket>) responseObserver);
          break;
        case METHODID_WATCH_ASSIGNMENTS:
          serviceImpl.watchAssignments((dev.openmatch.WatchAssignmentsRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.WatchAssignmentsResponse>) responseObserver);
          break;
        case METHODID_ACKNOWLEDGE_BACKFILL:
          serviceImpl.acknowledgeBackfill((dev.openmatch.AcknowledgeBackfillRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.AcknowledgeBackfillResponse>) responseObserver);
          break;
        case METHODID_CREATE_BACKFILL:
          serviceImpl.createBackfill((dev.openmatch.CreateBackfillRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.Backfill>) responseObserver);
          break;
        case METHODID_DELETE_BACKFILL:
          serviceImpl.deleteBackfill((dev.openmatch.DeleteBackfillRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_GET_BACKFILL:
          serviceImpl.getBackfill((dev.openmatch.GetBackfillRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.Backfill>) responseObserver);
          break;
        case METHODID_UPDATE_BACKFILL:
          serviceImpl.updateBackfill((dev.openmatch.UpdateBackfillRequest) request,
              (io.grpc.stub.StreamObserver<dev.openmatch.Backfill>) responseObserver);
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
          getCreateTicketMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.CreateTicketRequest,
              dev.openmatch.Ticket>(
                service, METHODID_CREATE_TICKET)))
        .addMethod(
          getDeleteTicketMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.DeleteTicketRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_TICKET)))
        .addMethod(
          getGetTicketMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.GetTicketRequest,
              dev.openmatch.Ticket>(
                service, METHODID_GET_TICKET)))
        .addMethod(
          getWatchAssignmentsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              dev.openmatch.WatchAssignmentsRequest,
              dev.openmatch.WatchAssignmentsResponse>(
                service, METHODID_WATCH_ASSIGNMENTS)))
        .addMethod(
          getAcknowledgeBackfillMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.AcknowledgeBackfillRequest,
              dev.openmatch.AcknowledgeBackfillResponse>(
                service, METHODID_ACKNOWLEDGE_BACKFILL)))
        .addMethod(
          getCreateBackfillMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.CreateBackfillRequest,
              dev.openmatch.Backfill>(
                service, METHODID_CREATE_BACKFILL)))
        .addMethod(
          getDeleteBackfillMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.DeleteBackfillRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_BACKFILL)))
        .addMethod(
          getGetBackfillMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.GetBackfillRequest,
              dev.openmatch.Backfill>(
                service, METHODID_GET_BACKFILL)))
        .addMethod(
          getUpdateBackfillMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              dev.openmatch.UpdateBackfillRequest,
              dev.openmatch.Backfill>(
                service, METHODID_UPDATE_BACKFILL)))
        .build();
  }

  private static abstract class FrontendServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    FrontendServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return dev.openmatch.FrontendProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("FrontendService");
    }
  }

  private static final class FrontendServiceFileDescriptorSupplier
      extends FrontendServiceBaseDescriptorSupplier {
    FrontendServiceFileDescriptorSupplier() {}
  }

  private static final class FrontendServiceMethodDescriptorSupplier
      extends FrontendServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    FrontendServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (FrontendServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new FrontendServiceFileDescriptorSupplier())
              .addMethod(getCreateTicketMethod())
              .addMethod(getDeleteTicketMethod())
              .addMethod(getGetTicketMethod())
              .addMethod(getWatchAssignmentsMethod())
              .addMethod(getAcknowledgeBackfillMethod())
              .addMethod(getCreateBackfillMethod())
              .addMethod(getDeleteBackfillMethod())
              .addMethod(getGetBackfillMethod())
              .addMethod(getUpdateBackfillMethod())
              .build();
        }
      }
    }
    return result;
  }
}
