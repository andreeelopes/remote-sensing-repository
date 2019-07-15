package pt.unl.fct.model;

import org.json.JSONObject;
import org.springframework.data.annotation.Id;

public class SchemaJson {

    @Id
    private String productType;

    public SchemaJson(String productType, JSONObject schema) {
        this.productType = productType;
        this.schema = schema;
    }

    private JSONObject schema;

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public JSONObject getSchema() {
        return schema;
    }

    public void setSchema(JSONObject schema) {
        this.schema = schema;
    }
}
