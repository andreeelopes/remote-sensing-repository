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
//public class Comment {
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
//    private String comment;
//
//    @NotNull
//    @ManyToOne
//    private Proposal proposal;
//
//    public Comment(){}
//
//    public Comment(@NotNull Employee author, @NotNull String comment, @NotNull Proposal proposal) {
//        this.author = author;
//        this.comment = comment;
//        this.proposal = proposal;
//    }
//}
