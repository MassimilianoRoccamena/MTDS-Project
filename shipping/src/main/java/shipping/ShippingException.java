package shipping;

public class ShippingException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public ShippingException(String message) {
        super(message);
	}
}