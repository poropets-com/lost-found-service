package com.propets.lostfound.kafka.service;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface IKafkaProducer {
    String EXCEPTION_LOG="exceptionlog";
    @Output(EXCEPTION_LOG)
    MessageChannel exceptionlog();

    String LOST_FOUND_POSTS="lostFoundPosts";
    @Output(LOST_FOUND_POSTS)
    MessageChannel lostFoundPosts();
    
}
