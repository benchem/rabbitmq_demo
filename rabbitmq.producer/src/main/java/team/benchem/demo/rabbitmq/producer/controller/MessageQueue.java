package team.benchem.demo.rabbitmq.producer.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.benchem.demo.rabbitmq.producer.service.RabbitProducer;

@RestController
@RequestMapping("/")
public class MessageQueue {

    @Autowired
    RabbitProducer producer;

    @RequestMapping("/send")
    public void send(@RequestParam String message){
        JSONObject msgPackage = new JSONObject();
        msgPackage.put("svrKey", "demo");
        msgPackage.put("message", message);
        try {
            producer.publicsh(msgPackage);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
