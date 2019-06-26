//package pt.unl.fct.ecma.models;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import lombok.Data;
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import java.util.LinkedList;
//import java.util.List;
//
//@Getter
//@Setter
//@Entity
//public class Employee {
//
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
//    @JsonIgnore
//    @NotNull
//    private String password;
//
//    @ManyToOne
//    @NotNull
//    private Company company;
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "pk.employee")
//    @JsonIgnore
//    private List<ProposalRole> rolesOnProposal = new LinkedList<>();
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "pk.bidder")
//    @JsonIgnore
//    private List<Bid> biddedProposals = new LinkedList<>();
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "author")
//    @JsonIgnore
//    private List<Comment> comments = new LinkedList<>();
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "author")
//    @JsonIgnore
//    private List<Review> reviews = new LinkedList<>();
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "approver")
//    @JsonIgnore
//    private List<Proposal> approver = new LinkedList<>();
//
//
//    public Employee(){
//    }
//
//    public Employee(@NotNull String username, @NotNull String name, @NotNull String email, @NotNull String job, boolean isAdmin, @NotNull String password) {
//        this.username = username;
//        this.name = name;
//        this.email = email;
//        this.job = job;
//        this.isAdmin = isAdmin;
//        setPassword(password);
//    }
//
//    public void setPassword(String password){
//        this.password = encodeText(password);
//    }
//
//    private String encodeText(String text){
//        return new BCryptPasswordEncoder().encode(text);
//    }
//}
