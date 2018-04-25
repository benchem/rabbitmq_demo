package team.benchem.demo.rabbitmq.producer.service;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
public class RabbitProducer {

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

    public RabbitProducer(){

    }

    protected Channel getChannel() throws IOException, TimeoutException {
        if(factory == null){
            factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            factory.setPort(RABBITMQ_PORT);
            factory.setUsername(RABBITMQ_USERNAME);
            factory.setPassword(RABBITMQ_PASSWORD);
        }
        if(mqConnection == null || !mqConnection.isOpen()){
            if(currentChannel != null && currentChannel.isOpen()){
                currentChannel.close();
            }
            mqConnection = factory.newConnection();
            currentChannel = null;
        }
        if(currentChannel == null){
            currentChannel = mqConnection.createChannel();
            currentChannel.exchangeDeclare(EXCHANGE_NAME, "direct", true, false, null);
            currentChannel.queueDeclare(QUEUE_NAME, true, false,false,null);
            currentChannel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        }
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

    public void publicsh(JSONObject jsonObject) throws IOException, TimeoutException {
        Channel channel = getChannel();
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                jsonObject.toJSONString().getBytes());
    }
}
