package com.ningct.nowcodercommunity.actuator;

import com.ningct.nowcodercommunity.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);
    @Resource
    private DataSource dataSource;

    @ReadOperation
    public String checkConnection(){
        try (
                Connection connection = dataSource.getConnection();
        ) {
            return CommunityUtil.getJSONString(0,"获取连接正常");

        } catch (Exception e) {
            logger.error("获取连接失败" +e.getMessage());
            return CommunityUtil.getJSONString(1,"获取连接失败");
        }
    }
}
