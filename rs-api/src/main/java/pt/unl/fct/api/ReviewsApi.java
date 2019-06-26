//package pt.unl.fct.ecma.api;
//
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import io.swagger.annotations.ApiResponse;
//import io.swagger.annotations.ApiResponses;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import pt.unl.fct.ecma.models.Review;
//
//import javax.validation.Valid;
//
//public interface ReviewsApi {
//
//
//    @ApiOperation(value = "Add a new review to the proposal", nickname = "addReview", notes = "", tags = {"reviews",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Added a new review"),
//            @ApiResponse(code = 405, message = "Invalid input")})
//    @RequestMapping(value = "/proposals/{proposalId}/reviews/",
//            produces = {"application/json"},
//            consumes = {"application/json"},
//            method = RequestMethod.POST)
//    void addReview(@ApiParam(value = "Proposal ID", required = true) @PathVariable("proposalId") Long proposalId,
//                   @ApiParam(value = "Review object to add to the proposal", required = true) @Valid @RequestBody Review review);
//
//
//    @ApiOperation(value = "Delete review with the ID provided", nickname = "deleteReview", notes = "", tags = {"reviews",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation"),
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Proposal not found")})
//    @RequestMapping(value = "/proposals/{proposalId}/reviews/{reviewId}",
//            method = RequestMethod.DELETE)
//    void deleteReview(@ApiParam(value = "Proposal ID", required = true) @PathVariable("proposalId") Long proposalId,
//                      @ApiParam(value = "review ID", required = true) @PathVariable("reviewId") Long reviewId);
//
//
//    @ApiOperation(value = "Get all reviews by the proposal ID", nickname = "getProposalReviews", notes = "Returns all reviews",
//            response = Review.class, responseContainer = "List", tags = {"reviews",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation", response = Review.class, responseContainer = "List"),
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Proposal not found")})
//    @RequestMapping(value = "/proposals/{proposalId}/reviews",
//            produces = {"application/json"},
//            method = RequestMethod.GET)
//    Page<Review> getProposalReviews(Pageable pageable,
//                                    @ApiParam(value = "Proposal ID", required = true) @PathVariable("proposalId") Long proposalId);
//
//
//    @ApiOperation(value = "Update reviews with ID provided", nickname = "reviewid", notes = "", tags = {"reviews",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Proposal or review not found"),
//            @ApiResponse(code = 405, message = "Validation exception")})
//    @RequestMapping(value = "/proposals/{proposalId}/reviews/{reviewId}",
//            consumes = {"application/json"},
//            method = RequestMethod.PUT)
//    void updateReview(@ApiParam(value = "Review object that needs to be updated in the collection", required = true)
//                      @Valid @RequestBody Review review, @ApiParam(value = "Review ID", required = true) @PathVariable("reviewId") Long reviewId,
//                      @ApiParam(value = "ID of proposal", required = true) @PathVariable("proposalId") Long proposalId);
//
//
//}
