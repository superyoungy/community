package com.yc.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like_entity";
    private static final String PREFIX_USER_LIKE = "like_user";
    private static final String PREFIX_ENTITY_FOLLOWEE = "followee";
    private static final String PREFIX_ENTITY_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";

    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT  + entityId + SPLIT + entityType;
    }

    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_ENTITY_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_ENTITY_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    public static String getKaptchaKey(String kaptchaOwner) {
        return PREFIX_KAPTCHA + ":" + kaptchaOwner;
    }

    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + ":" + ticket;
    }

    public static String getUserKey(int userId) {
        return PREFIX_USER + ":" + userId;
    }
}
