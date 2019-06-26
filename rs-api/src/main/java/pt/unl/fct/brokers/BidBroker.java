//package pt.unl.fct.ecma.brokers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.models.Bid;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.models.Proposal;
//import pt.unl.fct.ecma.services.BidService;
//import pt.unl.fct.ecma.services.EmployeeService;
//import pt.unl.fct.ecma.services.ProposalService;
//
//@Service
//public class BidBroker {
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
//
//    public void updateBid(Bid bid) {
//        proposalService.getProposal(bid.getPk().getProposal().getId());
//
//        if (bidService.bidBelongsToProposal(bid.getPk().getProposal().getId(), bid.getPk().getBidder().getId()))
//            bidService.updateBid(bid);
//        else throw new BadRequestException("This bid does not exist");
//    }
//
//    public void addBidToProposal(Bid bid) {
//        Proposal proposal = proposalService.getProposal(bid.getPk().getProposal().getId());
//        Employee bidder = employeeService.getEmployee(bid.getPk().getBidder().getId());
//
//        if(!bidService.bidBelongsToProposal(bid.getPk().getProposal().getId(), bid.getPk().getBidder().getId()))
//            bidService.addBidToProposal(proposal, bidder);
//    }
//
//    public void deleteBid(Long proposalId, Long employeeId) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//        Employee bidder = employeeService.getEmployee(employeeId);
//
//        if (bidService.bidBelongsToProposal(proposalId, employeeId))
//            bidService.deleteBid(proposal, bidder);
//        else throw new BadRequestException("This bid does not exist");
//    }
//
//    public Page<Bid> getBids(Pageable pageable, Long proposalId) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//        return bidService.getBids(pageable, proposal);
//    }
//
//}