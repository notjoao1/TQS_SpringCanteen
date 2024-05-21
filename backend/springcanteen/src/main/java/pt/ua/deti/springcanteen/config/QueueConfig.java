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
    public Queue<Order> regularIdleOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<Order> priorityIdleOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<Order> regularPreparingOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<Order> priorityPreparingOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<Order> regularReadyOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<Order> priorityReadyOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }
}
