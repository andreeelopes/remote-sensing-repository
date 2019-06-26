//package pt.unl.fct.ecma.repositories;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.query.Param;
//import pt.unl.fct.ecma.models.Review;
//
//import java.util.List;
//
//
//public interface ReviewRepository extends CrudRepository<Review,Long> {
//
//    Page<Review> findAllByProposal_Id(Long id, Pageable pageable);
//
//    @Query("SELECT r FROM Review r where r.proposal.id = :proposalid and r.author.id = :reviewerId")
//    List<Review> existsReviewOfEmployeeOnProposal(@Param(value = "proposalid") Long proposalId,@Param(value = "reviewerId") Long reviewerId);
//}
