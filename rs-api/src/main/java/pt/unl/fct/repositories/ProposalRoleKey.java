//package pt.unl.fct.ecma.repositories;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import lombok.Data;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.models.Proposal;
//
//import javax.persistence.Embeddable;
//import javax.persistence.Id;
//import javax.persistence.ManyToOne;
//import javax.validation.constraints.NotNull;
//import java.io.Serializable;
//@Data
//@Embeddable
//public class ProposalRoleKey implements Serializable {
//    @NotNull
//    @ManyToOne
//    private Proposal proposal;
//
//    @NotNull
//    @ManyToOne
//    private Employee employee;
//}
