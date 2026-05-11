package sem4.edustreambe.configuration;

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
        // Kích hoạt simple broker để gửi thông báo ngược lại cho client
        // /topic: cho thông báo chung
        // /queue: cho thông báo riêng tư (user-specific)
        config.enableSimpleBroker("/topic", "/queue");
        
        // Tiền tố cho các message gửi từ client lên server
        config.setApplicationDestinationPrefixes("/app");
        
        // Tiền tố cho thông báo riêng tư (Spring sẽ tự chuyển hướng /user/...)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint để client kết nối tới
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép mọi origin trong phát triển, nên thu hẹp trong sản xuất
                .withSockJS();
    }
}
