//package pt.unl.fct.ecma.services;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.models.Company;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.errors.NotFoundException;
//import pt.unl.fct.ecma.models.EmployeeWithPw;
//import pt.unl.fct.ecma.repositories.CompanyRepository;
//import pt.unl.fct.ecma.repositories.EmployeeRepository;
//
//import java.util.Optional;
//
//@Service
//public class CompanyService {
//
//    @Autowired
//    private CompanyRepository companyRepository;
//
//    @Autowired
//    private EmployeeRepository employeeRepository;
//
//    public void addAdmin(EmployeeWithPw employee, Company company) {
//        Employee dbemp = new Employee();
//        dbemp.setJob(employee.getJob());
//        dbemp.setName(employee.getName());
//        dbemp.setEmail(employee.getEmail());
//        dbemp.setPassword(new BCryptPasswordEncoder().encode(employee.getPassword()));
//        dbemp.setUsername(employee.getUsername());
//        company.getEmployees().add(dbemp);
//        dbemp.setAdmin(true);
//        dbemp.setCompany(company);
//        companyRepository.save(company);
//    }
//
//    public void addCompany(Company company) {
//        companyRepository.save(company);
//    }
//
//    public void addEmployee(EmployeeWithPw employee, Company company) {
//        Employee dbemp = new Employee();
//        dbemp.setJob(employee.getJob());
//        dbemp.setName(employee.getName());
//        dbemp.setEmail(employee.getEmail());
//        dbemp.setPassword(new BCryptPasswordEncoder().encode(employee.getPassword()));
//        dbemp.setUsername(employee.getUsername());
//        company.getEmployees().add(dbemp);
//        dbemp.setCompany(company);
//        companyRepository.save(company);
//    }
//
//    public void deleteEmployee(Employee employee) {
//        employeeRepository.delete(employee);
//    }
//
//    public void deleteCompany(Company company) {
//        companyRepository.delete(company);
//    }
//
//    public Page<Employee> getAdminsOfCompany(Company company, Pageable pageable) {
//        return companyRepository.getAdminsOfCompany(company.getId(), pageable);
//    }
//
//    public Page<Company> getAllCompanies(Pageable pageable) {
//        return companyRepository.findAll(pageable);
//    }
//
//    public Page<Company> getCompaniesByName(Pageable pageable, String name) {
//        return companyRepository.findByName(name, pageable);
//    }
//
//    public Company getCompany(Long companyId) {
//        Optional<Company> company = companyRepository.findById(companyId);
//        if (company.isPresent()) {
//            return company.get();
//        } else throw new NotFoundException(String.format("Company with id %d does not exist", companyId));
//    }
//
//
//    public Page<Employee> getAllEmployeesOfCompany(Pageable pageable, Company company) {
//        return companyRepository.getAllEmployees(company.getId(), pageable);
//    }
//
//    public Page<Employee> getAllEmployeesOfCompanyByName(Pageable pageable, Company company, String name) {
//        return companyRepository.getEmployeesByName(name, company.getId(), pageable);
//    }
//
//    public void updateCompany(Company company, Company oldCompany) {
//        oldCompany.setAddress(company.getAddress());
//        oldCompany.setEmail(company.getEmail());
//        oldCompany.setName(company.getName());
//        companyRepository.save(oldCompany);
//    }
//
//
//    public boolean isAdminOfCompany(Employee employee, Company company) {
//        return employee.isAdmin() && employee.getCompany().getId().equals(company.getId());
//    }
//
//    public boolean employeeBelongsToCompany(Employee employee, Company company) {
//        return employee.getCompany().getId().equals(company.getId());
//    }
//}
