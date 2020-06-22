package com.propets.lostfound.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.propets.lostfound.kafka.dto.LogDto;
import com.propets.lostfound.kafka.dto.PostDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;

import java.time.LocalDateTime;
import java.util.List;

@EnableBinding(IKafkaProducer.class)
public class KafkaProducer {
    ObjectMapper mapper=new ObjectMapper();
    @Autowired
    IKafkaProducer producer;
    public void sendLog(String email,String message) {
        LogDto log=new LogDto(email, LocalDateTime.now(),message);
        String logJson= null;
        try {
            logJson = mapper.writeValueAsString(log);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        producer.exceptionlog().send(MessageBuilder.withPayload(logJson).build());
    }
    
    public void sendPost(String action, long id,String postType, List<String>tags){
        PostDto post=new PostDto(action,id,postType,tags);
        String postJson= null;
        try {
            postJson = mapper.writeValueAsString(post);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        producer.lostFoundPosts().send(MessageBuilder.withPayload(postJson).build());
    }
}
