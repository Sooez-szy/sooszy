package com.sooszy.shiro.filter;

import com.sooszy.common.AuthConstants;
import com.sooszy.shiro.token.ITokenProcessor;
import com.sooszy.shiro.token.TokenFactory;
import com.sooszy.util.CookieUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoutFilter extends StatelessLogoutFilter {

	@Autowired
	private TokenFactory tokenFactory;

	@Override
    protected void doLogout(HttpServletRequest request, HttpServletResponse response) {
		String token = CookieUtil.findCookieValue(request.getCookies(), AuthConstants.PARAM_TOKEN);
		String userId = CookieUtil.findCookieValue(request.getCookies(), AuthConstants.PARAM_USERNAME);
		if(StringUtils.isNotBlank(token)){
			ITokenProcessor tokenProcessor = tokenFactory.getTokenProcessor(token);
			Cookie[] cookies = tokenProcessor.getLogoutCookie(token, userId);
			for (int i = 0; i < cookies.length; i++) {
				response.addCookie(cookies[i]);
			}
		}
    }

}
