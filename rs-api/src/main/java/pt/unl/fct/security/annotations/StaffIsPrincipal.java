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
//@PreAuthorize(pt.unl.fct.ecma.security.annotations.StaffIsPrincipal.Condition)
//public @interface StaffIsPrincipal {
//    String Condition = "@mySecurityService.isPrincipal(principal, #staffId)";
//}