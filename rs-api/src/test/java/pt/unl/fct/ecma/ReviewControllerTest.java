//package pt.unl.fct.ecma;
//
//import com.fasterxml.jackson.databind.JavaType;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//import pt.unl.fct.ecma.models.*;
//import pt.unl.fct.ecma.repositories.*;
//
//import javax.transaction.Transactional;
//
//import java.util.Iterator;
//import java.util.List;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static pt.unl.fct.ecma.utils.Utils.authenticateUser;
//import static pt.unl.fct.ecma.utils.Utils.toList;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
//public class ReviewControllerTest {
//
//    @Autowired
//    private WebApplicationContext wac;
//
//    @Autowired
//    private CompanyRepository companyRepository;
//
//    @Autowired
//    private EmployeeRepository employeeRepository;
//
//    @Autowired
//    private ProposalRepository proposalRepository;
//
//    @Autowired
//    private BidRepository bidRepository;
//
//    @Autowired
//    private ReviewRepository reviewRepository;
//
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Before
//    @Transactional
//    public void init() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//
//        Company company= new Company();
//        company.setName("MountainDew");
//        company.setEmail("mountaindew@outlook");
//        company.setAddress("Lisboa");
//
//        companyRepository.save(company);
//
//
//        Employee emp = new Employee();
//        emp.setEmail("simon@gmail.com");
//        emp.setJob("Informatico");
//        emp.setName("Simon");
//        emp.setUsername("simon");
//        emp.setPassword("simon");
//        emp.setAdmin(true);
//        emp.setCompany(company);
//
//        employeeRepository.save(emp);
//
//        Proposal prop = new Proposal();
//        prop.setCompanyProposed(company);
//        prop.setApprover(emp);
//        prop.setStatus(Proposal.Status.APPROVED.toString());
//        prop.setPartnerCompany(company);
//
//        ProposalRole role = new ProposalRole();
//        ProposalRoleKey proposalRoleKey = new ProposalRoleKey();
//        proposalRoleKey.setEmployee(emp);
//        proposalRoleKey.setProposal(prop);
//
//        role.setPk(proposalRoleKey);
//        role.setRole(ProposalRole.Role.STAFF.toString());
//        prop.getTeam().add(role);
//
//        proposalRepository.save(prop);
//
//        Bid bid = new Bid();
//        BidKey bidKey= new BidKey();
//        bidKey.setProposal(prop);
//        bidKey.setBidder(emp);
//        bid.setPk(bidKey);
//        bid.setStatus(Bid.Status.ACCEPTED.toString());
//
//        bidRepository.save(bid);
//
//        Review review = new Review();
//        review.setText("WoW");
//        review.setScore(Review.Score.EXCELENT);
//        review.setProposal(prop);
//        review.setAuthor(emp);
//
//        reviewRepository.save(review);
//
//    }
//    private Proposal getProposal() {
//        Iterator<Proposal> itProposal =proposalRepository.findAll().iterator();
//        return itProposal.next();
//    }
//
//    private Review getReview() {
//        Iterator<Review> itReview =reviewRepository.findAll().iterator();
//        return itReview.next();
//    }
//    private Employee getEmployee() {
//        Iterator<Employee> itEmployee =employeeRepository.findAll().iterator();
//        return itEmployee.next();
//    }
//    @Test
//    public void testGetReview() throws Exception{
//        authenticateUser("simon","simon");
//        Proposal proposal= getProposal();
//        final MvcResult result = this.mockMvc.perform(get("/proposals/" + proposal.getId()+"/reviews"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(jsonPath("$.content", hasSize(1)))
//                .andReturn();
//
//        String json = result.getResponse().getContentAsString();
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Review.class);
//        List<Review> reviewList = toList(objectMapper,json,type);
//        assertTrue(reviewList.stream().anyMatch( (p) -> p.getAuthor().getName().equals("Simon")));
//    }
//    @Test
//    public void testAddReview() throws Exception{
//        reviewRepository.deleteAll();
//        authenticateUser("simon","simon");
//        Proposal proposal = getProposal();
//        Employee employee = getEmployee();
//        Review review = new Review();
//        review.setText("WoW");
//        review.setScore(Review.Score.EXCELENT);
//        review.setProposal(proposal);
//        review.setAuthor(employee);
//
//
//        String json = objectMapper.writeValueAsString(review);
//        this.mockMvc.perform(post("/proposals/" + proposal.getId()+"/reviews/")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isOk());
//        long size =reviewRepository.count();
//        assertEquals(size, 1L);
//    }
//    @Test
//    public void testUpdateReview() throws Exception{
//        authenticateUser("simon","simon");
//        Review review = getReview();
//        Proposal proposal = getProposal();
//        review.setScore(Review.Score.BAD);
//        review.setText("Bue podre");
//        String json = objectMapper.writeValueAsString(review);
//        this.mockMvc.perform(put("/proposals/" + proposal.getId()+"/reviews/"+review.getId())
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isOk());
//        review = getReview();
//        assertEquals(review.getText(),"Bue podre");
//        assertEquals(review.getScore(), Review.Score.BAD);
//    }
//    @Test
//    public void testDeleteReview() throws Exception{
//        authenticateUser("simon","simon");
//        Proposal proposal = getProposal();
//        Review review = getReview();
//        this.mockMvc.perform(delete("/proposals/"+proposal.getId()+"/reviews/"+review.getId()))
//                .andExpect(status().isOk());
//        long size =reviewRepository.count();
//        assertEquals(size, 0L);
//    }
//}
