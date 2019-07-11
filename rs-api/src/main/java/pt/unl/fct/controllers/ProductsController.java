package pt.unl.fct.controllers;


import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.api.ProductsApi;
import pt.unl.fct.errors.BadRequestException;
import pt.unl.fct.model.FetchData;
import pt.unl.fct.model.Product;
import pt.unl.fct.services.ProductService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
public class ProductsController implements ProductsApi {

    @Autowired
    private ProductService productService;

    @Override
    public Object getProduct(@ApiParam(value = "Product ID", required = true) @PathVariable("productId") String productId) {
        return productService.getProduct(productId);
    }

    @Override
    public void fetchProductData(@ApiParam(value = "Data object to be fetched") @Valid @RequestBody FetchData fetchData) throws IOException {
        productService.fetchProductData(fetchData);
    }

    @Override
    public void addProduct(@Valid Product product) {

    }

    @Override
    public void updateProduct(String productId, @Valid Product product) {

    }

    @Override
    public void deleteProductData(Long productId, Long dataID) {

    }

    @Override
    public List<Object> getSchema() {
        return productService.getSchema();
    }

    @Override
    public Page<Product> getProducts(@ApiParam(value = "MongoDB Query") @Valid @RequestBody(required = false) String mongoQuery,
                                     @ApiParam(value = "Page") @RequestParam(defaultValue = "0", required = false) String page,
                                     @ApiParam(value = "Page Size") @RequestParam(defaultValue = "100", required = false) String pageSize) {
        try {
            new BasicQuery(mongoQuery);
        } catch (Exception e) {
            throw new BadRequestException("Malformed query");
        }

        return productService.getProducts(mongoQuery, Integer.parseInt(page), Integer.parseInt(pageSize));
    }

}
