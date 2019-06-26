//package pt.unl.fct.ecma.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//import pt.unl.fct.ecma.api.ProposalsApi;
//import pt.unl.fct.ecma.brokers.ProposalBroker;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.security.annotations.*;
//import pt.unl.fct.ecma.models.*;
//
//import javax.validation.Valid;
//
//@RestController
//public class ProposalController implements ProposalsApi {
//
//    @Autowired
//    private ProposalBroker proposalBroker;
//
//
//    @BelongsToProposalStaff
//    @Override
//    public Employee addPartner(@PathVariable("proposalId") Long proposalId,
//                           @RequestBody Employee member) {
//        return proposalBroker.addPartner(proposalId, member);
//    }
//
//    @Override
//    public Long addProposal(@Valid @RequestBody Proposal proposal) {
//
//        if (proposal.getId() != null)
//            throw new BadRequestException("Can not define id of new proposal");
//
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        return proposalBroker.addProposal(proposal, user.getUsername());
//    }
//
//    @BelongsToProposalStaff
//    @Override
//    public Employee addStaffMember(@PathVariable("proposalId") Long proposalId,
//                               @RequestBody Employee staffMember) {
//        return proposalBroker.addStaffMember(proposalId, staffMember);
//    }
//
//
//    @IsApproverOfProposal
//    @Override
//    public void updateProposal(@PathVariable("proposalId") Long proposalId,
//                               @RequestBody Proposal proposal) {
//        proposalBroker.updateProposal(proposalId, proposal);
//    }
//
//
//    @BelongsToProposalStaff
//    @Override
//    public void deletePartner(@PathVariable("proposalId") Long proposalId,
//                              @PathVariable("partnerId") Long partnerId) {
//        proposalBroker.deletePartner(proposalId, partnerId);
//    }
//
//    @BelongsToProposalStaff
//    @Override
//    public void deleteProposal(@PathVariable("proposalId") Long proposalId) {
//        proposalBroker.deleteProposal(proposalId);
//    }
//
//    @StaffIsPrincipal
//    @Override
//    public void deleteStaff(@PathVariable("proposalId") Long proposalId,
//                            @PathVariable("staffId") Long staffId) {
//        proposalBroker.deleteStaff(proposalId, staffId);
//    }
//
//    @BelongsToProposalTeamOrIsApprover
//    @Override
//    public Proposal getProposal(@PathVariable("proposalId") Long proposalId) {
//        return proposalBroker.getProposal(proposalId);
//    }
//
//    @BelongsToProposalTeamOrIsApprover
//    @Override
//    public Page<Employee> getProposalMembers(Pageable pageable,
//                                             @PathVariable("proposalId") Long proposalId) {
//        return proposalBroker.getProposalMembers(proposalId, pageable);
//    }
//
//    @BelongsToProposalTeamOrIsApprover
//    @Override
//    public Page<Employee> getStaffMembers(Pageable pageable,
//                                          @PathVariable("proposalId") Long proposalId) {
//        return proposalBroker.getProposalStaff(proposalId, pageable);
//    }
//
//}
