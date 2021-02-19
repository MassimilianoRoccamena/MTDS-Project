package user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    UserService userService;

    @PostMapping("/register/{name}/{address}")
    public Long registerCustomer(@PathVariable String name, @PathVariable String address) {
        try {
            return userService.newCustomer(name, address);
        } catch (UserException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
	}
}