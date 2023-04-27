package com.bk.example.service;

import org.springframework.stereotype.Service;
import com.bk.example.protobuf.proto.HelloReply;
import com.bk.example.protobuf.proto.HelloRequest;
import com.bk.example.protobuf.proto.SimpleGrpc.SimpleBlockingStub;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class GrpcClientService {

  @GrpcClient("spring-boot-grpc-server")
  SimpleBlockingStub simpleBlockingStub;

  public String sendMessage(String name) {
    try {
      HelloReply response = simpleBlockingStub.sayHello(HelloRequest.newBuilder().setName(name).build());
      return response.getMessage();
    } catch (StatusRuntimeException e) {
      return "FAILED with " + e.getStatus().getCode();
    }
  }
}
