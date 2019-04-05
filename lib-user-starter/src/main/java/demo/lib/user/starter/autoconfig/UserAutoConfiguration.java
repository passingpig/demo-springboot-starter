package demo.lib.user.starter.autoconfig;

import demo.lib.user.service.DefaultUserServiceImpl;
import demo.lib.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UserProperties.class)
public class UserAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(UserAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public UserService userService(UserProperties userProperties) {
        DefaultUserServiceImpl userService = new DefaultUserServiceImpl();
        userService.setAppId(userProperties.getAppId());
        userService.setAppSecret(userProperties.getAppSecret());
        logger.info("配置初始化，AppId={}, AppSecret={}", userProperties.getAppId(), userProperties.getAppSecret());
        return userService;
    }
}