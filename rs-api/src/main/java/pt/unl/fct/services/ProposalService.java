//package pt.unl.fct.ecma.services;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.errors.NotFoundException;
//import pt.unl.fct.ecma.models.*;
//import pt.unl.fct.ecma.repositories.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class ProposalService {
//
//    @Autowired
//    private ProposalRepository proposalRepository;
//
//
//    public void addPartner(Proposal proposal, Employee member) {
//        ProposalRole proposalRole = new ProposalRole();
//
//        ProposalRoleKey proposalRoleKey = new ProposalRoleKey();
//        proposalRoleKey.setEmployee(member);
//        proposalRoleKey.setProposal(proposal);
//
//        proposalRole.setPk(proposalRoleKey);
//        proposalRole.setRole(ProposalRole.Role.PARTNER.toString());
//
//        proposal.getTeam().add(proposalRole);
//
//        proposalRepository.save(proposal);
//    }
//
//    public Long addProposal(Proposal proposal) {
//        Proposal dbProposal=proposalRepository.save(proposal);
//        return dbProposal.getId();
//    }
//
//    public void addStaffMember(Proposal proposal, Employee staffMember) {
//        ProposalRole proposalRole = new ProposalRole();
//        ProposalRoleKey proposalRoleKey = new ProposalRoleKey();
//        proposalRoleKey.setEmployee(staffMember);
//        proposalRoleKey.setProposal(proposal);
//
//        proposalRole.setPk(proposalRoleKey);
//        proposalRole.setRole(ProposalRole.Role.STAFF.toString());
//
//        proposal.getTeam().add(proposalRole);
//
//        proposalRepository.save(proposal);
//    }
//
//    public Proposal getProposal(Long proposalId) {
//        Optional<Proposal> proposal = proposalRepository.findById(proposalId);
//        if (proposal.isPresent()) {
//            return proposal.get();
//        } else throw new NotFoundException(String.format("Proposal with id %d does not exist", proposalId));
//    }
//
//    public void deletePartner(Proposal proposal, Employee partnerMember) {
//        proposalRepository.deletePartner(proposal.getId(), partnerMember.getId());
//    }
//
//    public void deleteStaff(Proposal proposal, Employee staffMember) {
//        proposalRepository.deleteStaff(proposal.getId(), staffMember.getId());
//    }
//
//    public void deleteProposal(Proposal proposal) {
//        proposalRepository.delete(proposal);
//    }
//
//    public Page<Employee> getProposalMembers(Proposal proposal, Pageable pageable) {
//        return proposalRepository.getProposalMembers(proposal.getId(), pageable);
//    }
//
//    public Page<Employee> getProposalStaff(Proposal proposal, Pageable pageable) {
//        return proposalRepository.getProposalStaff(proposal.getId(), pageable);
//    }
//
//    public void updateProposal(Proposal proposal, Proposal oldProposal) {
//        oldProposal.setStatus(proposal.getStatus());
//        proposalRepository.save(oldProposal);
//    }
//
//    public boolean belongsToProposalPartners(Proposal proposal, Employee partnerMember) {
//
//        List<ProposalRole> r = proposalRepository.partnerExists(proposal.getId(), partnerMember.getId());
//        if (r.size() > 0)
//            return true;
//        else
//            throw new NotFoundException(String.format("Proposal with id %d does not have partner member with id %d",
//                    proposal.getId(), partnerMember.getId()));
//    }
//
//    public boolean belongsToProposalStaff(Proposal proposal, Employee staffMember) {
//
//        List<ProposalRole> r = proposalRepository.staffExists(proposal.getId(), staffMember.getId());
//        if (r.size() > 0)
//            return true;
//        else
//            throw new NotFoundException(String.format("Proposal with id %d does not have staff member with id %d",
//                    proposal.getId(), staffMember.getId()));
//    }
//
//
//}
