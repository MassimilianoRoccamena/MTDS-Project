package user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/delivery")
public class DeliveryManController {

    @Autowired
    UserService userService;

    @PostMapping("/register/{name}")
    public Long registerDeliveryMan(@PathVariable String name) {
        try {
            return userService.newDeliveryMan(name);
        } catch (UserException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
	}
}