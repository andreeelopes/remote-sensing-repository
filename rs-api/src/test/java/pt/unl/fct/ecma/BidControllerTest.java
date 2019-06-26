//package pt.unl.fct.ecma;
//
//import com.fasterxml.jackson.databind.JavaType;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//import pt.unl.fct.ecma.models.*;
//import pt.unl.fct.ecma.repositories.*;
//
//import javax.transaction.Transactional;
//import java.util.Iterator;
//import java.util.List;
//
//import static org.hamcrest.Matchers.hasSize;
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
//public class BidControllerTest {
//
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
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Before
//    @Transactional
//    public void init() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
//                .alwaysDo(MockMvcResultHandlers.print())
//                .build();
//
//        // Data Fixture
//        companyRepository.deleteAll();
//        employeeRepository.deleteAll();
//        bidRepository.deleteAll();
//        proposalRepository.deleteAll();
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
//        proposalRepository.save(prop);
//
//
//        Bid bid = new Bid();
//        BidKey bidKey= new BidKey();
//        bidKey.setProposal(prop);
//        bidKey.setBidder(emp);
//        bid.setPk(bidKey);
//        bid.setStatus(Bid.Status.WAITING.toString());
//
//
//        bidRepository.save(bid);
//
//        proposalRepository.save(prop);
//
//    }
//
//    @Test
//    public void testUpdateBid() throws Exception{
//
//        authenticateUser("simon","simon");
//
//        Proposal proposal= getProposal();
//
//        Employee employee = getEmployee();
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        Bid bid = new Bid();
//        BidKey bidKey = new BidKey();
//        bidKey.setProposal(proposal);
//        bidKey.setBidder(employee);
//        bid.setPk(bidKey);
//        bid.setStatus(Bid.Status.ACCEPTED.toString());
//
//        String json = mapper.writeValueAsString(bid);
//        this.mockMvc.perform(put("/proposals/"+proposal.getId()+"/bids/"+employee.getId())
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isOk());
//
//        long random= proposal.getId()+1L;
//        this.mockMvc.perform(put("/proposals/"+proposal.getId()+"/bids/"+random)
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isBadRequest());
//    }
//
//
//
//    @Test
//    public void testAddBid() throws Exception{
//        authenticateUser("simon","simon");
//
//        Proposal proposal=getProposal();
//
//        Employee employee = getEmployee();
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        Bid bid = new Bid();
//        BidKey bidKey= new BidKey();
//        bidKey.setProposal(proposal);
//        bidKey.setBidder(employee);
//        bid.setPk(bidKey);
//        bid.setStatus(Bid.Status.WAITING.toString());
//
//        String json = mapper.writeValueAsString(bid);
//        try {
//            this.mockMvc.perform(post("/proposals/" + proposal.getId() + "/bids/")
//                    .contentType(MediaType.APPLICATION_JSON_UTF8)
//                    .content(json))
//                    .andExpect(status().isForbidden());
//            Assert.fail();
//        }
//        catch (Exception e){
//
//        }
//
//        Employee emp2 = new Employee();
//        emp2.setEmail("andre@gmail.com");
//        emp2.setJob("Canalizador");
//        emp2.setName("Andre");
//        emp2.setUsername("andre");
//        emp2.setPassword("andre");
//        emp2.setAdmin(false);
//        emp2.setCompany(getCompany());
//
//        bid.getPk().setBidder(emp2);
//
//        ProposalRole role = new ProposalRole();
//        ProposalRoleKey proposalRoleKey = new ProposalRoleKey();
//        proposalRoleKey.setEmployee(emp2);
//        proposalRoleKey.setProposal(proposal);
//        role.setPk(proposalRoleKey);
//        role.setRole(ProposalRole.Role.PARTNER.toString());
//
//        emp2.getRolesOnProposal().add(role);
//        employeeRepository.save(emp2);
//
//        authenticateUser("andre","andre");
//
//        json = mapper.writeValueAsString(bid);
//        this.mockMvc.perform(post("/proposals/" + proposal.getId() + "/bids/")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isOk());
//
//    }
//
//    private Proposal getProposal() {
//        Iterator<Proposal> itProposal = proposalRepository.findAll().iterator();
//        return itProposal.next();
//    }
//
//    private Company getCompany() {
//        Iterator<Company> itCompany = companyRepository.findAll().iterator();
//        return itCompany.next();
//    }
//    private Employee getEmployee() {
//        Iterator<Employee> itEmployee = employeeRepository.findAll().iterator();
//        return itEmployee.next();
//    }
//    @Test
//    public void testGetBids() throws Exception{
//        authenticateUser("simon","simon");
//
//        Proposal proposal= getProposal();
//        final MvcResult result = this.mockMvc.perform(get("/proposals/"+proposal.getId()+"/bids"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(jsonPath("$.content", hasSize(1)))
//                .andReturn();
//
//        String list = result.getResponse().getContentAsString();
//
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Bid.class);
//        List<Bid> bids=toList(objectMapper,list,type);
//        assertTrue(bids.stream().anyMatch( (p) -> p.getPk().getBidder().getName().equals("Simon")));
//    }
//    @Test
//    public void testDeleteBid() throws Exception{
//        authenticateUser("simon","simon");
//
//        Proposal proposal = getProposal();
//        Employee employee = getEmployee();
//        Long random= proposal.getId()+1L;
//        this.mockMvc.perform(delete("/proposals/"+random+"/bids/"+employee.getId()))
//                .andExpect(status().isNotFound());
//        this.mockMvc.perform(delete("/proposals/"+proposal.getId()+"/bids/"+employee.getId()))
//                .andExpect(status().isOk());
//        this.mockMvc.perform(delete("/proposals/"+proposal.getId()+"/bids/"+employee.getId()))
//                .andExpect(status().isBadRequest());
//    }
//}