package fullcare.backend.security.jwt;

import fullcare.backend.security.jwt.exception.CustomJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static fullcare.backend.security.jwt.exception.JwtErrorCode.*;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String ACCESS_TOKEN_AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_AUTHORIZATION_HEADER = "Authorization_refresh";

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // todo 토큰이 없어도 모두가 접근이 가능한 API와 토큰을 이용해 인증을 해야만 접근 가능한 API를 어떻게 구분해야하는가?
        try{
            String refreshToken = getRefreshToken(request);
            String accessToken = getAccessToken(request);

            if(refreshToken != null && jwtTokenService.validateJwtToken(refreshToken)){
                Authentication authentication = jwtTokenService.getAuthentication(refreshToken);

                String[] newTokens = jwtTokenService.reIssueTokens(refreshToken, authentication); // * 리프레쉬 토큰이 DB와 일치 시 access, refresh 재발급
                String requestURI = request.getRequestURI();
                String successUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/token")
                        .queryParam("access_token", newTokens[0])
                        .queryParam("refresh_token", newTokens[1])
                        .queryParam("redirect_uri", requestURI)
                        .build().toUriString();
                System.out.println("successUrl = " + successUrl);

                response.sendRedirect(successUrl);
                return;
            } else if (accessToken != null && jwtTokenService.validateJwtToken(accessToken)){
                Authentication authentication = jwtTokenService.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else{
                //response.setStatus(401);
                setRequestAttribute(request, NOT_FOUND_TOKEN.getMessage());
            }
        }
        catch (CustomJwtException e){
            setRequestAttribute(request, e.getMessage());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        filterChain.doFilter(request,response);
    }

    private void setRequestAttribute(HttpServletRequest request, String message) {
        if (message.equals(MALFORMED_TOKEN.getMessage())) {
            request.setAttribute("message", MALFORMED_TOKEN.getMessage());
        }else if (message.equals(EXPIRED_TOKEN.getMessage())) {
            request.setAttribute("message", EXPIRED_TOKEN.getMessage());
        }else if (message.equals(UNSUPPORTED_TOKEN.getMessage())) {
            request.setAttribute("message", UNSUPPORTED_TOKEN.getMessage());
        }else if (message.equals(ILLEGAL_TOKEN.getMessage())) {
            request.setAttribute("message", ILLEGAL_TOKEN.getMessage());
        }else if (message.equals(NOT_FOUND_USER.getMessage())) {
            request.setAttribute("message", NOT_FOUND_USER.getMessage());
        }else if (message.equals(NOT_FOUND_TOKEN.getMessage())) {
            request.setAttribute("message", NOT_FOUND_TOKEN.getMessage());
        }
    }

    private String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(ACCESS_TOKEN_AUTHORIZATION_HEADER);

        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        return null;
    }

    private String getRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(REFRESH_TOKEN_AUTHORIZATION_HEADER);

        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

}
