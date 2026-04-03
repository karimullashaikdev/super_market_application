package com.karim.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// Clients subscribe to topics under /topic
		config.enableSimpleBroker("/topic");
		// Messages sent from clients go to /app prefix
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// WebSocket endpoint — clients connect to /ws
		// SockJS fallback for browsers that don't support WebSocket
		registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
	}
}