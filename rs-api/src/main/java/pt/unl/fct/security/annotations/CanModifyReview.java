//package pt.unl.fct.ecma.security.annotations;
//
//
//import org.springframework.security.access.prepost.PreAuthorize;
//
//import java.lang.annotation.*;
//
//@Target({ElementType.METHOD})
//@Retention(RetentionPolicy.RUNTIME)
//@Inherited
//@Documented
//@PreAuthorize(CanModifyReview.Condition)
//public @interface CanModifyReview {
//    String Condition = "@mySecurityService.canModifyReview(principal, #reviewId) and " +
//            "@mySecurityService.belongsToTeamProposal(principal, #proposalId)";
//}