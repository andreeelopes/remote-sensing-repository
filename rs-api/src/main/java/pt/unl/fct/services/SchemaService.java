package pt.unl.fct.services;


import org.bson.Document;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import pt.unl.fct.errors.BadRequestException;
import pt.unl.fct.errors.ConflictException;
import pt.unl.fct.errors.NotFoundException;
import pt.unl.fct.model.CustomMetadataSchema;
import pt.unl.fct.model.SchemaJson;
import pt.unl.fct.utils.Utils;

import java.util.List;

@Service
public class SchemaService {

    private static final String SCHEMA_COL = "schema";
    private static final String INDEXES_COL = "indexes";

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Object> getIndexes() {
        Query query = new Query();
        query.fields().exclude("_id");
        return mongoTemplate.find(query, Object.class, INDEXES_COL);
    }

    public List<Object> getSchemas() {
        return mongoTemplate.findAll(Object.class, SCHEMA_COL);
    }

    public Object getSchema(String productType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(productType));
        Object schemaObj = mongoTemplate.findOne(query, Object.class, SCHEMA_COL);

        if (schemaObj == null)
            throw new BadRequestException("Schema of the given product type does not exist");
        else
            return schemaObj;
    }

    public void addSchema(SchemaJson schema) {

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(schema.getProductType()));

        if (mongoTemplate.exists(query, SCHEMA_COL))
            throw new ConflictException("Schema of the given product type already exists");

        Document doc = new Document()
                .append("_id", schema.getProductType())
                .append("schema", Document.parse(schema.getSchema().toString()));

        mongoTemplate.insert(doc, SCHEMA_COL);
    }

    public void updateSchema(SchemaJson schema) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(schema.getProductType()));
        if (!mongoTemplate.exists(query, SCHEMA_COL))
            throw new NotFoundException("Schema of the given product type does not exist");

        Document doc = Document.parse(schema.getSchema().toString());

        Update update = new Update().set("schema", doc);
        mongoTemplate.upsert(query, update, SCHEMA_COL);
    }

    public void validateSchema(JSONObject json) {

        try {
            String productType = json.getString("productType");

            Object schemaObj = getSchema(productType);
            if (schemaObj == null)
                throw new BadRequestException("Define product schema before inserting any product of this type");

            JSONObject schema = new JSONObject(Utils.g.toJson(schemaObj)).getJSONObject("schema");

            SchemaLoader loader = SchemaLoader.builder()
                    .schemaJson(schema)
                    .build();
            Schema schemaLoaded = loader.load().build();
            schemaLoaded.validate(json);

        } catch (ValidationException e) {
            throw new BadRequestException("Wrong metadata schema - " + e.getMessage());
        } catch (JSONException e) {
            throw new BadRequestException("Product type is not present in the given metadata");
        }
    }


    public void validateMetaSchema(JSONObject schema) {

        try {
            SchemaLoader loader = SchemaLoader.builder()
                    .schemaJson(schema)
                    .build();
            loader.load().build();
        } catch (SchemaException e) {
            throw new BadRequestException("Json schema is malformed");
        }
    }


    public void updateCustomSchema(CustomMetadataSchema customMetadataSchema) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(customMetadataSchema.getProductType()));
        Object schema = mongoTemplate.findOne(query, Object.class, SCHEMA_COL);

        if (schema == null)
            throw new NotFoundException("Schema of the given product type does not exist");

        JSONObject schemaJson = new JSONObject(Utils.g.toJson(schema)).getJSONObject("schema");

        JSONObject customMetadataJson = new JSONObject()
                .put("id", "#/properties/custom/properties/" + customMetadataSchema.getName())
                .put("type", customMetadataSchema.getType())
                .put("title", customMetadataSchema.getDescription())
                .put("author", customMetadataSchema.getAuthor())
                .put("extensionId", customMetadataSchema.getExtensionId())
                .put("definitionDate", customMetadataSchema.getDefinitionDate().toDate());


        schemaJson.getJSONObject("properties")
                .getJSONObject("custom")
                .getJSONObject("properties")
                .put(customMetadataSchema.getName(), customMetadataJson);

        Document doc = Document.parse(schemaJson.toString());

        Update update = new Update().set("schema", doc);
        mongoTemplate.upsert(query, update, SCHEMA_COL);

    }
}
