package order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    KafkaService kafkaService;
    
    @PostMapping("/add/{name}")
	public Long addProduct(@PathVariable String name) {
        Product product = new Product(name);
        productRepository.save(product);
        return product.getId();
    }
    
    @DeleteMapping("/delete/{id}")
	public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
	}
}