package com.ningct.nowcodercommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ningct.nowcodercommunity.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    //通过id查询用户
    User selectById(@Param("id") int id);

    //通过姓名查询用户
    User selectByName(@Param("username") String username);

    //通过邮箱查询用户
    User selectByEmail(@Param("email") String email);

    //插入新增用户
    int insertUser(User user);

    //更新用户的状态
    int updateStatus(@Param("id") int id, @Param("status") int status);

    //更新头像
    int updateHeader(@Param("id") int id, @Param("headerUrl") String headerUrl);

    //更新密码
    int updatePassword(@Param("id") int id, @Param("password") String password);
}
