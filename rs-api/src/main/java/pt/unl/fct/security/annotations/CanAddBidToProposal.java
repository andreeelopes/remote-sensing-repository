//package pt.unl.fct.ecma.security.annotations;
//
//import org.springframework.security.access.prepost.PreAuthorize;
//
//import java.lang.annotation.*;
//
//@Target({ElementType.METHOD})
//@Retention(RetentionPolicy.RUNTIME)
//@Inherited
//@Documented
//@PreAuthorize(CanAddBidToProposal.Condition)
//public @interface CanAddBidToProposal {
//    String Condition = "@mySecurityService.belongsToTeamProposal(principal,#proposalId) and" +
//            " @mySecurityService.bidHasPrincipal(principal,#bid) and" +
//            " not @mySecurityService.isApproverOfProposal(principal,#proposalId)";
//}
