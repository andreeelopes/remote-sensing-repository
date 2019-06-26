//package pt.unl.fct.ecma.security;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.repositories.EmployeeRepository;
//
//import java.util.Arrays;
//
//import static java.util.Collections.emptyList;
//
//
//@Service
//public class MyUserDetailsService implements UserDetailsService {
//
//    // See
//    // https://www.baeldung.com/spring-security-authentication-with-a-database
//
//    private final EmployeeRepository employee;
//
//    public MyUserDetailsService(EmployeeRepository employee) {
//        this.employee = employee;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        // Some in-memory user authentication with priority 1
//        if ( username.equals("admin") )
//            return User.withUsername(username)
//                    .password(encoder().encode("password"))
//                    .roles("ADMIN").build();
//        else {
//            // Now for the database user searching
//            Employee person = employee.findByUsername(username);
//            if (person == null) throw new UsernameNotFoundException(username);
//            return new User(username, person.getPassword(),emptyList());
//        }
//    }
//
//    @Bean
//    public PasswordEncoder encoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
