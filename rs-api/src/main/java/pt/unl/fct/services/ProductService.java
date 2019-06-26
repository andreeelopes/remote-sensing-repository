package pt.unl.fct.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.unl.fct.errors.NotFoundException;
import pt.unl.fct.models.Product;
import pt.unl.fct.repositories.ProductRepository;

import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product getProduct(Long productId) {

        Optional<Product> comment = productRepository.findById(productId);
        if (comment.isPresent()) {
            return comment.get();
        } else throw new NotFoundException(String.format("Product with id %d does not exist", productId));
    }

}
