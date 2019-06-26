//package pt.unl.fct.ecma.repositories;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.query.Param;
//import pt.unl.fct.ecma.models.Bid;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.models.Proposal;
//import pt.unl.fct.ecma.models.ProposalRole;
//
//import javax.transaction.Transactional;
//import java.util.List;
//
//public interface ProposalRepository extends CrudRepository<Proposal,Long> {
//
//    Page<Proposal> findAll(Pageable pageable);
//
//    Page<Proposal> findByStatus(Pageable pageable, String status);
//
//
//    @Transactional
//    @Modifying
//    @Query("delete from Bid b where b.pk.proposal.id = :id and b.pk.bidder.id = :employeeid")
//    void deleteBidById(@Param(value = "id")Long id,@Param(value = "employeeid") Long employeeid);
//
//    @Query("select b from Bid b where b.pk.proposal.id = :id and b.pk.bidder.id = :employeeid")
//    List<Bid> existsBid(@Param(value = "id")Long id,@Param(value = "employeeid") Long employeeid);
//
//    @Query("select b from Bid b where b.pk.proposal.id = :proposalId and b.pk.bidder.id = :employeeid and b.status = :status")
//    List<Bid> existsBidOnProposalWithStatus(@Param(value = "proposalId")Long proposalId,
//                                          @Param(value = "employeeid") Long employeeid,
//                                            @Param(value = "status") String status);
//
//    @Query("SELECT b FROM Bid b where b.pk.proposal.id = :proposalid ")
//    Page<Bid> findAllBids(Pageable pageable, @Param(value = "proposalid") Long id);
//
//    @Modifying
//    @Query("update Bid b set b.status = :status where b.pk.bidder.id = :employeeid and b.pk.proposal.id = :proposalid")
//    @Transactional
//    void changeBidStatus(@Param(value = "status") String status,@Param(value = "employeeid")Long employeeId, @Param(value = "proposalid") Long proposalid );
//
//
//    @Transactional
//    @Modifying
//    @Query("delete FROM ProposalRole r WHERE r.pk.employee.id = :partnerid AND r.role LIKE CONCAT('%','PARTNER','%') AND r.pk.proposal.id = :id")
//    void deletePartner(@Param(value = "id")Long id, @Param(value = "partnerid") Long partnerid);
//
//    @Query("select r FROM ProposalRole r WHERE r.pk.employee.id = :partnerid AND r.role LIKE CONCAT('%','PARTNER','%') AND r.pk.proposal.id = :id")
//    List<ProposalRole> partnerExists(@Param(value = "id")Long id, @Param(value = "partnerid") Long partnerid);
//
//    @Transactional
//    @Modifying
//    @Query("delete FROM ProposalRole r WHERE r.pk.employee.id = :staffid AND r.role LIKE CONCAT('%','STAFF','%') AND r.pk.proposal.id = :id")
//    void deleteStaff(@Param(value = "id")Long id, @Param(value = "staffid") Long partnerid);
//
//    @Query("select r FROM ProposalRole r WHERE r.pk.employee.id = :staffid AND r.role LIKE CONCAT('%','STAFF','%') AND r.pk.proposal.id = :id")
//    List<ProposalRole> staffExists(@Param(value = "id")Long id, @Param(value = "staffid") Long staffId);
//
//    @Query("select r.pk.employee FROM ProposalRole r WHERE r.role LIKE CONCAT('%','PARTNER','%') AND r.pk.proposal.id = :id")
//    Page<Employee> getProposalMembers(@Param(value = "id")Long id, Pageable pageable);
//
//    @Query("select r.pk.employee FROM ProposalRole r WHERE r.role LIKE CONCAT('%','STAFF','%') AND r.pk.proposal.id = :id")
//    Page<Employee> getProposalStaff(@Param(value = "id")Long id, Pageable pageable);
//
//
//
//    @Query("SELECT b FROM Bid b where b.pk.proposal.id = :proposalid ")
//    List<Bid> getAllBids(@Param(value = "proposalid") Long id);
//
//    @Query("select r.pk.employee FROM ProposalRole r WHERE r.role LIKE CONCAT('%','PARTNER','%') AND r.pk.proposal.id = :id")
//    List<Employee> getProposalMembersWithoutPage(@Param(value = "id")Long proposalId);
//
//    @Query("select r.pk.employee FROM ProposalRole r WHERE r.role LIKE CONCAT('%','STAFF','%') AND r.pk.proposal.id = :id")
//    List<Employee> getProposalStaffWithoutPage(@Param(value = "id")Long proposalId);
//}
