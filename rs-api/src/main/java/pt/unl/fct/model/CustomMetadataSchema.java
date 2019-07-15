package pt.unl.fct.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.joda.time.DateTime;

import javax.validation.constraints.Pattern;


@ApiModel
public class CustomMetadataSchema {

    private String extensionId;

    @Pattern(regexp="^(number|string|object|array|interger|boolean)$",message="Invalid json schema type")
    private String type;
    //    private Indexing indexing; TODO
    @JsonIgnore
    private DateTime definitionDate = new DateTime();
    private String name;
    private String author;
    private String productType;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    public String getExtensionId() {
        return extensionId;
    }

    public void setExtensionId(String extensionId) {
        this.extensionId = extensionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DateTime getDefinitionDate() {
        return definitionDate;
    }

    public void setDefinitionDate(DateTime definitionDate) {
        this.definitionDate = definitionDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }
}
