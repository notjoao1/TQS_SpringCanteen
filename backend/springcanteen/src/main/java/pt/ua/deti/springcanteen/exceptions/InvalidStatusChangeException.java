package pt.ua.deti.springcanteen.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidStatusChangeException extends ResponseStatusException {
  public InvalidStatusChangeException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
