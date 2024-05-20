package pt.ua.deti.springcanteen.config;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pt.ua.deti.springcanteen.entities.Order;

@Configuration
public class QueueConfig {
    private static final int QUEUE_CAPACITY = 120;

    @Bean
    public Queue<Order> regularOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<Order> priorityOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }
}
