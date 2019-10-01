package pt.unl.fct.services;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import pt.unl.fct.errors.BadRequestException;
import pt.unl.fct.errors.ConflictException;
import pt.unl.fct.errors.NotFoundException;
import pt.unl.fct.model.CustomMetadata;
import pt.unl.fct.model.FetchData;
import pt.unl.fct.model.Product;
import pt.unl.fct.model.SchemaJson;
import pt.unl.fct.utils.Utils;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {

    private static final String PRODUCTS_MD_COL = "productsMD";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SchemaService schemaService;

    public Object getProduct(String productId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(productId));

        Object product = mongoTemplate.findOne(query, Object.class, PRODUCTS_MD_COL);

        if (product != null) {
            return product;
        } else throw new NotFoundException("Product does not exist");
    }

    public Page<Product> getProducts(String search, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Query query = new BasicQuery(search).with(pageable);

        List<Product> products = mongoTemplate.find(query, Product.class, PRODUCTS_MD_COL);

        return PageableExecutionUtils.getPage(
                products,
                pageable,
                () -> mongoTemplate.count(query, Product.class));
    }

    public void fetchProductData(FetchData fetchData) {
        Utils.fetchHttpPost(fetchData);
    }


    public void addProduct(JSONObject metadata) {
        Document doc = Document.parse(metadata.toString());
        mongoTemplate.insert(doc, PRODUCTS_MD_COL);
    }

    public void updateProduct(CustomMetadata customMetadata) {
        Object schemaObj = null;
        JSONObject product = null;
        try {
            product = new JSONObject(Utils.g.toJson(getProduct(customMetadata.getProductId())));

            schemaObj = schemaService.getSchema(product.getString("productType"));
            if (schemaObj == null)
                throw new BadRequestException("Product schema is not defined");
        } catch (JSONException e) {
            throw new BadRequestException("Custom metadata is not defined in the product schema");
        }

        JSONObject customField = new JSONObject(Utils.g.toJson(schemaObj))
                .getJSONObject("schema")
                .getJSONObject("properties")
                .getJSONObject("custom")
                .getJSONObject("properties")
                .getJSONObject(customMetadata.getFieldName());

        String type = customField.getString("type");

        JSONObject custom = product.getJSONObject("custom");

        try {
            switch (type) {
                case "string":
                    custom.put(customMetadata.getFieldName(), customMetadata.getValue().toString());
                    break;
                case "number":
                    custom.put(customMetadata.getFieldName(), Float.parseFloat(customMetadata.getValue().toString()));
                    break;
                case "integer":
                    custom.put(customMetadata.getFieldName(), Integer.parseInt(customMetadata.getValue().toString()));
                    break;
                case "array":
                    custom.put(customMetadata.getFieldName(), new JSONArray(Utils.g.toJson(customMetadata.getValue())));
                    break;
                case "boolean":
                    custom.put(customMetadata.getFieldName(), Boolean.parseBoolean(customMetadata.getValue().toString()));
                    break;
                default:
                    custom.put(customMetadata.getFieldName(), new JSONObject(Utils.g.toJson(customMetadata.getValue())));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new BadRequestException("The given value has the wrong type");
        }

        Document doc = Document.parse(custom.toString());

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(customMetadata.getProductId()));

        mongoTemplate.upsert(query, new Update().set("custom", doc), PRODUCTS_MD_COL);

    }


    public String getProductDataLocation(String productId, String dataId) {
        return "./scripts/wait-for-it.sh";
    }
}
