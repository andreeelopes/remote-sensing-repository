//package pt.unl.fct.ecma.services;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.errors.NotFoundException;
//import pt.unl.fct.ecma.models.Comment;
//import pt.unl.fct.ecma.models.Proposal;
//import pt.unl.fct.ecma.repositories.CommentRepository;
//
//import java.util.Optional;
//
//@Service
//public class CommentService {
//
//    @Autowired
//    private CommentRepository commentRepository;
//
//
//    public void addComment(Comment comment) {
//        commentRepository.save(comment);
//    }
//
//    public void deleteComment(Comment comment) {
//        commentRepository.delete(comment);
//    }
//
//    public Page<Comment> getProposalComments(Pageable pageable, Proposal proposal) {
//        return commentRepository.findAllByProposal_Id(proposal.getId(), pageable);
//    }
//
//    public void updateComment(Comment comment, Comment oldComment) {
//        oldComment.setComment(comment.getComment());
//        commentRepository.save(oldComment);
//    }
//
//
//    public Comment getComment(Long commentId) {
//        Optional<Comment> comment = commentRepository.findById(commentId);
//        if (comment.isPresent()) {
//            return comment.get();
//        } else throw new NotFoundException(String.format("Comment with id %d does not exist", commentId));
//    }
//
//    public boolean commentBelongsToProposal(Comment comment, Proposal proposal) {
//        if (comment.getProposal().getId().equals(proposal.getId())) {
//            return true;
//        } else
//            throw new NotFoundException(String.format("Comment with id %d does not have a proposal with id %d",
//                    comment.getId(), proposal.getId()));
//
//    }
//
//}
