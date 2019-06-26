//package pt.unl.fct.ecma.brokers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.models.*;
//import pt.unl.fct.ecma.services.EmployeeService;
//
//@Service
//public class EmployeeBroker {
//
//    @Autowired
//    private EmployeeService employeeService;
//
//
//    public Employee getEmployee(Long employeeId) {
//        return employeeService.getEmployee(employeeId);
//    }
//
//    public Page<Bid> getAllBids(Long employeeId, Pageable pageable) {
//        Employee bidder = employeeService.getEmployee(employeeId);
//        return employeeService.getAllBids(bidder, pageable);
//    }
//
//    public Page<Bid> getAllBidsByStatus(Long employeeId, String search, Pageable pageable) {
//        Employee bidder = employeeService.getEmployee(employeeId);
//        return employeeService.getAllBidsByStatus(bidder, search, pageable);
//    }
//
//    public Page<Employee> getAllEmployees(Pageable pageable) {
//        return employeeService.getAllEmployees(pageable);
//    }
//
//    public Page<Employee> getEmployeeByName(String search, Pageable pageable) {
//        return employeeService.getEmployeeByName(search, pageable);
//    }
//
//    public Page<Proposal> getProposalPartner(Pageable pageable, Long employeeId) {
//        Employee partnerMember = employeeService.getEmployee(employeeId);
//        return employeeService.getProposalPartner(pageable, partnerMember);
//    }
//
//    public Page<Proposal> getProposalStaff(Pageable pageable, Long employeeId) {
//        Employee staffMember = employeeService.getEmployee(employeeId);
//        return employeeService.getProposalStaff(pageable, staffMember);
//    }
//
//    public void updateEmployee(SimpleEmployee employee) {
//        Employee oldEmployee = employeeService.getEmployee(employee.getId());
//        employeeService.updateEmployee(employee, oldEmployee);
//    }
//
//    public Page<Employee> existEmployee(String employeeName) {
//       return employeeService.existEmployee(employeeName);
//    }
//
//    public Page<Review> getReviews(Long employeeId,Pageable pageable) {
//        Employee employee = employeeService.getEmployee(employeeId);
//        return employeeService.getReviews(employee,pageable);
//
//    }
//
//    public Page<Proposal> getAproverProposals(Long employeeId, Pageable pageable) {
//        Employee employee = employeeService.getEmployee(employeeId);
//        return employeeService.getProposalApprover(employee,pageable);
//    }
//}