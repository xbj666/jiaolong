package com.zbf.webSocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter getServerEndpointExporter(){

        return new ServerEndpointExporter ();
    }

}
