package pt.unl.fct.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.api.ProductsApi;
import pt.unl.fct.models.Product;
import pt.unl.fct.services.ProductService;

@RestController
public class ProductsController implements ProductsApi {

    @Autowired
    private ProductService productService;

    @Override
    public Product getProduct(@PathVariable("productId") Long productId) {
        return productService.getProduct(productId);
    }

}
