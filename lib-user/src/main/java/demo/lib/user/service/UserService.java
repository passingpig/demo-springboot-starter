package demo.lib.user.service;

import demo.lib.user.User;

public interface UserService {
    User getByToken(String token);
}
