//package pt.unl.fct.ecma.models;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import lombok.Data;
//import pt.unl.fct.ecma.repositories.ProposalRoleKey;
//
//import javax.persistence.EmbeddedId;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.ManyToOne;
//import java.io.Serializable;
//
//@Entity
//@Data
//public class ProposalRole  implements Serializable {
//
//    @EmbeddedId
//    private ProposalRoleKey pk;
//
//    private String role;
//
//    public enum Role {
//        STAFF, PARTNER
//    }
//}
