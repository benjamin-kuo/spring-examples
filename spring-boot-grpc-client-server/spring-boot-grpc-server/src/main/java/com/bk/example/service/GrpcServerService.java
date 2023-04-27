package com.bk.example.service;

import com.bk.example.protobuf.proto.HelloReply;
import com.bk.example.protobuf.proto.HelloRequest;
import com.bk.example.protobuf.proto.SimpleGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;


@GrpcService
public class GrpcServerService extends SimpleGrpc.SimpleImplBase {

  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    System.out.println("GrpcServerService..." + request.getName());
    HelloReply reply = HelloReply.newBuilder().setMessage("Hello ==> " + request.getName()).build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }
}
