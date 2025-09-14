package com.cs301.crm.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Try to get the accessToken cookie
        HttpCookie accessTokenCookie = exchange.getRequest().getCookies()
                .getFirst("accessToken");

        if (accessTokenCookie != null) {
            // Create a modified exchange with the new Authorization header
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("Authorization", "Bearer " + accessTokenCookie.getValue())
                            .build())
                    .build();

            return chain.filter(modifiedExchange);
        }

        // If no cookie is found, continue with the original exchange
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Set the filter order to run before other filters
        return Ordered.HIGHEST_PRECEDENCE;
    }
}