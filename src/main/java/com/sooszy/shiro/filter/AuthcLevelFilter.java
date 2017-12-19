package com.sooszy.shiro.filter;

import com.sooszy.common.ResponseCode;
import com.sooszy.common.ServerResponse;
import com.sooszy.util.JsonUtil;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class AuthcLevelFilter extends FormAuthenticationFilter {
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        if(this.isLoginRequest(request, response)) {
            if(this.isLoginSubmission(request, response)) {
                return this.executeLogin(request, response);
            } else {
                return true;
            }
        } else {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;utf-8");
            response.getWriter().print(JsonUtil.obj2String(ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc())));
            return false;
        }
    }


}
