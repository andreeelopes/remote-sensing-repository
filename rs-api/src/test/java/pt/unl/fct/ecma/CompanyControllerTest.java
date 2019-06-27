//package pt.unl.fct.ecma;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.JavaType;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import com.jayway.jsonpath.JsonPath;
//import net.minidev.json.JSONObject;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//import pt.unl.fct.ecma.models.Bid;
//import pt.unl.fct.ecma.models.Company;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.repositories.CompanyRepository;
//import pt.unl.fct.ecma.repositories.EmployeeRepository;
//
//
//import java.security.Principal;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//
//import static org.hamcrest.Matchers.*;
//import static org.junit.Assert.assertTrue;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static pt.unl.fct.ecma.utils.Utils.authenticateUser;
//import static pt.unl.fct.ecma.utils.Utils.toList;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class CompanyControllerTest {
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
//    ObjectMapper objectMapper;
//
//    private MockMvc mockMvc;
//
//    @Before
//    public void init() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//
//        // Data Fixture
//        companyRepository.deleteAll();
//        employeeRepository.deleteAll();
//
//        Company company2= new Company();
//        company2.setName("MountainDew");
//        company2.setEmail("mountaindew@outlook");
//        company2.setAddress("Lisboa");
//
//        Employee emp = new Employee();
//        emp.setEmail("simon@gmail.com");
//        emp.setJob("Informatico");
//        emp.setName("Simon");
//        emp.setUsername("simon");
//        emp.setPassword("simon");
//        emp.setAdmin(true);
//
//        emp.setCompany(company2);
//        companyRepository.save(company2);
//        employeeRepository.save(emp);
//
//    }
//
//    private List<Company> getCompanies(int sizeExpected) throws Exception {
//
//        final MvcResult result = this.mockMvc.perform(get("/companies"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(jsonPath("$.content", hasSize(sizeExpected)))
//                .andReturn()
//        ;
//        String list = result.getResponse().getContentAsString();
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Company.class);
//
//        return toList(objectMapper,list,type);
//    }
//
//    @Test
//    public void testGetCompanies() throws Exception {
//
//        List<Company> companies = getCompanies(1);
//        assertTrue(companies.stream().anyMatch( (p) -> p.getName().equals("MountainDew")));
//    }
//
//    @Test
//    public void testAddAdmin() throws Exception{
//
//        authenticateUser("admin","password");
//        List<Company> companies = getCompanies(1);
//        Company company=companies.get(0);
//        this.mockMvc.perform(post("/companies/"+company.getId()+"/admins")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content("{\"id\":\"2\",\"name\":\"Andre\", \"job\":\"Canalizador\", \"username\":\"andre\" , \"email\":\"andre@\" , \"password\":\"" + "andre" +"\"}"))
//                .andExpect(status().isOk());
//        this.mockMvc.perform(get("/companies/"+company.getId()+"/admins"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(jsonPath("$.content", hasSize(2)))
//        ;
//    }
//
//    @Test
//    public void testGetAdmins() throws  Exception{
//        List<Company> companies = getCompanies(1);
//        Company company=companies.get(0);
//
//        List<Employee> admins = getAdmins(company);
//
//        assertTrue(admins.stream().anyMatch( (p) -> p.getName().equals("Simon")));
//    }
//
//    private List<Employee> getAdmins(Company company) throws Exception {
//        final MvcResult result = this.mockMvc.perform(get("/companies/"+company.getId()+"/admins"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(jsonPath("$.content", hasSize(1)))
//                .andReturn()
//        ;
//        String list = result.getResponse().getContentAsString();
//
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Employee.class);
//
//
//        return toList(objectMapper,list,type);
//    }
//
//    @Test
//    public void testAddCompany() throws Exception{
//        authenticateUser("admin","password");
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        Company company = new Company();
//        company.setName("RedBull");
//        company.setEmail("redbull@outlook");
//        company.setAddress("Lisboa");
//        String json = mapper.writeValueAsString(company);
//
//        this.mockMvc.perform(post("/companies")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isOk());
//        List<Company> companies = getCompanies(2);
//
//        company = new Company();
//        company.setName("RedBull");
//        company.setEmail("redbull@outlook");
//        company.setAddress("Lisboa");
//        company.setId(2L);
//        json = mapper.writeValueAsString(company);
//
//        this.mockMvc.perform(post("/companies")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(json))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void testAddEmployee() throws Exception{
//        authenticateUser("simon","simon");
//
//        List<Company> companies = getCompanies(1);
//        Company company=companies.get(0);
//        this.mockMvc.perform(post("/companies/"+company.getId()+"/employees")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content("{\"id\":\"2\",\"name\":\"Andre\", \"job\":\"Canalizador\", \"username\":\"andre\" , \"email\":\"andre@\" , \"password\":\"" + "andre" + "\"}"))
//                .andExpect(status().isOk());
//        List<Employee> admins = getProducts(company,1);
//
//        assertTrue(admins.stream().anyMatch( (p) -> p.getName().equals("Andre")));
//    }
//
//    private List<Employee> getProducts(Company company,int sizeExpected) throws Exception {
//        final MvcResult result = this.mockMvc.perform(get("/companies/" + company.getId() + "/employees"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(jsonPath("$.content", hasSize(sizeExpected)))
//        .andReturn();
//
//        String list = result.getResponse().getContentAsString();
//        JavaType type = objectMapper.getTypeFactory().
//                constructCollectionType(List.class, Employee.class);
//        toList(objectMapper,list,type);
//
//        return toList(objectMapper,list,type);
//    }
//
//    @Test
//    public void testGetEmployees() throws Exception{
//        List<Company> companies = getCompanies(1);
//        Company company = companies.get(0);
//
//        getProducts(company,0);
//    }
//
//    @Test
//    public void testGetCompanyById() throws Exception{
//        List<Company> companies = getCompanies(1);
//        Company company = companies.get(0);
//        this.mockMvc.perform(get("/companies/" + company.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                ;
//        this.mockMvc.perform(get("/companies/"+2))
//                .andExpect(status().isNotFound())
//                ;
//    }
//
//    @Test
//    public void testDeleteAdmin() throws Exception{
//        authenticateUser("admin","password");
//
//        List<Company> companies = getCompanies(1);
//        Company company = companies.get(0);
//        List<Employee> admins = getAdmins(company);
//        Employee admin = admins.get(0);
//        Long random =company.getId()+1L;
//        this.mockMvc.perform(delete("/companies/" + random + "/admins/"+admin.getId()))
//                .andExpect(status().isNotFound());
//        this.mockMvc.perform(delete("/companies/" + company.getId() + "/admins/"+admin.getId()))
//                .andExpect(status().isOk());
//        this.mockMvc.perform(delete("/companies/" + company.getId() + "/admins/"+admin.getId()))
//                .andExpect(status().isNotFound());
//    }
//    @Test
//    public void testDeleteEmployee() throws Exception{
//        authenticateUser("simon","simon");
//
//        List<Company> companies = getCompanies(1);
//        Company company=companies.get(0);
//        this.mockMvc.perform(post("/companies/" + company.getId() + "/employees")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content("{\"id\":\"2\",\"name\":\"Andre\", \"job\":\"Canalizador\", \"username\":\"andre\" , \"email\":\"andre@\" , \"password\":\"" + "andre" + "\"}"))
//                .andExpect(status().isOk());
//
//        List<Employee> employees = getProducts(company,1);
//        Employee employee = employees.get(0);
//
//        this.mockMvc.perform(delete("/companies/"+company.getId() + "/employees/" + employee.getId()))
//                .andExpect(status().isOk());
//        this.mockMvc.perform(delete("/companies/"+company.getId() + "/employees/" + employee.getId()))
//                .andExpect(status().isNotFound());
//    }
//    @Test
//    public void testDeleteCompany()throws Exception{
//        authenticateUser("simon","simon");
//
//        List<Company> companies = getCompanies(1);
//        Company company = companies.get(0);
//
//        this.mockMvc.perform(delete("/companies/" + company.getId()))
//                .andExpect(status().isOk());
//
//        getCompanies(0);
//    }
//}