package com.propets.lostfound.kafka.dto;

import java.util.List;

public class PostDto {
    private String action;
    private long id;
    private String postType;
    private List<String>tags;

    public PostDto() {
    }

    public PostDto(String action, long id, String postType, List<String> tags) {
        this.action=action;
        this.id = id;
        this.postType = postType;
        this.tags = tags;
    }

    public String getAction() {
        return action;
    }

    public long getId() {
        return id;
    }

    public String getPostType() {
        return postType;
    }

    public List<String> getTags() {
        return tags;
    }
}
