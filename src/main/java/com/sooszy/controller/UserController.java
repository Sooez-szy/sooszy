package com.sooszy.controller;

import com.sooszy.common.Const;
import com.sooszy.common.ResponseCode;
import com.sooszy.common.ServerResponse;
import com.sooszy.pojo.User;
import com.sooszy.service.IUserService;
import com.sooszy.util.MD5Util;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        /*ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;*/
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, md5Password);
        try{
            subject.login(token);//会跳到我们自定义的realm中
            return ServerResponse.createBySuccessMessage("登录成功！");
        }catch (UnknownAccountException e) {
            return ServerResponse.createBySuccessMessage("用户不存在或者密码错误！");
        } catch (IncorrectCredentialsException e) {
            return ServerResponse.createBySuccessMessage("用户不存在或者密码错误！");
        } catch (AuthenticationException e) {
            return ServerResponse.createBySuccessMessage(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(HttpServletRequest request,HttpSession session, User user) {
        return iUserService.register(user);
    }
}
