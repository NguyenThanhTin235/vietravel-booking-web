package com.vietravel.booking.config;

import com.vietravel.booking.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService){
        this.jwtService=jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain)throws ServletException,IOException{
        String token=null;

        var header=request.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer ")){
            token=header.substring(7);
        }else{
            var cookies=request.getCookies();
            if(cookies!=null){
                for(Cookie c:cookies){
                    if("accessToken".equals(c.getName())){
                        token=c.getValue();
                        break;
                    }
                }
            }
        }

        if(token==null || token.isBlank()){
            filterChain.doFilter(request,response);
            return;
        }

        try{
            var claims=jwtService.parseClaims(token);
            var userId=String.valueOf(claims.get("sub"));
            var role=String.valueOf(claims.get("role"));

            var auth=new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_"+role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }catch(Exception ignored){
        }

        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path=request.getServletPath();
        return path.equals("/")
                || path.equals("/favicon.ico")
                || path.startsWith("/auth")
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/images")
                || path.startsWith("/vendor")
                || path.equals("/health");
    }
}
