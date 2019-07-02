package pt.unl.fct.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import pt.unl.fct.model.FetchData;
import pt.unl.fct.model.Product;

import javax.validation.Valid;
import java.io.IOException;

public interface ProductsApi {
    @ApiOperation(value = "Get the list of products based on the query", nickname = "getProducts", notes = "", response = Product.class, responseContainer = "List", tags = {"products-controller",})
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

    @ApiOperation(value = "Fetch data object of the given product", nickname = "fetchProductData", notes = "Check the progress in the status field", tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data download scheduled", response = FetchData.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Product or data object do not exist"),
            @ApiResponse(code = 409, message = "Data is already stored locally"),})
    @RequestMapping(value = "/products/{productId}/data/{dataId}",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.PUT)
    void fetchProductData(FetchData fetchData) throws IOException;


    @ApiOperation(value = "Adds a new product", nickname = "addProduct", notes = "", tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Added a new product"),
            @ApiResponse(code = 405, message = "Invalid input")})
    @RequestMapping(value = "/products",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    void addProduct(@ApiParam(value = "Product object to be added", required = true)
                    @Valid @RequestBody Product product);

    @ApiOperation(value = "Add metadata to product", nickname = "updateProduct", notes = "", tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Product updated successfully"),
            @ApiResponse(code = 404, message = "Product does not exist"),
            @ApiResponse(code = 405, message = "Invalid input")})
    @RequestMapping(value = "/products/{productId}",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    void updateProduct(
            @ApiParam(value = "Product ID", required = true) @PathVariable("productId") String productId,
            @ApiParam(value = "Product object to be updated", required = true) @Valid @RequestBody Product product);


    @ApiOperation(value = "Delete product data", nickname = "deleteProductData", notes = "", tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 404, message = "Product or data not found")})
    @RequestMapping(value = "/products/{productId}/data/{dataId}",
            method = RequestMethod.DELETE)
    void deleteProductData(@ApiParam(value = "ID of the product", required = true) @PathVariable("productId") Long productId,
                           @ApiParam(value = "ID of the data to be deleted", required = true) @PathVariable("dataId") Long dataID);


}
