package pt.unl.fct.services;

import org.bson.BsonArray;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.Document;
import org.joda.time.DateTime;
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
import springfox.documentation.spring.web.json.Json;

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


    public void addProduct(JSONObject mdDoc) {
        String productId = mdDoc.getString("_id");

        JSONObject dataObj = mdDoc.getJSONObject("data");
        JSONArray imageryObj = dataObj.getJSONArray("imagery");
        JSONArray metadataObj = dataObj.getJSONArray("metadata");


        dataObj.put("imagery", updateEntryUrl(imageryObj, productId));
        dataObj.put("metadata", updateEntryUrl(metadataObj, productId));
        mdDoc.put("data", dataObj);

        //change date format
        DateTime dateStart = DateTime.parse(mdDoc.getString("acquisitionDateStart"));
        DateTime dateEnd = DateTime.parse(mdDoc.getString("acquisitionDateEnd"));

        BsonDocument doc = BsonDocument.parse(mdDoc.toString());

        doc.remove("acquisitionDateStart");
        doc.remove("acquisitionDateEnd");

        doc.put("acquisitionDateStart", new BsonDateTime(dateStart.getMillis()));
        doc.put("acquisitionDateEnd", new BsonDateTime(dateEnd.getMillis()));

        mongoTemplate.insert(doc.toJson(), PRODUCTS_MD_COL);
    }

    private JSONArray updateEntryUrl(JSONArray arr, String productId) {
        int initialSize = arr.length();

        for (int i = 0; i < initialSize; i++) {
            JSONObject entry = arr.getJSONObject(i);
            String url = entry.getString("url");
            entry.put("url", Utils.BASE_DIR + "/" + productId + "/" + url);

            arr.put(entry);
        }
        for (int i = 0; i < initialSize; i++) // remove old entries
            arr.remove(i);

        return arr;
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


        JSONObject custom = null;
        String type = null;
        try {
            JSONObject customField = new JSONObject(Utils.g.toJson(schemaObj))
                    .getJSONObject("schema")
                    .getJSONObject("properties")
                    .getJSONObject("custom")
                    .getJSONObject("properties")
                    .getJSONObject(customMetadata.getFieldName());

            type = customField.getString("type");

            custom = product.getJSONObject("custom");
        } catch (Exception e) {
            e.printStackTrace();
            throw new NotFoundException("Extension does not exist");
        }


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

        JSONObject product = new JSONObject(Utils.g.toJson(getProduct(productId)));

        JSONArray imagery = product.getJSONObject("data").getJSONArray("imagery");
        JSONArray metadata = product.getJSONObject("data").getJSONArray("metadata");

        String imageryUrl = getProductDataUrl(imagery, dataId);
        String metadataUrl = getProductDataUrl(metadata, dataId);

        if (imageryUrl == null && metadataUrl == null) {
            throw new NotFoundException("Product data with id " + dataId + " does not exist");
        } else if (imageryUrl == null) {
            return metadataUrl;
        } else {
            return imageryUrl;
        }
    }


    private String getProductDataUrl(JSONArray arr, String dataId) {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject elem = arr.getJSONObject(i);

            if (elem.getString("_id").equals(dataId)) {
                String status = elem.getString("status");
                if (status.equals("local")) {
                    return elem.getString("url");
                } else
                    throw new NotFoundException("Product data is not in the infraestructure");
            }
        }
        return null;
    }


}
