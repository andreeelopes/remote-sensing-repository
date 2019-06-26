//package pt.unl.fct.ecma.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//import pt.unl.fct.ecma.api.CommentsApi;
//import pt.unl.fct.ecma.brokers.CommentBroker;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.models.Comment;
//import pt.unl.fct.ecma.security.annotations.BelongsToProposalTeamOrIsApprover;
//import pt.unl.fct.ecma.security.annotations.CanAddComment;
//import pt.unl.fct.ecma.security.annotations.IsAuthorOfComment;
//
//import javax.validation.Valid;
//
//@RestController
//public class CommentController implements CommentsApi {
//
//    @Autowired
//    private CommentBroker commentBroker;
//
//    @CanAddComment
//    @Override
//    public void addComment(@PathVariable("proposalId") Long proposalId,
//                           @Valid @RequestBody Comment comment) {
//
//        if (!comment.getProposal().getId().equals(proposalId))
//            throw new BadRequestException("Ids of proposal do not match");
//
//        if (comment.getId() != null)
//            throw new BadRequestException("Can not define id of new comment");
//
//        commentBroker.addComment(comment);
//    }
//
//    @IsAuthorOfComment
//    @Override
//    public void deleteComment(@PathVariable("proposalId") Long proposalId,
//                              @PathVariable("commentId") Long commentId) {
//        commentBroker.deleteComment(proposalId, commentId);
//    }
//
//    @BelongsToProposalTeamOrIsApprover
//    @Override
//    public Page<Comment> getProposalComments(Pageable pageable,
//                                             @PathVariable("proposalId") Long proposalId) {
//        return commentBroker.getProposalComments(pageable, proposalId);
//    }
//
//    @IsAuthorOfComment
//    @Override
//    public void updateComment(@Valid @RequestBody Comment comment,
//                              @PathVariable("commentId") Long commentId,
//                              @PathVariable("proposalId") Long proposalId) {
//
//        if (!comment.getProposal().getId().equals(proposalId))
//            throw new BadRequestException("Ids of proposal do not match");
//
//        if (!comment.getId().equals(commentId))
//            throw new BadRequestException("Ids of comment do not match");
//
//        commentBroker.updateComment(comment);
//    }
//}
