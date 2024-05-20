package pt.ua.deti.springcanteen.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import pt.ua.deti.springcanteen.service.EmployeeService;
import pt.ua.deti.springcanteen.service.JwtService;

@Component
@AllArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {
    final JwtService jwtService;
    final EmployeeService employeeService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
      return message;
      /* System.out.println("Made it here!!! intercepting websocket stuff");
      StompHeaderAccessor accessor =
          MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
      if (StompCommand.CONNECT.equals(accessor.getCommand())) {
        String jwt = accessor.getFirstNativeHeader("Authorization").substring(7);
        String userEmail = jwtService.extractSubject(jwt);

        UserDetails userDetails = employeeService.userDetailsService().loadUserByUsername(userEmail);
        if (userEmail != null && !userEmail.isEmpty() && jwtService.isTokenValid(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                    null, userDetails.getAuthorities());
            accessor.setUser(authToken);
          }
      }
      return message; */
    }
}