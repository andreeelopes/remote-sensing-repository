//package pt.unl.fct.ecma.models;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import lombok.Data;
//import lombok.Getter;
//import lombok.Setter;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import java.util.LinkedList;
//import java.util.List;
//@Getter
//@Setter
//@Entity
//public class Company {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotNull
//    private String name;
//
//    @NotNull
//    private String address;
//
//    @NotNull
//    private String email;
//
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "company")
//    @JsonIgnore
//    private List<Employee> employees = new LinkedList<>();
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "companyProposed")
//    @JsonIgnore
//    private List<Proposal> proposals;
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "partnerCompany")
//    @JsonIgnore
//    private List<Proposal> proposalsTargeted;
//
//}
