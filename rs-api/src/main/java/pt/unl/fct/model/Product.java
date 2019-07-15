package pt.unl.fct.model;

import io.swagger.annotations.ApiModel;
import org.springframework.data.annotation.Id;

import java.util.Date;

@ApiModel

public class Product {

    @Id
    private String id;

    private String title;

    private String program;

    private String platform;

    private String productType;

    private Date acquisitionDateStart;
    private Date acquisitionDateEnd;

    private String iconFile;

    private String metadataFile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Date getAcquisitionDateStart() {
        return acquisitionDateStart;
    }

    public void setAcquisitionDateStart(Date acquisitionDateStart) {
        this.acquisitionDateStart = acquisitionDateStart;
    }

    public Date getAcquisitionDateEnd() {
        return acquisitionDateEnd;
    }

    public void setAcquisitionDateEnd(Date acquisitionDateEnd) {
        this.acquisitionDateEnd = acquisitionDateEnd;
    }

    public String getIconFile() {
        return iconFile;
    }

    public void setIconFile(String iconFile) {
        this.iconFile = iconFile;
    }

    public String getMetadataFile() {
        return metadataFile;
    }

    public void setMetadataFile(String metadataFile) {
        this.metadataFile = metadataFile;
    }

}
