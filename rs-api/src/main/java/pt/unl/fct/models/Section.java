//package pt.unl.fct.ecma.models;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import lombok.Data;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//
//@Data
//@Entity
//public class Section {
//
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotNull
//    @ManyToOne
//    private Proposal proposal;
//
//    @NotNull
//    private String text;
//
//    @NotNull
//    private String type;
//
//    public enum Type {
//        TITLE, DESCRIPTION, GOALS, MATERIALS,
//        BUDGET, WORKPLAN
//    }
//
//}
