package com.ningct.nowcodercommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ningct.nowcodercommunity.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper extends BaseMapper<DiscussPost> {
    //获取一页帖子
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit,@Param("orderMode") int orderMode);

    //查询帖子有多少行
    int selectDiscussPostRows(@Param("userId") int userId);

    //插入帖子
    int insertDiscussPost(DiscussPost post);

    //删除所有帖子
    int deleteallDiscussPost();

    //通过id查询帖子
    DiscussPost selectDiscussPostById(@Param("id") int id);

    //更新帖子的数量
    int updatePostCommentCount(@Param("id")int id, @Param("commentCount")int commentCount);

    //更新帖子的类型
    int updatePostType(@Param("id") int id, @Param("type") int type);

    //更新帖子的状态
    int updatePostStatus(@Param("id") int id, @Param("status") int status);

    //更新帖子的分数
    int updatePostScore(@Param("id") int id, @Param("score") double score);

}
