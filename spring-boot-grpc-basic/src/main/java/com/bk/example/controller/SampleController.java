package com.bk.example.controller;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bk.example.protobuf.proto.DemoProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

@RestController
public class SampleController {

  private final static Logger logger = LoggerFactory.getLogger(SampleController.class);

  @GetMapping("sample")
  public String sampleGet() {

    //初始化數據
    DemoProto.Demo.Builder demo = DemoProto.Demo.newBuilder();
    demo.setId(1).setCode("00").setName("BK").build();

    //序列化
    DemoProto.Demo build = demo.build();
    //轉換成字節數組
    byte[] s = build.toByteArray();

    logger.info("protobuf bytes[]:{}", Arrays.toString(s));
    logger.info("protobuf serialized length :{}", s.length);

    DemoProto.Demo demo1 = null;
    String jsonObject = null;
    try {
      //反序列化
      demo1 = DemoProto.Demo.parseFrom(s);

      logger.info("getId:{}", demo1.getId());
      logger.info("getName:{}", demo1.getName());

      //to json
      jsonObject = JsonFormat.printer().print(demo1);

    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }

    logger.info("\nprotobuf bytes[]:{}", jsonObject);
    logger.info("protobuf serialized length :{}", jsonObject.getBytes().length);

    return "hello protobuf";
  }
}
