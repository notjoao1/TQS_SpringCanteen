package pt.ua.deti.springcanteen.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidOrderException extends ResponseStatusException {
    public InvalidOrderException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

}
