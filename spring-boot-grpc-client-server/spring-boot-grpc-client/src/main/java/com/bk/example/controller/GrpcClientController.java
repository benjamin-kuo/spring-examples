package com.bk.example.controller;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bk.example.service.GrpcClientService;

@RestController
public class GrpcClientController {

  @Resource
  private GrpcClientService grpcClientService;

  @RequestMapping("/")
  public String printMessage(@RequestParam(defaultValue = "BK") String name) {
    return grpcClientService.sendMessage(name);
  }
}
