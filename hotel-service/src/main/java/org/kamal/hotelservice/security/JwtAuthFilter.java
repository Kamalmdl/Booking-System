package org.kamal.hotelservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.isTokenValid(token)) {
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);
            Long userId = jwtService.extractUserId(token);


            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            var authenticatedUser = new AuthenticatedUser(userId, email);

            var authentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println(
                    "AUTH = " +
                            SecurityContextHolder.getContext().getAuthentication()
            );

            System.out.println(
                    "AUTHORITIES = " +
                            SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getAuthorities()
            );
        }

        filterChain.doFilter(request, response);
    }
}