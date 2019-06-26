//package pt.unl.fct.ecma;
//
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
//import static org.junit.Assert.assertTrue;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static pt.unl.fct.ecma.utils.Utils.authenticateUser;
//import static pt.unl.fct.ecma.utils.Utils.toList;
//
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class SectionControllerTest {
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
//    private SectionRepository sectionRepository;
//
//    private MockMvc mockMvc;
//
//    @Autowired
//    ObjectMapper objectMapper;
//
//
//    @Before
//    public void init() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
//                .alwaysDo(MockMvcResultHandlers.print())
//                .build();
//
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
//        Employee employee1 = new Employee("test1", "test1",
//                "test1@gmail.com", "Tile Painter", true,
//                "password");
//
//        Employee employee12 = new Employee("test12", "test12",
//                "test12@gmail.com", "Tile Painter", false,
//                "password");
//
//        Employee employee2 = new Employee("test2", "test2",
//                "test2@gmail.com", "Nails Painter", false, "password");
//
//        employee1.setCompany(company1);
//        employee12.setCompany(company1);
//        employee2.setCompany(company2);
//
//        companyRepository.save(company1);
//        companyRepository.save(company2);
//
//        employeeRepository.save(employee1);
//        employeeRepository.save(employee12);
//        employeeRepository.save(employee2);
//
//
//        Proposal prop = new Proposal();
//
//        ProposalRole role = new ProposalRole();
//        ProposalRoleKey proposalRoleKey = new ProposalRoleKey();
//        proposalRoleKey.setEmployee(employee12);
//        proposalRoleKey.setProposal(prop);
//
//        role.setPk(proposalRoleKey);
//        role.setRole(ProposalRole.Role.STAFF.toString());
//
//        prop.setCompanyProposed(company1);
//        prop.setApprover(employee1);
//        prop.getTeam().add(role);
//        prop.setStatus(Proposal.Status.PLACED.toString());
//        prop.setPartnerCompany(company2);
//
//        proposalRepository.save(prop);
//
//        Section title = new Section();
//        title.setText("Testing is really boring");
//        title.setType(Section.Type.TITLE.toString());
//        title.setProposal(prop);
//
//        sectionRepository.save(title);
//    }
//
//    @Test
//    public void testAddSection() throws Exception {
//        authenticateUser("test12", "password");
//
//        Section section = new Section();
//        section.setType(Section.Type.BUDGET.toString());
//        section.setText("0");
//        section.setProposal(proposalRepository.findById(1L).get());
//
//        requestAddSection(section);
//
//        List<Section> sections = requestProposalSections(1L);
//
//        assertEquals(sections.size(), 2);
//
//        assertTrue(sections.stream().anyMatch((p) ->
//                p.getType().equals(section.getType())
//                        && p.getText().equals(section.getText())));
//
//    }
//
//
//    @Test
//    public void testDeleteSection() throws Exception {
//        authenticateUser("test12", "password");
//
//        requestDeleteSection();
//
//        List<Section> sections = requestProposalSections(1L);
//        assertEquals(sections.size(), 0);
//    }
//
//
//    @Test
//    public void testProposalSections() throws Exception {
//        authenticateUser("test12", "password");
//
//        Section titleSection = sectionRepository.findById(1L).get();
//
//        List<Section> requestedSections = requestProposalSections(1L);
//
//        assertEquals(requestedSections.size(), 1);
//
//        assertTrue(requestedSections.stream()
//                .anyMatch((s) -> s.getText().equals(titleSection.getText())
//                        && s.getType().equals(titleSection.getType())));
//    }
//
//
//    @Test
//    public void testUpdateSection() throws Exception {
//        Section section = sectionRepository.findById(1L).get();
//
//        String newText = "Boring...";
//        section.setText(newText);
//
//        authenticateUser("test12", "password");
//        requestUpdateSection(section);
//
//        assertEquals(requestProposalSections(1L).size(), 1);
//        assertEquals(sectionRepository.findById(1L).get().getText(), newText);
//
//    }
//
//    private void requestUpdateSection(Section section) throws Exception {
//
//        this.mockMvc.perform(put("/proposals/" + 1L + "/sections/" + 1L)
//                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//                .content(objectMapper.writeValueAsString(section)))
//                .andExpect(status().isOk());
//    }
//
//    private void requestAddSection(Section section) throws Exception {
//        this.mockMvc.perform(post("/proposals/" + 1L + "/sections/")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content( objectMapper.writeValueAsString(section)))
//                .andExpect(status().isOk());
//    }
//
//    private void requestDeleteSection() throws Exception {
//        this.mockMvc.perform(delete("/proposals/" + 1L + "/sections/" + 1L))
//                .andExpect(status().isOk())
//                .andReturn();
//    }
//
//
//    private List<Section> requestProposalSections(Long proposalId) throws Exception {
//
//        final MvcResult result = this.mockMvc.perform(get("/proposals/" + proposalId + "/sections/"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andReturn();
//
//        String sectionsJson = result.getResponse().getContentAsString();
//
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Section.class);
//
//        return toList(objectMapper, sectionsJson, type);
//    }
//
//}
