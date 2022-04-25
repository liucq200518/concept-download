package com.github.linyuzai.concept.sample.connection.loadbalance;

import com.github.linyuzai.connection.loadbalance.websocket.concept.WebSocketLoadBalanceConcept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/concept-connection-loadbalance")
public class ConnectionLoadBalanceController {

    @Autowired
    private WebSocketLoadBalanceConcept concept;

    @GetMapping("message")
    public void sendMessage(@RequestParam String msg) {
        Map<String, String> headers = new LinkedHashMap<>();
        //发送给订阅了设备更新的客户端
        //headers.put("type","device_update");
        concept.send(msg, headers);
    }
}
