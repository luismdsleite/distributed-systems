package ds.assignment.tokenring;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.51.1)",
    comments = "Source: msgHandler.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class msgHandlerGrpc {

  private msgHandlerGrpc() {}

  public static final String SERVICE_NAME = "msgHandler";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ds.assignment.tokenring.MsgHandler.Message,
      ds.assignment.tokenring.MsgHandler.Empty> getSendMsgMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "sendMsg",
      requestType = ds.assignment.tokenring.MsgHandler.Message.class,
      responseType = ds.assignment.tokenring.MsgHandler.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ds.assignment.tokenring.MsgHandler.Message,
      ds.assignment.tokenring.MsgHandler.Empty> getSendMsgMethod() {
    io.grpc.MethodDescriptor<ds.assignment.tokenring.MsgHandler.Message, ds.assignment.tokenring.MsgHandler.Empty> getSendMsgMethod;
    if ((getSendMsgMethod = msgHandlerGrpc.getSendMsgMethod) == null) {
      synchronized (msgHandlerGrpc.class) {
        if ((getSendMsgMethod = msgHandlerGrpc.getSendMsgMethod) == null) {
          msgHandlerGrpc.getSendMsgMethod = getSendMsgMethod =
              io.grpc.MethodDescriptor.<ds.assignment.tokenring.MsgHandler.Message, ds.assignment.tokenring.MsgHandler.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "sendMsg"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.assignment.tokenring.MsgHandler.Message.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.assignment.tokenring.MsgHandler.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new msgHandlerMethodDescriptorSupplier("sendMsg"))
              .build();
        }
      }
    }
    return getSendMsgMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static msgHandlerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<msgHandlerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<msgHandlerStub>() {
        @java.lang.Override
        public msgHandlerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new msgHandlerStub(channel, callOptions);
        }
      };
    return msgHandlerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static msgHandlerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<msgHandlerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<msgHandlerBlockingStub>() {
        @java.lang.Override
        public msgHandlerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new msgHandlerBlockingStub(channel, callOptions);
        }
      };
    return msgHandlerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static msgHandlerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<msgHandlerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<msgHandlerFutureStub>() {
        @java.lang.Override
        public msgHandlerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new msgHandlerFutureStub(channel, callOptions);
        }
      };
    return msgHandlerFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class msgHandlerImplBase implements io.grpc.BindableService {

    /**
     */
    public void sendMsg(ds.assignment.tokenring.MsgHandler.Message request,
        io.grpc.stub.StreamObserver<ds.assignment.tokenring.MsgHandler.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendMsgMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSendMsgMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                ds.assignment.tokenring.MsgHandler.Message,
                ds.assignment.tokenring.MsgHandler.Empty>(
                  this, METHODID_SEND_MSG)))
          .build();
    }
  }

  /**
   */
  public static final class msgHandlerStub extends io.grpc.stub.AbstractAsyncStub<msgHandlerStub> {
    private msgHandlerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected msgHandlerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new msgHandlerStub(channel, callOptions);
    }

    /**
     */
    public void sendMsg(ds.assignment.tokenring.MsgHandler.Message request,
        io.grpc.stub.StreamObserver<ds.assignment.tokenring.MsgHandler.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendMsgMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class msgHandlerBlockingStub extends io.grpc.stub.AbstractBlockingStub<msgHandlerBlockingStub> {
    private msgHandlerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected msgHandlerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new msgHandlerBlockingStub(channel, callOptions);
    }

    /**
     */
    public ds.assignment.tokenring.MsgHandler.Empty sendMsg(ds.assignment.tokenring.MsgHandler.Message request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendMsgMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class msgHandlerFutureStub extends io.grpc.stub.AbstractFutureStub<msgHandlerFutureStub> {
    private msgHandlerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected msgHandlerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new msgHandlerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ds.assignment.tokenring.MsgHandler.Empty> sendMsg(
        ds.assignment.tokenring.MsgHandler.Message request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendMsgMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SEND_MSG = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final msgHandlerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(msgHandlerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEND_MSG:
          serviceImpl.sendMsg((ds.assignment.tokenring.MsgHandler.Message) request,
              (io.grpc.stub.StreamObserver<ds.assignment.tokenring.MsgHandler.Empty>) responseObserver);
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

  private static abstract class msgHandlerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    msgHandlerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ds.assignment.tokenring.MsgHandler.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("msgHandler");
    }
  }

  private static final class msgHandlerFileDescriptorSupplier
      extends msgHandlerBaseDescriptorSupplier {
    msgHandlerFileDescriptorSupplier() {}
  }

  private static final class msgHandlerMethodDescriptorSupplier
      extends msgHandlerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    msgHandlerMethodDescriptorSupplier(String methodName) {
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
      synchronized (msgHandlerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new msgHandlerFileDescriptorSupplier())
              .addMethod(getSendMsgMethod())
              .build();
        }
      }
    }
    return result;
  }
}
