package demo.lib.user.service;

import demo.lib.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultUserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultUserServiceImpl.class);

    private String appId;

    private String appSecret;

    @Override
    public User getByToken(String token) {
        logger.info("DefaultUserServiceImpl--in--appId={}, appSecret={}, token={}", appId, appSecret, token);
        return new User();
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
}
