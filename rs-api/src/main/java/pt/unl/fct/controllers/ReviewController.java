//package pt.unl.fct.ecma.controllers;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//import pt.unl.fct.ecma.api.ReviewsApi;
//import pt.unl.fct.ecma.brokers.ReviewBroker;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.models.Review;
//import pt.unl.fct.ecma.security.annotations.BelongsToProposalTeamOrIsApprover;
//import pt.unl.fct.ecma.security.annotations.CanAddReview;
//import pt.unl.fct.ecma.security.annotations.CanModifyReview;
//
//import javax.validation.Valid;
//
//@RestController
//public class ReviewController implements ReviewsApi {
//
//    @Autowired
//    private ReviewBroker reviewBroker;
//
//    @CanModifyReview
//    @Override
//    public void updateReview(  @RequestBody Review review,
//                              @PathVariable("reviewId") Long reviewId,
//                             @PathVariable("proposalId") Long proposalId) {
//
//        if (!review.getProposal().getId().equals(proposalId))
//            throw new BadRequestException("Ids of proposal do not match");
//        if (!review.getId().equals(reviewId))
//            throw new BadRequestException("Ids of review do not match");
//
//        reviewBroker.updateReview(review);
//    }
//
//    @BelongsToProposalTeamOrIsApprover
//    @Override
//    public Page<Review> getProposalReviews(Pageable pageable,
//                                           @PathVariable("proposalId") Long proposalId) {
//        return reviewBroker.getProposalReviews(proposalId, pageable);
//    }
//
//    @CanModifyReview
//    @Override
//    public void deleteReview(@PathVariable("proposalId") Long proposalId,
//                             @PathVariable("reviewId") Long reviewId) {
//        reviewBroker.deleteReview(proposalId, reviewId);
//    }
//
//    @CanAddReview
//    @Override
//    public void addReview(@PathVariable("proposalId") Long proposalId,
//                          @Valid @RequestBody Review review) {
//
//        if (review.getId() != null)
//            throw new BadRequestException("Cannot submit id of review");
//        if (!review.getProposal().getId().equals(proposalId))
//            throw new BadRequestException("Ids of proposal do not match");
//
//        reviewBroker.addReview(review);
//    }
//
//}
