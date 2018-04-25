package team.benchem.demo.rabbitmq.consumer.service;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
public class RabbitConsumer implements ApplicationRunner {
    private static final String EXCHANGE_NAME = "team.benchem";
    private static final String QUEUE_NAME = "demoQueue";
    private static final String ROUTING_KEY= "ALL";

    @Value("${spring.rabbitmq.host}")
    private String RABBITMQ_HOST;

    @Value("${spring.rabbitmq.port}")
    private Integer RABBITMQ_PORT;

    @Value("${spring.rabbitmq.username}")
    private String RABBITMQ_USERNAME;

    @Value("${spring.rabbitmq.password}")
    private  String RABBITMQ_PASSWORD;

    @Value("${spring.rabbitmq.virtual-host}")
    private String RABBITMQ_VIRTUALHOST;

    ConnectionFactory factory;
    Connection mqConnection;
    Channel currentChannel;
    Consumer consumer;

    protected Channel getChannel() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setPort(RABBITMQ_PORT);
        factory.setUsername(RABBITMQ_USERNAME);
        factory.setPassword(RABBITMQ_PASSWORD);
        mqConnection = factory.newConnection();
        currentChannel = mqConnection.createChannel();
        currentChannel.basicQos(64);
        return currentChannel;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(currentChannel != null && currentChannel.isOpen()){
            currentChannel.close();
        }
        if(mqConnection != null && mqConnection.isOpen()){
            mqConnection.close();
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Channel channel = getChannel();
        consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                JSONObject jObj = JSONObject.parseObject(new String(body));
                System.out.println(jObj.toJSONString());
                channel.basicAck(envelope.getDeliveryTag(), false);
                //channel.basicReject(envelope.getDeliveryTag(),true);
            }
        };
        channel.basicConsume(QUEUE_NAME, consumer);
    }
}
