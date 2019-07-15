package pt.unl.fct.model;

import io.swagger.annotations.ApiModel;

@ApiModel
public class FetchData {

    private String productId;

    private String dataObjectId;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDataObjectId() {
        return dataObjectId;
    }

    public void setDataObjectId(String dataObjectId) {
        this.dataObjectId = dataObjectId;
    }
}
