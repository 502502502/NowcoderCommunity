package com.ningct.nowcodercommunity.config;


import com.ningct.nowcodercommunity.quartz.ScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {
    //注入JobDetail
    @Bean
    public JobDetailFactoryBean ScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(ScoreRefreshJob.class);
        factoryBean.setName("ScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }
    //注入Trigger
    @Bean
    public SimpleTriggerFactoryBean ScoreRefreshJobTrigger(JobDetail scoreRefreshJboDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(scoreRefreshJboDetail);
        factoryBean.setName("ScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000*60*2);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
