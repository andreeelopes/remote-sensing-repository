package pt.unl.fct.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.unl.fct.model.CustomMetadata;
import pt.unl.fct.model.FetchData;
import pt.unl.fct.model.Product;

import javax.validation.Valid;
import java.io.File;
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


    @ApiOperation(value = "Get product data", nickname = "getProductData", notes = "Downloads the product data", response = ResponseEntity.class, tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ResponseEntity.class),
            @ApiResponse(code = 404, message = "Product data with id <id> does not exist"),
            @ApiResponse(code = 404, message = "Product data is not in the infraestructure"),
            @ApiResponse(code = 404, message = "Product with id <id> does not exist")
    })
    @RequestMapping(value = "/products/{productId}/data/{dataId}",
            produces = {"application/octet-stream"},
            method = RequestMethod.GET)
    ResponseEntity<Resource> getProductData(@ApiParam(value = "ID of the product", required = true) @PathVariable("productId") String productId,
                                            @ApiParam(value = "ID of the data to be deleted", required = true) @PathVariable("dataId") String dataId) throws IOException;


    @ApiOperation(value = "Fetch data object of the given product", nickname = "fetchProductData", notes = "Check the progress in the status field", tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data download scheduled", response = FetchData.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Product or data object do not exist"),
            @ApiResponse(code = 500, message = "Work submission failed"),
            @ApiResponse(code = 409, message = "Data is already stored locally"),})
    @RequestMapping(value = "/products/{productId}/data/{dataId}",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.PUT)
    void fetchProductData(FetchData fetchData) throws IOException;


    @ApiOperation(value = "Adds a new product", nickname = "addProduct", notes = "", tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Added a new product"),
            @ApiResponse(code = 409, message = "Product with the given id already exists"),
            @ApiResponse(code = 400, message = "Malformed json"),
            @ApiResponse(code = 400, message = "Uploaded file is not a zip"),
            @ApiResponse(code = 400, message = "Define product schema before inserting any product of this type"),
            @ApiResponse(code = 500, message = "Error when saving product data to disk"),
            @ApiResponse(code = 400, message = "Wrong metadata schema"),
            @ApiResponse(code = 400, message = "Product type is not present in metadata")
    })
    @RequestMapping(value = "/products",
            consumes = {"multipart/form-data"},
            method = RequestMethod.POST)
    void addProduct(MultipartFile file, JSONObject metadata);

    @ApiOperation(value = "Add custom metadata to product", nickname = "updateProduct", notes = "", tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Product updated successfully"),
            @ApiResponse(code = 404, message = "Product does not exist"),
            @ApiResponse(code = 400, message = "Custom metadata is not defined in the product schema"),
            @ApiResponse(code = 404, message = "Extension does not exist"),
            @ApiResponse(code = 400, message = "Product schema is not defined"),
            @ApiResponse(code = 400, message = "The given value has the wrong type"),
            @ApiResponse(code = 400, message = "Request parameter and body do not match")
    })
    @RequestMapping(value = "/products/{productId}",
            consumes = {"application/json"},
            method = RequestMethod.PUT)
    void updateProduct(String productId, CustomMetadata customMetadata);


    @ApiOperation(value = "Delete product data", nickname = "deleteProductData", notes = "", tags = {"products-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 404, message = "Product or data not found")})
    @RequestMapping(value = "/products/{productId}/data/{dataId}",
            method = RequestMethod.DELETE)
    void deleteProductData(@ApiParam(value = "ID of the product", required = true) @PathVariable("productId") String productId,
                           @ApiParam(value = "ID of the data to be deleted", required = true) @PathVariable("dataId") String dataId);

}
