//package pt.unl.fct.ecma.services;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.models.*;
//import pt.unl.fct.ecma.errors.NotFoundException;
//import pt.unl.fct.ecma.repositories.EmployeeRepository;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class EmployeeService {
//
//    @Autowired
//    private EmployeeRepository employeeRepository;
//
//
//    public Employee getEmployee(Long employeeId) {
//        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
//        if (employeeOpt.isPresent())
//            return employeeOpt.get();
//        else
//            throw new NotFoundException("Employee not found");
//    }
//
//    public Page<Employee> getAllEmployees(Pageable pageable) {
//        return employeeRepository.findAll(pageable);
//    }
//
//    public Page<Employee> getEmployeeByName(String name, Pageable pageable) {
//        return employeeRepository.findByName(name, pageable);
//    }
//
//    public Employee getEmployeeByUsername(String username) {
//        return employeeRepository.findByUsername(username);
//    }
//
//    public void updateEmployee(SimpleEmployee employee, Employee oldEmployee) {
//        oldEmployee.setAdmin(employee.isAdmin());
//        oldEmployee.setEmail(employee.getEmail());
//        oldEmployee.setName(employee.getName());
//        oldEmployee.setJob(employee.getJob());
//        employeeRepository.save(oldEmployee);
//    }
//
//    public Page<Bid> getAllBids(Employee bidder, Pageable pageable) {
//        return employeeRepository.findAllBids(pageable, bidder.getId());
//    }
//
//    public Page<Bid> getAllBidsByStatus(Employee bidder, String status, Pageable pageable) {
//        return employeeRepository.findBidsByStatus(status, bidder.getId(), pageable);
//    }
//
//    public Page<Proposal> getProposalPartner(Pageable pageable, Employee partnerMember) {
//        return employeeRepository.findProposalPartner(pageable, partnerMember.getId());
//    }
//
//    public Page<Proposal> getProposalStaff(Pageable pageable, Employee staffMember) {
//        return employeeRepository.findProposalStaff(pageable, staffMember.getId());
//    }
//
//    public Page<Employee> existEmployee(String employeeName) {
//        List<Employee> list= new ArrayList<>();
//        Employee emp=employeeRepository.findByUsername(employeeName);
//        if(emp!=null)
//        list.add(emp);
//        Page<Employee> page= new PageImpl<Employee>(list);
//
//        return page;
//    }
//
//    public Page<Review> getReviews(Employee employee,Pageable pageable) {
//        return employeeRepository.getReviews(pageable,employee.getId());
//    }
//
//    public Page<Proposal> getProposalApprover(Employee employee, Pageable pageable) {
//        return employeeRepository.getProposalsApprover(pageable,employee.getId());
//    }
//}
