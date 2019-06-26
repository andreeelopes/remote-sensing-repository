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
//
//@Entity
//@Getter
//@Setter
//public class Proposal {
//
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotNull
//    @ManyToOne
//    private Employee approver;
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "pk.proposal")
//    @JsonIgnore
//    private List<ProposalRole> team = new LinkedList<>();
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "proposal")
//    @JsonIgnore
//    private List<Comment> comments = new LinkedList<>();
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "proposal")
//    @JsonIgnore
//    private List<Review> reviews = new LinkedList<>();
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "proposal")
//    @JsonIgnore
//    private List<Section> sections = new LinkedList<>();
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "pk.proposal")
//    @JsonIgnore
//    private List<Bid> bids = new LinkedList<>();
//
//    @NotNull
//    @ManyToOne
//    private Company companyProposed;
//
//    @NotNull
//    @ManyToOne
//    private Company partnerCompany;
//
//    private String status = Status.PLACED.toString();
//
//    public enum Status {
//        PLACED, REVIEW_PERIOD, APPROVED, DECLINED
//    }
//}
