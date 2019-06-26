//package pt.unl.fct.ecma.models;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import lombok.Data;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//
//@Entity
//@Data
//public class Review {
//    public enum Score{BAD,POOR,OK,GREAT,EXCELENT}
//
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotNull
//    @ManyToOne
//    private Employee author;
//
//    @NotNull
//    private String text;
//
//    @NotNull
//    @ManyToOne
//    private Proposal proposal;
//
//    @NotNull
//    private Score score;
//}
