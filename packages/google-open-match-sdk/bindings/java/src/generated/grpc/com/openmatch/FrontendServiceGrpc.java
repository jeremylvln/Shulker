package com.openmatch;

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
  private static volatile io.grpc.MethodDescriptor<com.openmatch.CreateTicketRequest,
      com.openmatch.Ticket> getCreateTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateTicket",
      requestType = com.openmatch.CreateTicketRequest.class,
      responseType = com.openmatch.Ticket.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.openmatch.CreateTicketRequest,
      com.openmatch.Ticket> getCreateTicketMethod() {
    io.grpc.MethodDescriptor<com.openmatch.CreateTicketRequest, com.openmatch.Ticket> getCreateTicketMethod;
    if ((getCreateTicketMethod = FrontendServiceGrpc.getCreateTicketMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getCreateTicketMethod = FrontendServiceGrpc.getCreateTicketMethod) == null) {
          FrontendServiceGrpc.getCreateTicketMethod = getCreateTicketMethod =
              io.grpc.MethodDescriptor.<com.openmatch.CreateTicketRequest, com.openmatch.Ticket>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.CreateTicketRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.Ticket.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("CreateTicket"))
              .build();
        }
      }
    }
    return getCreateTicketMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.openmatch.DeleteTicketRequest,
      com.google.protobuf.Empty> getDeleteTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteTicket",
      requestType = com.openmatch.DeleteTicketRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.openmatch.DeleteTicketRequest,
      com.google.protobuf.Empty> getDeleteTicketMethod() {
    io.grpc.MethodDescriptor<com.openmatch.DeleteTicketRequest, com.google.protobuf.Empty> getDeleteTicketMethod;
    if ((getDeleteTicketMethod = FrontendServiceGrpc.getDeleteTicketMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getDeleteTicketMethod = FrontendServiceGrpc.getDeleteTicketMethod) == null) {
          FrontendServiceGrpc.getDeleteTicketMethod = getDeleteTicketMethod =
              io.grpc.MethodDescriptor.<com.openmatch.DeleteTicketRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.DeleteTicketRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("DeleteTicket"))
              .build();
        }
      }
    }
    return getDeleteTicketMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.openmatch.GetTicketRequest,
      com.openmatch.Ticket> getGetTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTicket",
      requestType = com.openmatch.GetTicketRequest.class,
      responseType = com.openmatch.Ticket.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.openmatch.GetTicketRequest,
      com.openmatch.Ticket> getGetTicketMethod() {
    io.grpc.MethodDescriptor<com.openmatch.GetTicketRequest, com.openmatch.Ticket> getGetTicketMethod;
    if ((getGetTicketMethod = FrontendServiceGrpc.getGetTicketMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getGetTicketMethod = FrontendServiceGrpc.getGetTicketMethod) == null) {
          FrontendServiceGrpc.getGetTicketMethod = getGetTicketMethod =
              io.grpc.MethodDescriptor.<com.openmatch.GetTicketRequest, com.openmatch.Ticket>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.GetTicketRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.Ticket.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("GetTicket"))
              .build();
        }
      }
    }
    return getGetTicketMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.openmatch.WatchAssignmentsRequest,
      com.openmatch.WatchAssignmentsResponse> getWatchAssignmentsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WatchAssignments",
      requestType = com.openmatch.WatchAssignmentsRequest.class,
      responseType = com.openmatch.WatchAssignmentsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.openmatch.WatchAssignmentsRequest,
      com.openmatch.WatchAssignmentsResponse> getWatchAssignmentsMethod() {
    io.grpc.MethodDescriptor<com.openmatch.WatchAssignmentsRequest, com.openmatch.WatchAssignmentsResponse> getWatchAssignmentsMethod;
    if ((getWatchAssignmentsMethod = FrontendServiceGrpc.getWatchAssignmentsMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getWatchAssignmentsMethod = FrontendServiceGrpc.getWatchAssignmentsMethod) == null) {
          FrontendServiceGrpc.getWatchAssignmentsMethod = getWatchAssignmentsMethod =
              io.grpc.MethodDescriptor.<com.openmatch.WatchAssignmentsRequest, com.openmatch.WatchAssignmentsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WatchAssignments"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.WatchAssignmentsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.WatchAssignmentsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("WatchAssignments"))
              .build();
        }
      }
    }
    return getWatchAssignmentsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.openmatch.AcknowledgeBackfillRequest,
      com.openmatch.AcknowledgeBackfillResponse> getAcknowledgeBackfillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AcknowledgeBackfill",
      requestType = com.openmatch.AcknowledgeBackfillRequest.class,
      responseType = com.openmatch.AcknowledgeBackfillResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.openmatch.AcknowledgeBackfillRequest,
      com.openmatch.AcknowledgeBackfillResponse> getAcknowledgeBackfillMethod() {
    io.grpc.MethodDescriptor<com.openmatch.AcknowledgeBackfillRequest, com.openmatch.AcknowledgeBackfillResponse> getAcknowledgeBackfillMethod;
    if ((getAcknowledgeBackfillMethod = FrontendServiceGrpc.getAcknowledgeBackfillMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getAcknowledgeBackfillMethod = FrontendServiceGrpc.getAcknowledgeBackfillMethod) == null) {
          FrontendServiceGrpc.getAcknowledgeBackfillMethod = getAcknowledgeBackfillMethod =
              io.grpc.MethodDescriptor.<com.openmatch.AcknowledgeBackfillRequest, com.openmatch.AcknowledgeBackfillResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AcknowledgeBackfill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.AcknowledgeBackfillRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.AcknowledgeBackfillResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("AcknowledgeBackfill"))
              .build();
        }
      }
    }
    return getAcknowledgeBackfillMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.openmatch.CreateBackfillRequest,
      com.openmatch.Backfill> getCreateBackfillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateBackfill",
      requestType = com.openmatch.CreateBackfillRequest.class,
      responseType = com.openmatch.Backfill.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.openmatch.CreateBackfillRequest,
      com.openmatch.Backfill> getCreateBackfillMethod() {
    io.grpc.MethodDescriptor<com.openmatch.CreateBackfillRequest, com.openmatch.Backfill> getCreateBackfillMethod;
    if ((getCreateBackfillMethod = FrontendServiceGrpc.getCreateBackfillMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getCreateBackfillMethod = FrontendServiceGrpc.getCreateBackfillMethod) == null) {
          FrontendServiceGrpc.getCreateBackfillMethod = getCreateBackfillMethod =
              io.grpc.MethodDescriptor.<com.openmatch.CreateBackfillRequest, com.openmatch.Backfill>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateBackfill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.CreateBackfillRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.Backfill.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("CreateBackfill"))
              .build();
        }
      }
    }
    return getCreateBackfillMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.openmatch.DeleteBackfillRequest,
      com.google.protobuf.Empty> getDeleteBackfillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteBackfill",
      requestType = com.openmatch.DeleteBackfillRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.openmatch.DeleteBackfillRequest,
      com.google.protobuf.Empty> getDeleteBackfillMethod() {
    io.grpc.MethodDescriptor<com.openmatch.DeleteBackfillRequest, com.google.protobuf.Empty> getDeleteBackfillMethod;
    if ((getDeleteBackfillMethod = FrontendServiceGrpc.getDeleteBackfillMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getDeleteBackfillMethod = FrontendServiceGrpc.getDeleteBackfillMethod) == null) {
          FrontendServiceGrpc.getDeleteBackfillMethod = getDeleteBackfillMethod =
              io.grpc.MethodDescriptor.<com.openmatch.DeleteBackfillRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteBackfill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.DeleteBackfillRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("DeleteBackfill"))
              .build();
        }
      }
    }
    return getDeleteBackfillMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.openmatch.GetBackfillRequest,
      com.openmatch.Backfill> getGetBackfillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetBackfill",
      requestType = com.openmatch.GetBackfillRequest.class,
      responseType = com.openmatch.Backfill.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.openmatch.GetBackfillRequest,
      com.openmatch.Backfill> getGetBackfillMethod() {
    io.grpc.MethodDescriptor<com.openmatch.GetBackfillRequest, com.openmatch.Backfill> getGetBackfillMethod;
    if ((getGetBackfillMethod = FrontendServiceGrpc.getGetBackfillMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getGetBackfillMethod = FrontendServiceGrpc.getGetBackfillMethod) == null) {
          FrontendServiceGrpc.getGetBackfillMethod = getGetBackfillMethod =
              io.grpc.MethodDescriptor.<com.openmatch.GetBackfillRequest, com.openmatch.Backfill>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetBackfill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.GetBackfillRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.Backfill.getDefaultInstance()))
              .setSchemaDescriptor(new FrontendServiceMethodDescriptorSupplier("GetBackfill"))
              .build();
        }
      }
    }
    return getGetBackfillMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.openmatch.UpdateBackfillRequest,
      com.openmatch.Backfill> getUpdateBackfillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateBackfill",
      requestType = com.openmatch.UpdateBackfillRequest.class,
      responseType = com.openmatch.Backfill.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.openmatch.UpdateBackfillRequest,
      com.openmatch.Backfill> getUpdateBackfillMethod() {
    io.grpc.MethodDescriptor<com.openmatch.UpdateBackfillRequest, com.openmatch.Backfill> getUpdateBackfillMethod;
    if ((getUpdateBackfillMethod = FrontendServiceGrpc.getUpdateBackfillMethod) == null) {
      synchronized (FrontendServiceGrpc.class) {
        if ((getUpdateBackfillMethod = FrontendServiceGrpc.getUpdateBackfillMethod) == null) {
          FrontendServiceGrpc.getUpdateBackfillMethod = getUpdateBackfillMethod =
              io.grpc.MethodDescriptor.<com.openmatch.UpdateBackfillRequest, com.openmatch.Backfill>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateBackfill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.UpdateBackfillRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.openmatch.Backfill.getDefaultInstance()))
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
    default void createTicket(com.openmatch.CreateTicketRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.Ticket> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateTicketMethod(), responseObserver);
    }

    /**
     * <pre>
     * DeleteTicket immediately stops Open Match from using the Ticket for matchmaking and removes the Ticket from state storage.
     * The client should delete the Ticket when finished matchmaking with it.
     * </pre>
     */
    default void deleteTicket(com.openmatch.DeleteTicketRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteTicketMethod(), responseObserver);
    }

    /**
     * <pre>
     * GetTicket get the Ticket associated with the specified TicketId.
     * </pre>
     */
    default void getTicket(com.openmatch.GetTicketRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.Ticket> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTicketMethod(), responseObserver);
    }

    /**
     * <pre>
     * WatchAssignments stream back Assignment of the specified TicketId if it is updated.
     *   - If the Assignment is not updated, GetAssignment will retry using the configured backoff strategy.
     * </pre>
     */
    default void watchAssignments(com.openmatch.WatchAssignmentsRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.WatchAssignmentsResponse> responseObserver) {
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
    default void acknowledgeBackfill(com.openmatch.AcknowledgeBackfillRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.AcknowledgeBackfillResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAcknowledgeBackfillMethod(), responseObserver);
    }

    /**
     * <pre>
     * CreateBackfill creates a new Backfill object.
     * BETA FEATURE WARNING:  This call and the associated Request and Response
     * messages are not finalized and still subject to possible change or removal.
     * </pre>
     */
    default void createBackfill(com.openmatch.CreateBackfillRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.Backfill> responseObserver) {
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
    default void deleteBackfill(com.openmatch.DeleteBackfillRequest request,
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
    default void getBackfill(com.openmatch.GetBackfillRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.Backfill> responseObserver) {
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
    default void updateBackfill(com.openmatch.UpdateBackfillRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.Backfill> responseObserver) {
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
    public void createTicket(com.openmatch.CreateTicketRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.Ticket> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateTicketMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * DeleteTicket immediately stops Open Match from using the Ticket for matchmaking and removes the Ticket from state storage.
     * The client should delete the Ticket when finished matchmaking with it.
     * </pre>
     */
    public void deleteTicket(com.openmatch.DeleteTicketRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteTicketMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * GetTicket get the Ticket associated with the specified TicketId.
     * </pre>
     */
    public void getTicket(com.openmatch.GetTicketRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.Ticket> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTicketMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * WatchAssignments stream back Assignment of the specified TicketId if it is updated.
     *   - If the Assignment is not updated, GetAssignment will retry using the configured backoff strategy.
     * </pre>
     */
    public void watchAssignments(com.openmatch.WatchAssignmentsRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.WatchAssignmentsResponse> responseObserver) {
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
    public void acknowledgeBackfill(com.openmatch.AcknowledgeBackfillRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.AcknowledgeBackfillResponse> responseObserver) {
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
    public void createBackfill(com.openmatch.CreateBackfillRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.Backfill> responseObserver) {
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
    public void deleteBackfill(com.openmatch.DeleteBackfillRequest request,
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
    public void getBackfill(com.openmatch.GetBackfillRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.Backfill> responseObserver) {
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
    public void updateBackfill(com.openmatch.UpdateBackfillRequest request,
        io.grpc.stub.StreamObserver<com.openmatch.Backfill> responseObserver) {
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
    public com.openmatch.Ticket createTicket(com.openmatch.CreateTicketRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateTicketMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * DeleteTicket immediately stops Open Match from using the Ticket for matchmaking and removes the Ticket from state storage.
     * The client should delete the Ticket when finished matchmaking with it.
     * </pre>
     */
    public com.google.protobuf.Empty deleteTicket(com.openmatch.DeleteTicketRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteTicketMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * GetTicket get the Ticket associated with the specified TicketId.
     * </pre>
     */
    public com.openmatch.Ticket getTicket(com.openmatch.GetTicketRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTicketMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * WatchAssignments stream back Assignment of the specified TicketId if it is updated.
     *   - If the Assignment is not updated, GetAssignment will retry using the configured backoff strategy.
     * </pre>
     */
    public java.util.Iterator<com.openmatch.WatchAssignmentsResponse> watchAssignments(
        com.openmatch.WatchAssignmentsRequest request) {
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
    public com.openmatch.AcknowledgeBackfillResponse acknowledgeBackfill(com.openmatch.AcknowledgeBackfillRequest request) {
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
    public com.openmatch.Backfill createBackfill(com.openmatch.CreateBackfillRequest request) {
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
    public com.google.protobuf.Empty deleteBackfill(com.openmatch.DeleteBackfillRequest request) {
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
    public com.openmatch.Backfill getBackfill(com.openmatch.GetBackfillRequest request) {
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
    public com.openmatch.Backfill updateBackfill(com.openmatch.UpdateBackfillRequest request) {
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
    public com.google.common.util.concurrent.ListenableFuture<com.openmatch.Ticket> createTicket(
        com.openmatch.CreateTicketRequest request) {
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
        com.openmatch.DeleteTicketRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteTicketMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * GetTicket get the Ticket associated with the specified TicketId.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.openmatch.Ticket> getTicket(
        com.openmatch.GetTicketRequest request) {
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
    public com.google.common.util.concurrent.ListenableFuture<com.openmatch.AcknowledgeBackfillResponse> acknowledgeBackfill(
        com.openmatch.AcknowledgeBackfillRequest request) {
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
    public com.google.common.util.concurrent.ListenableFuture<com.openmatch.Backfill> createBackfill(
        com.openmatch.CreateBackfillRequest request) {
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
        com.openmatch.DeleteBackfillRequest request) {
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
    public com.google.common.util.concurrent.ListenableFuture<com.openmatch.Backfill> getBackfill(
        com.openmatch.GetBackfillRequest request) {
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
    public com.google.common.util.concurrent.ListenableFuture<com.openmatch.Backfill> updateBackfill(
        com.openmatch.UpdateBackfillRequest request) {
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
          serviceImpl.createTicket((com.openmatch.CreateTicketRequest) request,
              (io.grpc.stub.StreamObserver<com.openmatch.Ticket>) responseObserver);
          break;
        case METHODID_DELETE_TICKET:
          serviceImpl.deleteTicket((com.openmatch.DeleteTicketRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_GET_TICKET:
          serviceImpl.getTicket((com.openmatch.GetTicketRequest) request,
              (io.grpc.stub.StreamObserver<com.openmatch.Ticket>) responseObserver);
          break;
        case METHODID_WATCH_ASSIGNMENTS:
          serviceImpl.watchAssignments((com.openmatch.WatchAssignmentsRequest) request,
              (io.grpc.stub.StreamObserver<com.openmatch.WatchAssignmentsResponse>) responseObserver);
          break;
        case METHODID_ACKNOWLEDGE_BACKFILL:
          serviceImpl.acknowledgeBackfill((com.openmatch.AcknowledgeBackfillRequest) request,
              (io.grpc.stub.StreamObserver<com.openmatch.AcknowledgeBackfillResponse>) responseObserver);
          break;
        case METHODID_CREATE_BACKFILL:
          serviceImpl.createBackfill((com.openmatch.CreateBackfillRequest) request,
              (io.grpc.stub.StreamObserver<com.openmatch.Backfill>) responseObserver);
          break;
        case METHODID_DELETE_BACKFILL:
          serviceImpl.deleteBackfill((com.openmatch.DeleteBackfillRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_GET_BACKFILL:
          serviceImpl.getBackfill((com.openmatch.GetBackfillRequest) request,
              (io.grpc.stub.StreamObserver<com.openmatch.Backfill>) responseObserver);
          break;
        case METHODID_UPDATE_BACKFILL:
          serviceImpl.updateBackfill((com.openmatch.UpdateBackfillRequest) request,
              (io.grpc.stub.StreamObserver<com.openmatch.Backfill>) responseObserver);
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
              com.openmatch.CreateTicketRequest,
              com.openmatch.Ticket>(
                service, METHODID_CREATE_TICKET)))
        .addMethod(
          getDeleteTicketMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.openmatch.DeleteTicketRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_TICKET)))
        .addMethod(
          getGetTicketMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.openmatch.GetTicketRequest,
              com.openmatch.Ticket>(
                service, METHODID_GET_TICKET)))
        .addMethod(
          getWatchAssignmentsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              com.openmatch.WatchAssignmentsRequest,
              com.openmatch.WatchAssignmentsResponse>(
                service, METHODID_WATCH_ASSIGNMENTS)))
        .addMethod(
          getAcknowledgeBackfillMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.openmatch.AcknowledgeBackfillRequest,
              com.openmatch.AcknowledgeBackfillResponse>(
                service, METHODID_ACKNOWLEDGE_BACKFILL)))
        .addMethod(
          getCreateBackfillMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.openmatch.CreateBackfillRequest,
              com.openmatch.Backfill>(
                service, METHODID_CREATE_BACKFILL)))
        .addMethod(
          getDeleteBackfillMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.openmatch.DeleteBackfillRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_BACKFILL)))
        .addMethod(
          getGetBackfillMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.openmatch.GetBackfillRequest,
              com.openmatch.Backfill>(
                service, METHODID_GET_BACKFILL)))
        .addMethod(
          getUpdateBackfillMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.openmatch.UpdateBackfillRequest,
              com.openmatch.Backfill>(
                service, METHODID_UPDATE_BACKFILL)))
        .build();
  }

  private static abstract class FrontendServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    FrontendServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.openmatch.FrontendProto.getDescriptor();
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
