package pt.unl.fct.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import pt.unl.fct.model.Product;

public interface ProductsApi {
    @ApiOperation(value = "Get the list of products based on the query", nickname = "getProducts", notes = "WARNING - MongoDB inverts the geojson coordinates order", response = Product.class, responseContainer = "List", tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Product.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Malformed query")}
    )
    @RequestMapping(value = "/products/query",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    Page<Product> getProducts(String mongoQuery, String page, String pageSize);


    @ApiOperation(value = "Get product by ID", nickname = "getProduct", notes = "Returns the product with the given ID", response = Object.class, tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Object.class),
            @ApiResponse(code = 404, message = "Product with id <id> does not exist")})
    @RequestMapping(value = "/products/{productId}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    Object getProduct(String productId);


}
