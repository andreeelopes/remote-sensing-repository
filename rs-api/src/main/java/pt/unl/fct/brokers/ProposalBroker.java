//package pt.unl.fct.ecma.brokers;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.models.Proposal;
//import pt.unl.fct.ecma.services.BidService;
//import pt.unl.fct.ecma.services.CompanyService;
//import pt.unl.fct.ecma.services.EmployeeService;
//import pt.unl.fct.ecma.services.ProposalService;
//
//@Service
//public class ProposalBroker {
//
//    @Autowired
//    private ProposalService proposalService;
//
//    @Autowired
//    private EmployeeService employeeService;
//
//    @Autowired
//    private BidService bidService;
//
//    @Autowired
//    private CompanyService companyService;
//
//    public Employee addPartner(Long proposalId, Employee member) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//        Employee dbEmployee = employeeService.getEmployeeByUsername(member.getUsername());
//
//        if (companyService.employeeBelongsToCompany(dbEmployee, proposal.getPartnerCompany())) {
//            proposalService.addPartner(proposal, dbEmployee);
//            return dbEmployee;
//        } else
//            throw new BadRequestException("Employee does not belong to the partner company of this proposal");
//    }
//
//    public Long addProposal(Proposal proposal, String principalName) {
//
//        Employee dbApprover = employeeService.getEmployee(proposal.getApprover().getId());
//
//        if (companyService.employeeBelongsToCompany(dbApprover, proposal.getCompanyProposed())) {
//            Long id=proposalService.addProposal(proposal);
//            Employee author = employeeService.getEmployeeByUsername(principalName);
//            addStaffMember(proposal.getId(), author);
//            return id;
//        } else {
//            throw new BadRequestException("Approver must belong to the company which made the proposal");
//        }
//
//    }
//
//    public Employee addStaffMember(Long proposalId, Employee staffMember) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//        Employee dbEmployee =  employeeService.getEmployeeByUsername(staffMember.getUsername());
//
//        if (companyService.employeeBelongsToCompany(dbEmployee, proposal.getCompanyProposed())) {
//            proposalService.addStaffMember(proposal, dbEmployee);
//            return dbEmployee;
//        } else
//            throw new BadRequestException("Employee does not belong to the company that made this proposal");
//    }
//
//
//    public void updateProposal(Long proposalId, Proposal proposal) {
//        Proposal oldProposal = proposalService.getProposal(proposalId);
//        String newStatus = proposal.getStatus();
//        if (newStatus.equals(Proposal.Status.REVIEW_PERIOD.toString())) {
//            bidService.pickBids(proposalId);
//        }
//        proposalService.updateProposal(proposal, oldProposal);
//    }
//
//    public void deletePartner(Long proposalId, Long partnerId) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//        Employee partnerMember = employeeService.getEmployee(partnerId);
//
//        if (proposalService.belongsToProposalPartners(proposal, partnerMember))
//            proposalService.deletePartner(proposal, partnerMember);
//    }
//
//    public void deleteProposal(Long proposalId) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//        proposalService.deleteProposal(proposal);
//    }
//
//    public void deleteStaff(Long proposalId, Long staffId) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//        Employee staffMember = employeeService.getEmployee(staffId);
//
//        if (proposalService.belongsToProposalStaff(proposal, staffMember))
//            proposalService.deleteStaff(proposal, staffMember);
//    }
//
//    public Proposal getProposal(Long proposalId) {
//        return proposalService.getProposal(proposalId);
//    }
//
//    public Page<Employee> getProposalMembers(Long proposalId, Pageable pageable) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//        return proposalService.getProposalMembers(proposal, pageable);
//    }
//
//    public Page<Employee> getProposalStaff(Long proposalId, Pageable pageable) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//        return proposalService.getProposalStaff(proposal, pageable);
//    }
//
//
//}
