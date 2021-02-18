package order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    public Long newProduct(String name) throws ProductException {

        // If (name is used):   exception
        // Else:                save product
        if (productRepository.findByName(name).isPresent()) {
            throw new ProductException("Product " + name + " already exists");
        }

        Product product = new Product(name);
        productRepository.save(product);
        return product.getId();
    }

    public void destroyProduct(Long id) throws ProductException {

        // If (product not found):   exception
        // Else:                     delete product
        if (!productRepository.findById(id).isPresent()) {
            throw new ProductException("Product " + id.toString() + " not found");
        }

        productRepository.deleteById(id);
	}
}