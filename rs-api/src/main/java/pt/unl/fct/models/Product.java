package pt.unl.fct.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
public class Product  {

    @Id
    private String id;
}
