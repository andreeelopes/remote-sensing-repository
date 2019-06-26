//package pt.unl.fct.ecma.brokers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.models.Proposal;
//import pt.unl.fct.ecma.models.Review;
//import pt.unl.fct.ecma.services.ProposalService;
//import pt.unl.fct.ecma.services.ReviewService;
//
//@Service
//public class ReviewBroker {
//
//    @Autowired
//    private ReviewService reviewService;
//
//    @Autowired
//    private ProposalService proposalService;
//
//
//    public void updateReview(Review review) {
//        reviewService.updateReview(review);
//    }
//
//    public Page<Review> getProposalReviews(Long proposalId, Pageable pageable) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//        return reviewService.getProposalReviews(proposal, pageable);
//    }
//
//    public void deleteReview(Long proposalId, Long reviewId) {
//        proposalService.getProposal(proposalId);
//        Review review = reviewService.findById(reviewId);
//        reviewService.deleteReview(review);
//    }
//
//    public void addReview(Review review) {
//        Proposal proposal = proposalService.getProposal(review.getProposal().getId());
//        if (!reviewService.existsReviewOfEmployeeOnProposal(proposal, review.getAuthor()))
//            reviewService.addReview(review);
//        else
//            throw new BadRequestException("Already exists a review of that employee on that proposal");
//    }
//}
