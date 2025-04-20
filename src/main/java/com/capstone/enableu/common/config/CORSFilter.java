package com.capstone.enableu.common.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CORSFilter.class);

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        String origin = request.getHeader("Origin");

        response.setHeader("Access-Control-Allow-Origin", origin    );
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, User-Token, Access-Token, Authorization, Basic-Authorization");
        if (CorsUtils.isPreFlightRequest(request)) {
            return;
        }

        chain.doFilter(req, res);

    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }

}

