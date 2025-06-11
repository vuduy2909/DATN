package com.capstone.wellnessnavigatorgym.security.jwt;

import com.capstone.wellnessnavigatorgym.security.userprinciple.UserDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtility jwtUtility;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailService userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            logger.debug("JWT Token: {}", jwt != null ? "Present" : "Not found");
            
            if (jwt != null) {
                boolean isValid = jwtUtility.validateJwtToken(jwt);
                String username = null;
                
                if (isValid) {
                    username = jwtUtility.getUserNameFromJwtToken(jwt);
                    logger.debug("JWT Token validated with JwtUtility for user: {}", username);
                } else {
                    // Try with JwtTokenProvider
                    isValid = jwtTokenProvider.validateToken(jwt);
                    if (isValid) {
                        username = jwtTokenProvider.getUsernameFromToken(jwt);
                        logger.debug("JWT Token validated with JwtTokenProvider for user: {}", username);
                    }
                }
                
                logger.debug("JWT Token validation result: {}", isValid);
                
                if (isValid && username != null) {
                    try {
                        UserDetails userDetails = userDetailService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("User authentication set successfully for: {}", username);
                    } catch (Exception userDetailsException) {
                        // This catches banned customer exceptions from UserDetailService
                        logger.warn("Authentication failed for user {}: {}", username, userDetailsException.getMessage());
                        SecurityContextHolder.clearContext();
                        
                        // If this is a banned customer, return 401 immediately
                        if (userDetailsException.getMessage().contains("banned")) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Customer account is banned\",\"message\":\"Your account has been banned\"}");
                            return;
                        }
                    }
                } else {
                    logger.warn("JWT Token validation failed with both providers");
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }
        return null;
    }
}
