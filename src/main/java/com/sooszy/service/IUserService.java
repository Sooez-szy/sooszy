package com.sooszy.service;

import com.sooszy.common.ServerResponse;
import com.sooszy.pojo.User;

public interface IUserService {
    ServerResponse<User> login(String username, String password);
    ServerResponse<String> register(User user);
    User getByUsername(String username);

}
