package com.onedrivex.common;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(filterName = "sessionFilter",urlPatterns = {"/admin","/admin/*"})
public class LoginFilter implements Filter{
	
	String NO_LOGIN = "您还未登录";
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpSession session = request.getSession(false);
		if(session != null && session.getAttribute("isLogin") != null){
            filterChain.doFilter(request, response);
        }else{
            String requestType = request.getHeader("X-Requested-With");
            //判断是否是ajax请求
            if(requestType!=null && "XMLHttpRequest".equals(requestType)){
                response.getWriter().write(this.NO_LOGIN);
            }else{
                //重定向到登录页(需要在static文件夹下建立此html文件)
                response.sendRedirect(request.getContextPath()+"/login");
            }
            return;
        }
	}

}
