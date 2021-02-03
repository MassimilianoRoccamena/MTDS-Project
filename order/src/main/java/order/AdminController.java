package order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    KafkaService kafkaService;
    
    @PostMapping("/add/{name}")
	public Long addProduct(@PathVariable String name) {
        try {

            if (productRepository.findByName(name).isPresent()) {
                throw new OrderException("Product " + name + " already exists");
            }
    
            Product product = new Product(name);
            productRepository.save(product);
            return product.getId();

        } catch (OrderException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
	public void deleteProduct(@PathVariable Long id) {
        try {

            if (!productRepository.findById(id).isPresent()) {
                throw new OrderException("Product " + id.toString() + " not found");
            }
    
            productRepository.deleteById(id);

        } catch (OrderException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
	}
}