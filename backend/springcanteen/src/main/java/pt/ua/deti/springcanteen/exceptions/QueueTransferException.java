package pt.ua.deti.springcanteen.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class QueueTransferException extends ResponseStatusException {
  public QueueTransferException(String message) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }
}
