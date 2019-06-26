//package pt.unl.fct.ecma;
//
//import com.fasterxml.jackson.databind.JavaType;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
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
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotEquals;
//import static org.junit.Assert.assertTrue;
//
//import java.util.List;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static pt.unl.fct.ecma.utils.Utils.toList;
//
//import pt.unl.fct.ecma.utils.Utils;
//
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class EmployeeControllerTest {
//
//    @Autowired
//    private WebApplicationContext wac;
//    @Autowired
//    private CompanyRepository companyRepository;
//    @Autowired
//    private EmployeeRepository employeeRepository;
//    @Autowired
//    private ProposalRepository proposalRepository;
//    @Autowired
//    private BidRepository bidRepository;
//    @Autowired
//    private ProposalRoleRepository proposalRoleRepository;
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
//        bidRepository.deleteAll();
//        proposalRoleRepository.deleteAll();
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
//                "test2@gmail.com", "Nails Painter", false, "password");
//
//        employee1.setCompany(company1);
//        employee2.setCompany(company2);
//
//        employee1 = employeeRepository.save(employee1);
//        employee2 = employeeRepository.save(employee2);
//
//        //first proposal
//        Proposal proposal1 = new Proposal();
//        proposal1.setCompanyProposed(company1);
//        proposal1.setApprover(employee1);
//        proposal1.setStatus(Proposal.Status.APPROVED.toString());
//        proposal1.setPartnerCompany(company2);
//
//        //second proposal
//        Proposal proposal2 = new Proposal();
//        proposal2.setCompanyProposed(company2);
//        proposal2.setApprover(employee2);
//        proposal2.setStatus(Proposal.Status.APPROVED.toString());
//        proposal2.setPartnerCompany(company1);
//
//        proposal1 = proposalRepository.save(proposal1);
//        proposal2 = proposalRepository.save(proposal2);
//
//        //Bid 1 for proposal 1
//        Bid bid1 = new Bid();
//        BidKey bid1Key = new BidKey();
//        bid1Key.setProposal(proposal1);
//        bid1Key.setBidder(employee2);
//        bid1.setPk(bid1Key);
//        bid1.setStatus(Bid.Status.ACCEPTED.toString());
//
//        //Bid 2 for proposal 2
//        Bid bid2 = new Bid();
//        BidKey bid2Key = new BidKey();
//        bid2Key.setProposal(proposal2);
//        bid2Key.setBidder(employee1);
//        bid2.setPk(bid2Key);
//        bid2.setStatus(Bid.Status.ACCEPTED.toString());
//
//        bidRepository.save(bid1);
//        bidRepository.save(bid2);
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
//        //employee 1 partner role
//        ProposalRoleKey compoundKey2 = new ProposalRoleKey();
//        compoundKey2.setEmployee(employee1);
//        compoundKey2.setProposal(proposal2);
//
//        ProposalRole employee1role2 = new ProposalRole();
//        employee1role2.setPk(compoundKey2);
//        employee1role2.setRole(ProposalRole.Role.PARTNER.toString());
//
//        proposalRoleRepository.save(employee1role1);
//        proposalRoleRepository.save(employee1role2);
//    }
//
//    @Test
//    public void testGetEmployee() throws Exception {
//        long id = 1;
//
//        Employee testEmployee = employeeRepository.findById(id).get();
//        Employee requestedEmployee = requestGetEmployee(id);
//
//        assertEquals(requestedEmployee.getUsername(), testEmployee.getUsername());
//    }
//
//    @Test
//    public void testGetEmployees() throws Exception {
//
//        Employee employee1 = employeeRepository.findById(1L).get();
//        Employee employee2 = employeeRepository.findById(2L).get();
//
//        List<Employee> requestedEmployees = requestGetEmployees();
//
//        assertEquals(requestedEmployees.size(), 2);
//        assertTrue(requestedEmployees.stream()
//                .anyMatch((p) -> p.getUsername().equals(employee1.getUsername())));
//        assertTrue(requestedEmployees.stream()
//                .anyMatch((p) -> p.getUsername().equals(employee2.getUsername())));
//    }
//
//    @Test
//    public void testUpdateEmployee() throws Exception {
//
//        long employeeId = 1L;
//
//        Employee employee = employeeRepository.findById(employeeId).get();
//        String newName = "David Gilmour";
//        employee.setName(newName);
//
//        Utils.authenticateUser("test1", "password");
//        requestUpdateEmployee(employeeId, employee);
//        Employee updatedEmployee = requestGetEmployee(employeeId);
//
//        assertEquals(updatedEmployee.getName(), newName);
//    }
//
//    @Test
//    public void testGetEmployeeBids() throws Exception {
//
//        long employeeId = 1L;
//
//        Utils.authenticateUser("test1", "password");
//        List<Bid> bidsOfEmployee = requestGetEmployeeBids(employeeId);
//
//        assertEquals(bidsOfEmployee.size(), 1);
//    }
//
//
//    @Test
//    public void testGetProposalPartner() throws Exception {
//
//        long employeeId = 1L;
//
//        Utils.authenticateUser("test1", "password");
//        List<Proposal> partnerProposalsOfEmployee = requestGetProposalPartner(employeeId);
//
//        assertEquals(partnerProposalsOfEmployee.size(), 1);
//    }
//
//    @Test
//    public void testGetProposalStaff() throws Exception {
//
//        long employeeId = 1L;
//
//        Utils.authenticateUser("test1", "password");
//        List<Proposal> staffProposalsOfEmployee = requestGetProposalStaff(employeeId);
//        assertEquals(staffProposalsOfEmployee.size(), 1);
//    }
//
//
//    //Auxiliary Methods
//
//
//    private Employee requestGetEmployee(long employeeId) throws Exception {
//
//        final MvcResult result = this.mockMvc.perform(get("/employees/" + employeeId))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andReturn();
//
//        String json = result.getResponse().getContentAsString();
//
//        return objectMapper.readValue(json, Employee.class);
//    }
//
//    private List<Employee> requestGetEmployees() throws Exception {
//
//        final MvcResult result = this.mockMvc.perform(get("/employees"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andReturn();
//
//        String jsonList = result.getResponse().getContentAsString();
//
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Employee.class);
//
//        return toList(objectMapper, jsonList, type);
//    }
//
//
//    private void requestUpdateEmployee(long employeeId, Employee updatedEmployee) throws Exception {
//
//        this.mockMvc.perform(put("/employees/" + employeeId)
//                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//                .content(objectMapper.writeValueAsString(updatedEmployee)))
//                .andExpect(status().isOk());
//    }
//
//    private List<Bid> requestGetEmployeeBids(long employeeId) throws Exception {
//
//        final MvcResult result = this.mockMvc.perform(get("/employees/" + employeeId + "/bids"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andReturn();
//
//        String bidsJson = result.getResponse().getContentAsString();
//
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Bid.class);
//
//        return toList(objectMapper, bidsJson, type);
//    }
//
//    private List<Proposal> requestGetProposalPartner(long employeeId) throws Exception {
//
//        final MvcResult result = this.mockMvc.perform(get("/employees/" + employeeId + "/partnerproposals"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andReturn();
//
//        String proposalsJson = result.getResponse().getContentAsString();
//
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Proposal.class);
//
//        return toList(objectMapper, proposalsJson, type);
//    }
//
//    private List<Proposal> requestGetProposalStaff(long employeeId) throws Exception {
//
//        final MvcResult result = this.mockMvc.perform(get("/employees/" + employeeId + "/staffproposals"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andReturn();
//
//        String proposalsJson = result.getResponse().getContentAsString();
//
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Proposal.class);
//
//        return toList(objectMapper, proposalsJson, type);
//    }
//
//
//}
//
//
//
//
