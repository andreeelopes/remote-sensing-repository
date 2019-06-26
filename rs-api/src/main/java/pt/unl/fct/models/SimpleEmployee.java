//package pt.unl.fct.ecma.models;
//
//import lombok.Getter;
//import lombok.Setter;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//
//@Getter
//@Setter
//public class SimpleEmployee {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotNull
//    @Column(unique = true)
//    private String username;
//
//    @NotNull
//    private String name;
//
//    @NotNull
//    private String email;
//
//    @NotNull
//    private String job;
//
//    private boolean isAdmin = false;
//
//}
