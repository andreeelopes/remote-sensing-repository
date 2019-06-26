//package pt.unl.fct.ecma.brokers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.models.Comment;
//import pt.unl.fct.ecma.models.Proposal;
//import pt.unl.fct.ecma.services.CommentService;
//import pt.unl.fct.ecma.services.EmployeeService;
//import pt.unl.fct.ecma.services.ProposalService;
//
//@Service
//public class CommentBroker {
//
//    @Autowired
//    private CommentService commentService;
//
//    @Autowired
//    private ProposalService proposalService;
//
//    @Autowired
//    private EmployeeService employeeService;
//
//
//    public void addComment(Comment comment) {
//        proposalService.getProposal(comment.getProposal().getId());
//        employeeService.getEmployee(comment.getAuthor().getId());
//
//        commentService.addComment(comment);
//    }
//
//    public void deleteComment(Long proposalId, Long commentId) {
//        Comment comment = commentService.getComment(commentId);
//        Proposal proposal = proposalService.getProposal(proposalId);
//
//        if(commentService.commentBelongsToProposal(comment, proposal))
//            commentService.deleteComment(comment);
//    }
//
//    public Page<Comment> getProposalComments(Pageable pageable, Long proposalId) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//
//        return commentService.getProposalComments(pageable, proposal);
//    }
//
//    public void updateComment(Comment comment) {
//        proposalService.getProposal(comment.getProposal().getId());
//
//        Comment oldComment = commentService.getComment(comment.getId());
//
//        commentService.updateComment(comment, oldComment);
//    }
//
//}
