package shipping;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public class OrderException extends Exception {

    private static final long serialVersionUID = 1L;

    @Getter
    private HttpStatus httpStatus;
    
    public OrderException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
