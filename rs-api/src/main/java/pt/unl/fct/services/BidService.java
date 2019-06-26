//package pt.unl.fct.ecma.services;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.models.Bid;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.models.Proposal;
//import pt.unl.fct.ecma.repositories.BidKey;
//import pt.unl.fct.ecma.repositories.ProposalRepository;
//
//import javax.transaction.Transactional;
//import java.util.List;
//import java.util.Random;
//
//@Service
//public class BidService {
//
//    private static final float PERCENTAGE_OF_BIDS_PICKED = 0.8f;
//
//    @Autowired
//    private ProposalRepository proposalRepository;
//
//
//    @Transactional
//    public void addBidToProposal(Proposal proposal, Employee bidder) {
//        Bid newBid = new Bid();
//        BidKey key = new BidKey();
//        key.setBidder(bidder);
//        key.setProposal(proposal);
//        newBid.setPk(key);
//        newBid.setStatus("WAITING");
//
//        proposal.getBids().add(newBid);
//
//        proposalRepository.save(proposal);
//    }
//
//    public void deleteBid(Proposal proposal, Employee employee) {
//        proposalRepository.deleteBidById(proposal.getId(), employee.getId());
//    }
//
//    public Page<Bid> getBids(Pageable pageable, Proposal proposal) {
//        return proposalRepository.findAllBids(pageable, proposal.getId());
//    }
//
//    public void updateBid(Bid bid) {
//        proposalRepository.changeBidStatus(bid.getStatus(), bid.getPk().getBidder().getId(), bid.getPk().getProposal().getId());
//    }
//
//    public boolean bidBelongsToProposal(Long proposal, Long bidder) {
//        List<Bid> bids = proposalRepository.existsBid(proposal, bidder);
//        if (bids.size() > 0) {
//            return true;
//        } else return false;
//    }
//
//    public void pickBids(Long proposalId) {
//        List<Bid> bids = proposalRepository.getAllBids(proposalId);
//        int bidsSize = bids.size();
//
//        if (bidsSize > 0) {
//            Random rand = new Random();
//
//            int numberOfPicks = (int) (bidsSize * PERCENTAGE_OF_BIDS_PICKED);
//
//            if (numberOfPicks == 0)
//                numberOfPicks = 1;
//
//
//            for (int i = 0; i < numberOfPicks; i++) {
//                int randomIndex = rand.nextInt(bids.size());
//                Bid randomBid = bids.get(randomIndex);
//                randomBid.setStatus(Bid.Status.ACCEPTED.toString());
//                updateBid(randomBid);
//                bids.remove(randomBid);
//            }
//
//            for (Bid bid : bids) { //Set the remaining bids to DENIED
//                bid.setStatus(Bid.Status.DENIED.toString());
//                updateBid(bid);
//            }
//
//        }
//    }
//
//}
