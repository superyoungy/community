package com.yc.community.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
    private String topic;
    private int userId;
    private int entityTYpe;
    private int entityId;
    private int entityUserId;
    private Map<String, Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityTYpe;
    }

    public Event setEntityType(int entityTYpe) {
        this.entityTYpe = entityTYpe;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    @Override
    public String toString() {
        return "Event{" +
                "topic='" + topic + '\'' +
                ", userId=" + userId +
                ", entityTYpe=" + entityTYpe +
                ", entityId=" + entityId +
                ", entityUserId=" + entityUserId +
                '}';
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event addData(String name, Object value) {
        this.data.put(name, value);
        return this;
    }
}
