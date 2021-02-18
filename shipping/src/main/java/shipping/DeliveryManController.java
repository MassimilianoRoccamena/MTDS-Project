package shipping;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/delivery")
public class DeliveryManController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderService orderService;

    @PostMapping("/{userId}/deliver/{orderId}")
    public void deliverOrder(@PathVariable Long userId, @PathVariable Long orderId) {
        try {
            orderService.deliverOrder(userId, orderId);
        } catch (UserException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (OrderException e) {
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage(), e);
        }
    }

    @GetMapping("/{id}/orders")
    public Iterable<Order> getOrders(@PathVariable Long id) {
        return orderRepository.findAllByDeliveryManId(id);
    }
}