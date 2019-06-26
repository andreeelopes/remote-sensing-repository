//package pt.unl.fct.ecma.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import pt.unl.fct.ecma.api.EmployeesApi;
//import pt.unl.fct.ecma.brokers.EmployeeBroker;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.errors.FoundException;
//import pt.unl.fct.ecma.errors.NotFoundException;
//import pt.unl.fct.ecma.models.*;
//import pt.unl.fct.ecma.security.annotations.IsPrincipal;
//
//import javax.validation.Valid;
//
//@RestController
//
//public class EmployeeController implements EmployeesApi {
//
//    @Autowired
//    private EmployeeBroker employeeBroker;
//
//
//    @Override
//    public Employee getEmployee(@PathVariable("employeeId") Long employeeId) {
//
//        return employeeBroker.getEmployee(employeeId);
//    }
//
//    @Override
//    @IsPrincipal
//    public Page<Bid> getEmployeeBids(@PathVariable("employeeId") Long employeeId,
//                                     @Valid @RequestParam(value = "search", required = false) String search,
//                                     Pageable pageable) {
//        if (search == null) {
//            return employeeBroker.getAllBids(employeeId, pageable);
//        } else {
//            return employeeBroker.getAllBidsByStatus(employeeId, search, pageable);
//        }
//    }
//
//
//    @Override
//    public Page<Employee> getEmployees(@Valid @RequestParam(value = "search", required = false) String search,
//                                       Pageable pageable,@RequestParam(value = "exist", required = false) String exist) {
//        if (search == null && exist==null) {
//            return employeeBroker.getAllEmployees(pageable);
//        }
//        if(exist!=null){
//            return employeeBroker.existEmployee(exist);
//        }
//        else {
//            return employeeBroker.getEmployeeByName(search, pageable);
//        }
//    }
//
//    @Override
//    @IsPrincipal
//    public Page<Proposal> getProposalPartner(@PathVariable("employeeId") Long employeeId,
//                                             @Valid @RequestParam(value = "search", required = false) String search,
//                                             Pageable pageable) { //TODO search?
//        return employeeBroker.getProposalPartner(pageable, employeeId);
//    }
//
//    @Override
//    @IsPrincipal
//    public Page<Proposal> getProposalStaff(@PathVariable("employeeId") Long employeeId,
//                                           @Valid @RequestParam(value = "search", required = false) String search,
//                                           Pageable pageable) { //TODO search?
//        return employeeBroker.getProposalStaff(pageable, employeeId);
//    }
//
//    @Override
//    @IsPrincipal
//    public void updateEmployee(@Valid @RequestBody SimpleEmployee employee,
//                               @PathVariable("employeeId") Long employeeId) {
//
//        if (!employee.getId().equals(employeeId))
//            throw new BadRequestException("Ids of employee do not match");
//
//        employeeBroker.updateEmployee(employee);
//    }
//
//    @Override
//    public Page<Review> getReviews(@PathVariable("employeeId")Long employeeId,Pageable pageable) {
//        return employeeBroker.getReviews(employeeId,pageable);
//    }
//
//    @Override
//    public Page<Proposal> getProposalsApprover(@PathVariable("employeeId")Long employeeId, Pageable pageable) {
//        return employeeBroker.getAproverProposals(employeeId,pageable);
//    }
//
//
//}
