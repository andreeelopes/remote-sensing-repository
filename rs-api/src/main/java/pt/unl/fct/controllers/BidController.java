//package pt.unl.fct.ecma.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//import pt.unl.fct.ecma.api.BidsApi;
//import pt.unl.fct.ecma.brokers.BidBroker;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.models.Bid;
//import pt.unl.fct.ecma.security.annotations.CanAddBidToProposal;
//import pt.unl.fct.ecma.security.annotations.IsApproverOfProposal;
//import pt.unl.fct.ecma.security.annotations.IsBidder;
//import pt.unl.fct.ecma.services.BidService;
//
//import javax.validation.Valid;
//
//@RestController
//public class BidController implements BidsApi {
//
//    @Autowired
//    private BidBroker bidBroker;
//
//    @IsApproverOfProposal
//    @Override
//    public void updateBid(@Valid @RequestBody Bid bid,
//                          @PathVariable("employeeId") Long employeeId,
//                          @PathVariable("proposalId") Long proposalId) {
//
//        if (!bid.getPk().getProposal().getId().equals(proposalId))
//            throw new BadRequestException("Ids of proposal do not match");
//
//        if (!bid.getPk().getBidder().getId().equals(employeeId))
//            throw new BadRequestException("Ids of employee do not match");
//
//        bidBroker.updateBid(bid);
//    }
//
//    @CanAddBidToProposal
//    @Override
//    public void addBidToProposal(@PathVariable("proposalId") Long proposalId,
//                                 @Valid @RequestBody Bid bid) {
//
//        if (!bid.getPk().getProposal().getId().equals(proposalId))
//            throw new BadRequestException("Ids of proposal do not match");
//
//        bidBroker.addBidToProposal(bid);
//    }
//
//    @IsBidder
//    @Override
//    public void deleteBid(@PathVariable("proposalId") Long proposalId,
//                          @PathVariable("employeeId") Long employeeId) {
//        bidBroker.deleteBid(proposalId, employeeId);
//    }
//
//    @IsApproverOfProposal
//    @Override
//    public Page<Bid> getBids(Pageable pageable,
//                             @PathVariable("proposalId")Long proposalId) {
//        return bidBroker.getBids(pageable, proposalId);
//    }
//
//
//}
