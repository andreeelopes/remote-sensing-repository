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
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//import pt.unl.fct.ecma.models.*;
//import pt.unl.fct.ecma.repositories.*;
//
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotEquals;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static pt.unl.fct.ecma.utils.Utils.authenticateUser;
//import static pt.unl.fct.ecma.utils.Utils.toList;
//
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class CommentControllerTest {
//
//    @Autowired
//    private WebApplicationContext wac;
//    @Autowired
//    private CompanyRepository companyRepository;
//    @Autowired
//    private EmployeeRepository employeeRepository;
//    @Autowired
//    private ProductRepository proposalRepository;
//    @Autowired
//    private BidRepository bidRepository;
//    @Autowired
//    private ProposalRoleRepository proposalRoleRepository;
//    @Autowired
//    private CommentRepository commentRepository;
//
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//
//    @Before
//    public void init() {
//
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
//                .alwaysDo(MockMvcResultHandlers.print())
//                .build();
//
//        // Clean test database
//        companyRepository.deleteAll();
//        employeeRepository.deleteAll();
//        proposalRepository.deleteAll();
//        proposalRoleRepository.deleteAll();
//        commentRepository.deleteAll();
//
//
//        //Insert test data
//        Company company1 = new Company();
//        company1.setName("Company1");
//        company1.setEmail("company1@mail.com");
//        company1.setAddress("Lisboa");
//
//        Company company2 = new Company();
//        company2.setName("Company2");
//        company2.setEmail("company2@mail.com");
//        company2.setAddress("Porto");
//
//        company1 = companyRepository.save(company1);
//        company2 = companyRepository.save(company2);
//
//        Employee employee1 = new Employee("test1", "test1",
//                "test1@gmail.com", "Tile Painter", true,
//                "password");
//
//        Employee employee2 = new Employee("test2", "test2",
//                "test2@gmail.com", "Nails Painter", false,
//                "password");
//
//        employee1.setCompany(company1);
//        employee2.setCompany(company2);
//
//        employee1 = employeeRepository.save(employee1);
//        employeeRepository.save(employee2);
//
//        //first proposal
//        Proposal proposal1 = new Proposal();
//        proposal1.setCompanyProposed(company1);
//        proposal1.setApprover(employee1);
//        proposal1.setStatus(Proposal.Status.APPROVED.toString());
//        proposal1.setPartnerCompany(company2);
//
//
//        proposal1 = proposalRepository.save(proposal1);
//
//        //employee 1 staff role
//        ProposalRoleKey compoundKey1 = new ProposalRoleKey();
//        compoundKey1.setEmployee(employee1);
//        compoundKey1.setProposal(proposal1);
//
//        ProposalRole employee1role1 = new ProposalRole();
//        employee1role1.setPk(compoundKey1);
//        employee1role1.setRole(ProposalRole.Role.STAFF.toString());
//
//        proposalRoleRepository.save(employee1role1);
//
//        Comment comment = new Comment(employee1, "testcomment", proposal1);
//
//        commentRepository.save(comment);
//    }
//
//    @Test
//    public void testAddComment() throws Exception {
//
//        long proposalId = 1;
//        Comment comment = new Comment(employeeRepository.findByUsername("test1"),
//                "Bruce Spring",
//                proposalRepository.findById(proposalId).get());
//
//        assertEquals(commentRepository.count(), 1);
//
//        authenticateUser("test1", "password");
//        requestAddComment(proposalId, comment);
//
//        assertEquals(commentRepository.count(), 2);
//
//    }
//
//    @Test
//    public void testDeleteComment() throws Exception {
//
//        long proposalId = 1;
//        long commentId = 1;
//
//        assertEquals(commentRepository.count(), 1);
//
//        authenticateUser("test1", "password");
//        requestDeleteComment(proposalId, commentId);
//
//        assertEquals(commentRepository.count(), 0);
//    }
//
//    @Test
//    public void testUpdateComment() throws Exception {
//
//        long proposalId = 1;
//        long commentId = 1;
//
//        assertEquals(commentRepository.count(), 1);
//
//        Comment comment = commentRepository.findById(commentId).get();
//        String beforeText = comment.getComment();
//
//        String newText = "while(testing){ do not break please}";
//        comment.setComment(newText);
//
//        authenticateUser("test1", "password");
//        requestUpdateComment(proposalId, commentId, comment);
//
//        assertEquals(commentRepository.count(), 1);
//        assertNotEquals(beforeText, newText);
//
//    }
//
//    @Test
//    public void testGetProposalComments() throws Exception {
//
//        long proposalId = 1;
//
//        authenticateUser("test1", "password");
//        List<Comment> proposalComments = requestProposalComments(proposalId);
//        assertEquals(proposalComments.size(), 1);
//    }
//
//
//    //Helper Methods
//
//
//    private void requestAddComment(Long proposalId, Comment comment) throws Exception {
//
//        this.mockMvc.perform(post("/proposals/" + proposalId + "/comments/")
//                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//                .content(objectMapper.writeValueAsString(comment)))
//                .andExpect(status().isOk());
//    }
//
//    private void requestDeleteComment(long proposalId, long commentId) throws Exception {
//
//        this.mockMvc.perform(delete("/proposals/" + proposalId + "/comments/" + commentId)
//                .contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(status().isOk());
//    }
//
//    private void requestUpdateComment(long proposalId, long commentId, Comment comment) throws Exception {
//
//        this.mockMvc.perform(put("/proposals/" + proposalId + "/comments/" + commentId)
//                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//                .content(objectMapper.writeValueAsString(comment)))
//                .andExpect(status().isOk());
//    }
//
//    private List<Comment> requestProposalComments(long proposalId) throws Exception {
//
//        final MvcResult result = this.mockMvc.perform(
//                get("/proposals/" + proposalId + "/comments/"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andReturn();
//
//        String proposalsJson = result.getResponse().getContentAsString();
//
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Comment.class);
//
//        return toList(objectMapper, proposalsJson, type);
//    }
//}
