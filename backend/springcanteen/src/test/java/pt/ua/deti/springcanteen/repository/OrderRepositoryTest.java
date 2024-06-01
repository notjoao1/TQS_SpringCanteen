package pt.ua.deti.springcanteen.repository;

import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.impl.IMenuService;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

  @Container
  public static PostgreSQLContainer container = new PostgreSQLContainer<>("postgres:latest")
    .withUsername("testname")
    .withPassword("testpassword")
    .withDatabaseName("sc_test");

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", container::getJdbcUrl);
    registry.add("spring.datasource.password", container::getPassword);
    registry.add("spring.datasource.username", container::getUsername);
  }


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
