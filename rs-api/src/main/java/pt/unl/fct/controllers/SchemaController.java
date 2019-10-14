package pt.unl.fct.controllers;

import io.swagger.annotations.ApiParam;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.api.SchemaApi;
import pt.unl.fct.errors.BadRequestException;
import pt.unl.fct.model.CustomMetadataSchema;
import pt.unl.fct.model.Schema;
import pt.unl.fct.model.SchemaJson;
import pt.unl.fct.services.SchemaService;
import pt.unl.fct.utils.Utils;

import javax.validation.Valid;
import java.util.List;


@RestController
public class SchemaController implements SchemaApi {

    @Autowired
    private SchemaService schemaService;

    @Override
    public List<Object> getIndexes() {
        return schemaService.getIndexes();
    }


    @Override
    public List<Object> getSchemas() {
        return schemaService.getSchemas();
    }

    @Override
    public Object getSchema(@ApiParam(value = "Product Type", required = true) @PathVariable("productType") String productType) {
        return schemaService.getSchema(productType);
    }


    @Override
    public void addSchema(@Valid @RequestBody Schema schema) {
        JSONObject schemaJsonObj = new JSONObject(Utils.g.toJson(schema.getSchema()));
        SchemaJson schemaJson = new SchemaJson(schema.getProductType(), schemaJsonObj);

        schemaService.validateMetaSchema(schemaJson.getSchema());
        schemaService.addSchema(schemaJson);
    }


    @Override
    public void updateSchema(@Valid @RequestBody Schema schema,
                             @ApiParam(value = "Product Type", required = true) @PathVariable("productType") String productType) {
        if (!schema.getProductType().equals(productType))
            throw new BadRequestException("Request parameter and body do not match");

        JSONObject schemaJsonObj = new JSONObject(Utils.g.toJson(schema.getSchema()));
        SchemaJson schemaJson = new SchemaJson(schema.getProductType(), schemaJsonObj);

        schemaService.validateMetaSchema(schemaJson.getSchema());
        schemaService.updateSchema(schemaJson);
    }

    @Override
    public void updateCustomSchema(@Valid @RequestBody CustomMetadataSchema customMetadataSchema,
                                   @ApiParam(value = "Product Type", required = true) @PathVariable("productType") String productType) {
        if (!customMetadataSchema.getProductType().equals(productType))
            throw new BadRequestException("Request parameter and body do not match");

        schemaService.updateCustomSchema(customMetadataSchema);
    }

}
