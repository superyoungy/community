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
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";

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

    // 日访问量
    public static String getUvKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 日期区间访问量
    public static String getUvKey(String beginDate, String endDate) {
        return PREFIX_UV + SPLIT + beginDate + SPLIT + endDate;
    }

    //日活跃用户
    public static String getDauKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    //日期区间活跃用户
    public static String getDauKey(String beginDate, String endDate) {
        return PREFIX_DAU + SPLIT + beginDate + SPLIT + endDate;
    }

    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
