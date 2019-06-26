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
//@PreAuthorize(CanAddComment.Condition)
//public @interface CanAddComment {
//    String Condition =
//            "@mySecurityService.isAuthorOfComment(principal, #comment) and " +
//            "@mySecurityService.belongsToTeamProposal(principal, #proposalId)";
//}
