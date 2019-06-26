package pt.unl.fct.repositories;

import org.springframework.data.repository.CrudRepository;
import pt.unl.fct.models.Product;


public interface ProductRepository extends CrudRepository<Product, Long> {
}
