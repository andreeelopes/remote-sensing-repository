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
//@PreAuthorize(CanAddReview.Condition)
//public @interface CanAddReview {
//    String Condition = "@mySecurityService.isBidApproved(principal, #proposalId) and " +
//            "@mySecurityService.belongsToTeamProposal(principal, #proposalId)";
//}
