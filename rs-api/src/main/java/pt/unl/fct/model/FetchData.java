package pt.unl.fct.model;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@Getter
@Setter
public class FetchData {

    private String productId;

    private String dataObjectId;
}
