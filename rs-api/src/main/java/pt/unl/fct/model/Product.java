package pt.unl.fct.model;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJson;
import org.springframework.data.mongodb.core.geo.GeoJsonGeometryCollection;

import java.util.Date;

@ApiModel
@Getter
@Setter
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

}
