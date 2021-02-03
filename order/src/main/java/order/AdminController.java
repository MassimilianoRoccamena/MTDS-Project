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
        if (productRepository.findByName(name).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product " + name + " already exists");
        }

        Product product = new Product(name);
        productRepository.save(product);
        return product.getId();
    }
    
    @DeleteMapping("/delete/{id}")
	public void deleteProduct(@PathVariable Long id) {
        if (!productRepository.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product " + id.toString() + " not found");
        }

        productRepository.deleteById(id);
	}
}