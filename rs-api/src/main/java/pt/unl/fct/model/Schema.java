package pt.unl.fct.model;


import com.google.gson.JsonObject;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;

@ApiModel
@Data
public class Schema {

    @Id
    private String productType;

    private Object schema;

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Object getSchema() {
        return schema;
    }

    public void setSchema(Object schema) {
        this.schema = schema;
    }
}
