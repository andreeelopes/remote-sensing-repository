//package pt.unl.fct.ecma;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
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
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static pt.unl.fct.ecma.utils.Utils.authenticateUser;
//
//@RunWith(SpringRunner.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
//@SpringBootTest
//public class ProposalControllerTest {
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
//    private ProposalRoleRepository proposalRoleRepository;
//
//    @Autowired
//    private BidRepository bidRepository;
//
//    @Autowired
//    ObjectMapper objectMapper;
//
//    private MockMvc mockMvc;
//
//    @Before
//    @Transactional
//    public void init() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
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
//        Employee emp2 = new Employee();
//        emp2.setEmail("andre@gmail.com");
//        emp2.setJob("Canalizador");
//        emp2.setName("Andre");
//        emp2.setUsername("andre");
//        emp2.setPassword("andre");
//        emp2.setAdmin(false);
//        emp2.setCompany(company);
//
//        employeeRepository.save(emp2);
//
//        Employee emp3 = new Employee();
//        emp3.setEmail("nelson@sapo.pt");
//        emp3.setJob("Pintor de Azuleijos");
//        emp3.setName("Nelson");
//        emp3.setUsername("nelson");
//        emp3.setPassword("password");
//        emp3.setAdmin(false);
//        emp3.setCompany(company);
//
//        employeeRepository.save(emp3);
//
//        Employee emp4 = new Employee();
//        emp4.setEmail("maria@sapo.pt");
//        emp4.setJob("Pintora de Azuleijos");
//        emp4.setName("Maria");
//        emp4.setUsername("maria");
//        emp4.setPassword("password");
//        emp4.setAdmin(false);
//        emp4.setCompany(company);
//
//        employeeRepository.save(emp4);
//
//        Proposal prop = new Proposal();
//        prop.setCompanyProposed(company);
//        prop.setApprover(emp);
//        prop.setStatus(Proposal.Status.REVIEW_PERIOD.toString());
//        prop.setPartnerCompany(company);
//        ProposalRole role = new ProposalRole();
//        ProposalRoleKey proposalRoleKey = new ProposalRoleKey();
//        proposalRoleKey.setEmployee(emp);
//        proposalRoleKey.setProposal(prop);
//
//        role.setPk(proposalRoleKey);
//        role.setRole(ProposalRole.Role.STAFF.toString());
//        prop.getTeam().add(role);
//
//        //role for the third employee
//        ProposalRole role3 = new ProposalRole();
//        ProposalRoleKey proposalRoleKey3 = new ProposalRoleKey();
//        proposalRoleKey3.setEmployee(emp3);
//        proposalRoleKey3.setProposal(prop);
//
//        role3.setPk(proposalRoleKey3);
//        role3.setRole(ProposalRole.Role.STAFF.toString());
//        prop.getTeam().add(role3);
//        proposalRepository.save(prop);
//
//        //Bid 1 for proposal from emp2
//        Bid bid1 = new Bid();
//        BidKey bid1Key = new BidKey();
//        bid1Key.setProposal(prop);
//        bid1Key.setBidder(emp2);
//        bid1.setPk(bid1Key);
//        bid1.setStatus(Bid.Status.WAITING.toString());
//
//        bidRepository.save(bid1);
//
//        //Bid 1 for proposal from emp2
//        Bid bid2 = new Bid();
//        BidKey bid2Key = new BidKey();
//        bid2Key.setProposal(prop);
//        bid2Key.setBidder(emp3);
//        bid2.setPk(bid2Key);
//        bid2.setStatus(Bid.Status.WAITING.toString());
//
//        bidRepository.save(bid2);
//    }
//
//    @Test
//    public void testGetProposal() throws Exception{
//        Proposal dbProposal = proposalRepository.findById(1L).get();
//        Proposal testProposal = requestGetProposal(1L);
//
//        assertEquals(dbProposal.getId(), testProposal.getId());
//        assertEquals(dbProposal.getApprover().getId(), testProposal.getApprover().getId());
//    }
//    private Proposal requestGetProposal(long proposalid) throws Exception {
//        authenticateUser("simon","simon");
//        final MvcResult result = this.mockMvc.perform(get("/proposals/" + proposalid))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andReturn();
//
//        String json = result.getResponse().getContentAsString();
//
//        return objectMapper.readValue(json, Proposal.class);
//    }
//
//
//    private Company getCompany() {
//        Iterator<Company> itCompany =companyRepository.findAll().iterator();
//        return itCompany.next();
//    }
//    private Employee getEmployee() {
//        Iterator<Employee> itEmployee =employeeRepository.findAll().iterator();
//        return itEmployee.next();
//    }
//    private Proposal getProposal() {
//        Iterator<Proposal> itProposal =proposalRepository.findAll().iterator();
//        return itProposal.next();
//    }
//
//    @Test
//    public void testAddProposal() throws Exception{
//        authenticateUser("simon","simon");
//
//        Proposal prop = new Proposal();
//        prop.setCompanyProposed(getCompany());
//        prop.setApprover(getEmployee());
//        prop.setStatus(Proposal.Status.APPROVED.toString());
//        prop.setPartnerCompany(getCompany());
//
//        String json = objectMapper.writeValueAsString(prop);
//        this.mockMvc.perform(post("/proposals/")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isOk());
//        long size =proposalRepository.count();
//        assertEquals(size, 2L);
//    }
//    @Test
//    public void testAddMember() throws Exception{
//        long sizeBefore = proposalRoleRepository.count();
//        AddAndreAsMember();
//        assertEquals(proposalRoleRepository.count(), sizeBefore + 1);
//    }
//
//    private void AddAndreAsMember() throws Exception {
//        authenticateUser("simon","simon");
//
//        Employee emp = employeeRepository.findById(2L).get();
//
//        String json = objectMapper.writeValueAsString(emp);
//        this.mockMvc.perform(post("/proposals/"+getProposal().getId()+"/partnermembers/")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void testAddStaff() throws Exception{
//
//        authenticateUser("simon","simon");
//
//        long sizeBefore = proposalRoleRepository.count();
//
//        Employee emp = employeeRepository.findById(2L).get();
//
//        String json = objectMapper.writeValueAsString(emp);
//        this.mockMvc.perform(post("/proposals/" + getProposal().getId() + "/staff/")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isOk());
//        assertEquals(sizeBefore + 1, proposalRoleRepository.count());
//    }
//    @Test
//    public void testUpdateProposal() throws Exception{
//        authenticateUser("simon","simon");
//        Proposal proposal = getProposal();
//        proposal.setStatus(Proposal.Status.APPROVED.toString());
//        String json = objectMapper.writeValueAsString(proposal);
//        this.mockMvc.perform(put("/proposals/"+proposal.getId())
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isOk());
//        proposal = getProposal();
//        assertTrue(proposal.getStatus().equals(Proposal.Status.APPROVED.toString()));
//    }
//    @Test
//    public void testGetMembers() throws Exception{
//        authenticateUser("simon","simon");
//        Proposal proposal = getProposal();
//        this.mockMvc.perform(get("/proposals/"+proposal.getId()+"/partnermembers/"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(jsonPath("$.content", hasSize(0)));
//
//    }
//    @Test
//    public void testGetStaff() throws Exception{
//        authenticateUser("simon","simon");
//        Proposal proposal = getProposal();
//        MvcResult result = this.mockMvc.perform(get("/proposals/"+proposal.getId()+"/staff/"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(jsonPath("$.content", hasSize(2)))
//                .andReturn();
//        String list = result.getResponse().getContentAsString();
//
//        JsonParser jsonParser = new JsonParser();
//        JsonElement element = jsonParser.parse(list);
//        JsonObject array=element.getAsJsonObject();
//        String content=array.get("content").toString();
//        ObjectMapper mapper = new ObjectMapper();
//
//        List<Employee> staff = mapper.readValue(content,new TypeReference<List<Employee>>(){});
//        assertTrue(staff.stream().anyMatch( (p) -> p.getName().equals("Simon")));
//
//    }
//    @Test
//    public void testDeleteMember() throws Exception{
//        AddAndreAsMember();
//        Proposal proposal = getProposal();
//        Employee emp = employeeRepository.findById(2L).get();
//        int beforeDelete = proposalRepository.getProposalMembersWithoutPage(proposal.getId()).size();
//        this.mockMvc.perform(delete("/proposals/"+proposal.getId()+"/partnermembers/"+emp.getId()))
//                .andExpect(status().isOk());
//        assertEquals(proposalRepository.getProposalMembersWithoutPage(proposal.getId()).size(),beforeDelete - 1);
//    }
//    @Test
//    public void testDeleteStaff() throws Exception{
//        authenticateUser("simon","simon");
//        Proposal proposal = getProposal();
//        Employee employee = getEmployee();
//        long teamSizeBefore = proposalRoleRepository.count();
//
//
//        this.mockMvc.perform(delete("/proposals/"+proposal.getId()+"/staff/"+employee.getId()))
//                .andExpect(status().isOk());
//        assertEquals(proposalRoleRepository.count(),teamSizeBefore-1);
//    }
//    @Test
//    public void testDeleteProposal() throws Exception{
//        authenticateUser("simon","simon");
//        Proposal proposal = getProposal();
//        this.mockMvc.perform(delete("/proposals/"+proposal.getId()))
//                .andExpect(status().isOk());
//        assertEquals(proposalRepository.count(),0L);
//    }
//
//    @Test
//    public void testProposalReviewPeriod() throws Exception {
//
//        long proposalId = 1;
//
//        authenticateUser("simon","simon");
//
//        List<Bid> bidsBefore  = proposalRepository.getAllBids(proposalId);
//
//        Proposal proposal = proposalRepository.findById(proposalId).get();
//        String newStatus = Proposal.Status.REVIEW_PERIOD.toString();
//        proposal.setStatus(newStatus);
//
//        assertTrue(bidsBefore.stream().allMatch(p -> !p.getStatus().equals(Bid.Status.ACCEPTED.toString())));
//        assertTrue(bidsBefore.stream().allMatch(p -> !p.getStatus().equals(Bid.Status.DENIED.toString())));
//
//
//        requestUpdateProposal(proposalId, proposal);
//
//        List<Bid> bidsAfter  = proposalRepository.getAllBids(proposalId);
//
//        assertEquals(proposalRepository.findById(proposalId).get().getStatus(),
//                newStatus);
//
//        assertEquals(bidsAfter.size(), bidsBefore.size());
//
//        assertTrue(bidsAfter.stream().anyMatch(p -> p.getStatus().equals(Bid.Status.ACCEPTED.toString())));
//        assertTrue(bidsAfter.stream().anyMatch(p -> p.getStatus().equals(Bid.Status.DENIED.toString())));
//        assertFalse(bidsAfter.stream().anyMatch(p -> p.getStatus().equals(Bid.Status.WAITING.toString())));
//
//    }
//
//    private void requestUpdateProposal(long proposalId, Proposal proposal) throws Exception{
//
//        this.mockMvc.perform(put("/proposals/" + proposalId)
//                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//                .content(objectMapper.writeValueAsString(proposal)))
//                .andExpect(status().isOk());
//    }
//}