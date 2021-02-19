package order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    ProductService productService;

    @PostMapping("/add/{name}")
    public Long createProduct(@PathVariable String name) {
        try {
            return productService.newProduct(name);
        } catch (ProductException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public void deleteProduct(@PathVariable Long id) {
        try {
            productService.destroyProduct(id);
        } catch (ProductException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
	}
}