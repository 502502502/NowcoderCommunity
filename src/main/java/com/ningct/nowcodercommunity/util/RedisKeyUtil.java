package com.ningct.nowcodercommunity.util;

public class RedisKeyUtil {
    public static final String SPLIT = ":";
    public static final String PREFIX_RNTITY_LIKE = "like:entity";
    public static final String PREFIX_USER_LIKE = "like:user";

    public static final String PREFIX_FOLLOWEE = "followee";

    public static final String PREFIX_FOLLOWER = "follower";

    public static final String PREFIX_KAPTCHA = "kaptcha";

    public static final String PREFIX_TICKET = "ticket";

    public static final String PREFIX_USER = "user";
    public static final String PREFIX_UV = "uv";
    public static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";

    //生成点赞关键词
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_RNTITY_LIKE + SPLIT +entityType +SPLIT +entityId;
    }
    //生成用户点赞关键词
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE +SPLIT +userId;
    }

    //某个用户关注的实体
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE +SPLIT +userId +SPLIT +entityType;
    }
    //某个实体拥有的粉丝
    public  static  String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER +SPLIT +entityType +SPLIT +entityId;
    }

    //登录验证码
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA +SPLIT +owner;
    }
    //登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET +SPLIT +ticket;
    }
    //登录用户
    public static String getUserKey(int userId){
        return PREFIX_USER +SPLIT +userId;
    }

    //生成UV关键词
    public static String getUVKey(String date){
        return PREFIX_UV +SPLIT +date;
    }
    //生成时间段UV关键词
    public static String getUVKey(String startDate, String endDate){
        return PREFIX_UV +SPLIT +startDate + SPLIT + endDate;
    }
    public static String getDAUKey(String date){
        return PREFIX_DAU +SPLIT +date;
    }
    public static String getDAUKey(String startDate, String endDate){
        return PREFIX_DAU +SPLIT +startDate + SPLIT + endDate;
    }
    public static String getScorePostRefreshKey(){
        return PREFIX_POST +SPLIT +"score";
    }
}


