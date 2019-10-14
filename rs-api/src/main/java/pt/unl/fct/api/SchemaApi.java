package pt.unl.fct.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pt.unl.fct.model.CustomMetadataSchema;
import pt.unl.fct.model.Schema;

import java.util.List;

public interface SchemaApi {

    @ApiOperation(value = "Get the metamodel of each one of the product's type", nickname = "getSchemas", notes = "Returns an array of json schemas describing the metamodel", response = List.class, tags = {"schema-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Object.class),})
    @RequestMapping(value = "/schema",
            produces = {"application/json"},
            method = RequestMethod.GET)
    List<Object> getSchemas();

    @ApiOperation(value = "Get the metamodel of the given product type", nickname = "getSchema", notes = "Returns the schema of the given product type", response = Object.class, tags = {"schema-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Object.class),
            @ApiResponse(code = 404, message = "Schema of the given product type does not exist"),
    })
    @RequestMapping(value = "/schema/{productType}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    Object getSchema(String productType);

    @ApiOperation(value = "Add schema of new product", nickname = "addSchema", notes = "", tags = {"schema-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Product schema added"),
            @ApiResponse(code = 400, message = "Json schema is malformed"),
            @ApiResponse(code = 409, message = "Schema of the given product type already exists"),})
    @RequestMapping(value = "/schema",
            consumes = {"application/json"},
            method = RequestMethod.POST)
    void addSchema(Schema schema);

    @ApiOperation(value = "Update product schema", nickname = "updateSchema", notes = "", tags = {"schema-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Product schema updated"),
            @ApiResponse(code = 404, message = "Schema of the given product type does not exist"),
            @ApiResponse(code = 400, message = "Json schema is malformed"),
            @ApiResponse(code = 400, message = "Request parameter and body do not match"),
    })
    @RequestMapping(value = "/schema/{productType}",
            consumes = {"application/json"},
            method = RequestMethod.PUT)
    void updateSchema(Schema schema, String productType);

    @ApiOperation(value = "Add custom product schema", nickname = "updateCustomSchema", notes = "", tags = {"schema-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Product schema updated"),
            @ApiResponse(code = 404, message = "Schema of the given product type does not exist"),
            @ApiResponse(code = 400, message = "Json schema is malformed"),})
    @RequestMapping(value = "/schema/{productType}/custom",
            consumes = {"application/json"},
            method = RequestMethod.POST)
    void updateCustomSchema(CustomMetadataSchema customMetadataSchema, String productType);

    @ApiOperation(value = "Get existing indexes", nickname = "getSchema", notes = "To perform efficient queries", response = List.class, tags = {"schema-controller",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = List.class),})
    @RequestMapping(value = "/indexes",
            produces = {"application/json"},
            method = RequestMethod.GET)
    List<Object> getIndexes();

}
