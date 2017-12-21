package com.sooszy.controller;

import com.sooszy.annotation.CustomCrossOrigin;
import com.sooszy.common.AuthConstants;
import com.sooszy.common.Const;
import com.sooszy.common.ResponseCode;
import com.sooszy.common.ServerResponse;
import com.sooszy.pojo.User;
import com.sooszy.service.IUserService;
import com.sooszy.shiro.token.ITokenProcessor;
import com.sooszy.shiro.token.TokenParameter;
import com.sooszy.util.MD5Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
//@CustomCrossOrigin
@RequestMapping("/user/")
@Api
public class UserController {
    @Autowired
    private IUserService iUserService;
    //为网页版本的登录Controller指定webTokenProcessor 相应的移动的指定为maTokenProcessor

    @Autowired
    protected ITokenProcessor webTokenProcessor;

    @ApiOperation(value = "web表单登录", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户标识", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "password", value = "用户密码", required = true, dataType = "string", paramType = "form"),
    })
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    public ServerResponse<User> login(HttpServletResponse response,HttpServletRequest request) throws IOException {
        /*ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;*/
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        ServerResponse<User> serverResponse = iUserService.login(username, password);
        if (serverResponse.isSuccess()){

            TokenParameter tp = new TokenParameter();
            //设置用户标识
            tp.setUserName(username);
            //设置登录时间
            tp.setLoginTs(String.valueOf(System.currentTimeMillis()));
            tp.getExt().put(AuthConstants.ExtendConstants.PARAM_USERTYPE, "1");
            Cookie[] cookies = webTokenProcessor.getCookieFromTokenParameter(tp);
            for (Cookie cookie : cookies) {
                response.addCookie(cookie);
            }
            return ServerResponse.createBySuccessMessage("登录成功！");
        } else {
            return ServerResponse.createByErrorMessage("用户名或密码错误！");
        }
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    public ServerResponse<User> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    public ServerResponse<String> register(HttpServletRequest request, HttpSession session, User user) {
        return iUserService.register(user);
    }

    @ApiOperation(value = "测试", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @RequestMapping(value = "test.do", method = RequestMethod.GET)
    public ServerResponse<String> test(HttpServletRequest request, HttpSession session, User user) {
        return ServerResponse.createBySuccessMessage("测试成功！");
    }

    @ApiOperation(value = "测试", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @RequestMapping(value = "logintest.do", method = RequestMethod.GET)
    public ServerResponse<String> logintest(HttpServletRequest request, HttpSession session, User user) {
        return ServerResponse.createBySuccessMessage("测试成功！");
    }
}
