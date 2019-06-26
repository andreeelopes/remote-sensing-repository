package pt.unl.fct.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pt.unl.fct.models.Product;

public interface ProductsApi {

    @ApiOperation(value = "Get product by ID", nickname = "getProduct", notes = "Returns the product with the given ID", response = Product.class, tags = {"product",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Product.class),
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Product not found")})
    @RequestMapping(value = "/products/{productId}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    Product getProduct(@ApiParam(value = "Product ID", required = true) @PathVariable("productId") Long productId);


}
