package pt.ua.deti.springcanteen.config;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pt.ua.deti.springcanteen.dto.OrderEntry;
import pt.ua.deti.springcanteen.entities.Order;

@Configuration
public class QueueConfig {
    private static final int QUEUE_CAPACITY = 120;

    @Bean
    public Queue<OrderEntry> regularIdleOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<OrderEntry> priorityIdleOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<OrderEntry> regularPreparingOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<OrderEntry> priorityPreparingOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<OrderEntry> regularReadyOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Bean
    public Queue<OrderEntry> priorityReadyOrders() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }
}
