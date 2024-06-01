package pt.ua.deti.springcanteen.repository;

import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.impl.IMenuService;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@FlywayTest
class OrderRepositoryTest {

  @Autowired
  private OrderRepository orderRepository;

  private static final Logger logger = LoggerFactory.getLogger(IMenuService.class);

  // Just to verify if the repository is working (already implemented by Spring Data JPA)
  // no need to test findByIsPaid(false) as is all implemented by Spring Data JPA
  @Test
  void whenFindByNotPaid_thenReturnNotPaidOrders(){
    logger.info("Testing OrderRepository - findByIsPaid(true)");
    assertThat(orderRepository.findByIsPaid(true))
      .isNotNull()
      .hasSize(6);
  }
}
