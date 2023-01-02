package generated;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Msg Handler with lamport clocks support.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.51.1)",
    comments = "Source: lamportMsgHandler.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class lamportMsgHandlerGrpc {

  private lamportMsgHandlerGrpc() {}

  public static final String SERVICE_NAME = "proto.lamportMsgHandler";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<generated.LamportMsgHandler.Get,
      generated.LamportMsgHandler.Empty> getGetValueMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getValue",
      requestType = generated.LamportMsgHandler.Get.class,
      responseType = generated.LamportMsgHandler.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<generated.LamportMsgHandler.Get,
      generated.LamportMsgHandler.Empty> getGetValueMethod() {
    io.grpc.MethodDescriptor<generated.LamportMsgHandler.Get, generated.LamportMsgHandler.Empty> getGetValueMethod;
    if ((getGetValueMethod = lamportMsgHandlerGrpc.getGetValueMethod) == null) {
      synchronized (lamportMsgHandlerGrpc.class) {
        if ((getGetValueMethod = lamportMsgHandlerGrpc.getGetValueMethod) == null) {
          lamportMsgHandlerGrpc.getGetValueMethod = getGetValueMethod =
              io.grpc.MethodDescriptor.<generated.LamportMsgHandler.Get, generated.LamportMsgHandler.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getValue"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.LamportMsgHandler.Get.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.LamportMsgHandler.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new lamportMsgHandlerMethodDescriptorSupplier("getValue"))
              .build();
        }
      }
    }
    return getGetValueMethod;
  }

  private static volatile io.grpc.MethodDescriptor<generated.LamportMsgHandler.Put,
      generated.LamportMsgHandler.Empty> getPutValueMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "putValue",
      requestType = generated.LamportMsgHandler.Put.class,
      responseType = generated.LamportMsgHandler.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<generated.LamportMsgHandler.Put,
      generated.LamportMsgHandler.Empty> getPutValueMethod() {
    io.grpc.MethodDescriptor<generated.LamportMsgHandler.Put, generated.LamportMsgHandler.Empty> getPutValueMethod;
    if ((getPutValueMethod = lamportMsgHandlerGrpc.getPutValueMethod) == null) {
      synchronized (lamportMsgHandlerGrpc.class) {
        if ((getPutValueMethod = lamportMsgHandlerGrpc.getPutValueMethod) == null) {
          lamportMsgHandlerGrpc.getPutValueMethod = getPutValueMethod =
              io.grpc.MethodDescriptor.<generated.LamportMsgHandler.Put, generated.LamportMsgHandler.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "putValue"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.LamportMsgHandler.Put.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.LamportMsgHandler.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new lamportMsgHandlerMethodDescriptorSupplier("putValue"))
              .build();
        }
      }
    }
    return getPutValueMethod;
  }

  private static volatile io.grpc.MethodDescriptor<generated.LamportMsgHandler.Ack,
      generated.LamportMsgHandler.Empty> getAckEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ackEvent",
      requestType = generated.LamportMsgHandler.Ack.class,
      responseType = generated.LamportMsgHandler.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<generated.LamportMsgHandler.Ack,
      generated.LamportMsgHandler.Empty> getAckEventMethod() {
    io.grpc.MethodDescriptor<generated.LamportMsgHandler.Ack, generated.LamportMsgHandler.Empty> getAckEventMethod;
    if ((getAckEventMethod = lamportMsgHandlerGrpc.getAckEventMethod) == null) {
      synchronized (lamportMsgHandlerGrpc.class) {
        if ((getAckEventMethod = lamportMsgHandlerGrpc.getAckEventMethod) == null) {
          lamportMsgHandlerGrpc.getAckEventMethod = getAckEventMethod =
              io.grpc.MethodDescriptor.<generated.LamportMsgHandler.Ack, generated.LamportMsgHandler.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ackEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.LamportMsgHandler.Ack.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.LamportMsgHandler.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new lamportMsgHandlerMethodDescriptorSupplier("ackEvent"))
              .build();
        }
      }
    }
    return getAckEventMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static lamportMsgHandlerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<lamportMsgHandlerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<lamportMsgHandlerStub>() {
        @java.lang.Override
        public lamportMsgHandlerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new lamportMsgHandlerStub(channel, callOptions);
        }
      };
    return lamportMsgHandlerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static lamportMsgHandlerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<lamportMsgHandlerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<lamportMsgHandlerBlockingStub>() {
        @java.lang.Override
        public lamportMsgHandlerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new lamportMsgHandlerBlockingStub(channel, callOptions);
        }
      };
    return lamportMsgHandlerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static lamportMsgHandlerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<lamportMsgHandlerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<lamportMsgHandlerFutureStub>() {
        @java.lang.Override
        public lamportMsgHandlerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new lamportMsgHandlerFutureStub(channel, callOptions);
        }
      };
    return lamportMsgHandlerFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Msg Handler with lamport clocks support.
   * </pre>
   */
  public static abstract class lamportMsgHandlerImplBase implements io.grpc.BindableService {

    /**
     */
    public void getValue(generated.LamportMsgHandler.Get request,
        io.grpc.stub.StreamObserver<generated.LamportMsgHandler.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetValueMethod(), responseObserver);
    }

    /**
     */
    public void putValue(generated.LamportMsgHandler.Put request,
        io.grpc.stub.StreamObserver<generated.LamportMsgHandler.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutValueMethod(), responseObserver);
    }

    /**
     */
    public void ackEvent(generated.LamportMsgHandler.Ack request,
        io.grpc.stub.StreamObserver<generated.LamportMsgHandler.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAckEventMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetValueMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                generated.LamportMsgHandler.Get,
                generated.LamportMsgHandler.Empty>(
                  this, METHODID_GET_VALUE)))
          .addMethod(
            getPutValueMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                generated.LamportMsgHandler.Put,
                generated.LamportMsgHandler.Empty>(
                  this, METHODID_PUT_VALUE)))
          .addMethod(
            getAckEventMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                generated.LamportMsgHandler.Ack,
                generated.LamportMsgHandler.Empty>(
                  this, METHODID_ACK_EVENT)))
          .build();
    }
  }

  /**
   * <pre>
   * Msg Handler with lamport clocks support.
   * </pre>
   */
  public static final class lamportMsgHandlerStub extends io.grpc.stub.AbstractAsyncStub<lamportMsgHandlerStub> {
    private lamportMsgHandlerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected lamportMsgHandlerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new lamportMsgHandlerStub(channel, callOptions);
    }

    /**
     */
    public void getValue(generated.LamportMsgHandler.Get request,
        io.grpc.stub.StreamObserver<generated.LamportMsgHandler.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetValueMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putValue(generated.LamportMsgHandler.Put request,
        io.grpc.stub.StreamObserver<generated.LamportMsgHandler.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutValueMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void ackEvent(generated.LamportMsgHandler.Ack request,
        io.grpc.stub.StreamObserver<generated.LamportMsgHandler.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAckEventMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Msg Handler with lamport clocks support.
   * </pre>
   */
  public static final class lamportMsgHandlerBlockingStub extends io.grpc.stub.AbstractBlockingStub<lamportMsgHandlerBlockingStub> {
    private lamportMsgHandlerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected lamportMsgHandlerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new lamportMsgHandlerBlockingStub(channel, callOptions);
    }

    /**
     */
    public generated.LamportMsgHandler.Empty getValue(generated.LamportMsgHandler.Get request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetValueMethod(), getCallOptions(), request);
    }

    /**
     */
    public generated.LamportMsgHandler.Empty putValue(generated.LamportMsgHandler.Put request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutValueMethod(), getCallOptions(), request);
    }

    /**
     */
    public generated.LamportMsgHandler.Empty ackEvent(generated.LamportMsgHandler.Ack request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAckEventMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Msg Handler with lamport clocks support.
   * </pre>
   */
  public static final class lamportMsgHandlerFutureStub extends io.grpc.stub.AbstractFutureStub<lamportMsgHandlerFutureStub> {
    private lamportMsgHandlerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected lamportMsgHandlerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new lamportMsgHandlerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<generated.LamportMsgHandler.Empty> getValue(
        generated.LamportMsgHandler.Get request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetValueMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<generated.LamportMsgHandler.Empty> putValue(
        generated.LamportMsgHandler.Put request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutValueMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<generated.LamportMsgHandler.Empty> ackEvent(
        generated.LamportMsgHandler.Ack request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAckEventMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_VALUE = 0;
  private static final int METHODID_PUT_VALUE = 1;
  private static final int METHODID_ACK_EVENT = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final lamportMsgHandlerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(lamportMsgHandlerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_VALUE:
          serviceImpl.getValue((generated.LamportMsgHandler.Get) request,
              (io.grpc.stub.StreamObserver<generated.LamportMsgHandler.Empty>) responseObserver);
          break;
        case METHODID_PUT_VALUE:
          serviceImpl.putValue((generated.LamportMsgHandler.Put) request,
              (io.grpc.stub.StreamObserver<generated.LamportMsgHandler.Empty>) responseObserver);
          break;
        case METHODID_ACK_EVENT:
          serviceImpl.ackEvent((generated.LamportMsgHandler.Ack) request,
              (io.grpc.stub.StreamObserver<generated.LamportMsgHandler.Empty>) responseObserver);
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

  private static abstract class lamportMsgHandlerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    lamportMsgHandlerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return generated.LamportMsgHandler.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("lamportMsgHandler");
    }
  }

  private static final class lamportMsgHandlerFileDescriptorSupplier
      extends lamportMsgHandlerBaseDescriptorSupplier {
    lamportMsgHandlerFileDescriptorSupplier() {}
  }

  private static final class lamportMsgHandlerMethodDescriptorSupplier
      extends lamportMsgHandlerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    lamportMsgHandlerMethodDescriptorSupplier(String methodName) {
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
      synchronized (lamportMsgHandlerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new lamportMsgHandlerFileDescriptorSupplier())
              .addMethod(getGetValueMethod())
              .addMethod(getPutValueMethod())
              .addMethod(getAckEventMethod())
              .build();
        }
      }
    }
    return result;
  }
}
