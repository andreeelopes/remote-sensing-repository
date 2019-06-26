//package pt.unl.fct.ecma.brokers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.models.Company;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.models.EmployeeWithPw;
//import pt.unl.fct.ecma.services.CompanyService;
//import pt.unl.fct.ecma.services.EmployeeService;
//
//@Service
//public class CompanyBroker {
//
//    @Autowired
//    private CompanyService companyService;
//
//    @Autowired
//    private EmployeeService employeeService;
//
//    public void addAdmin(EmployeeWithPw employee, Long companyId) {
//        Company company = companyService.getCompany(companyId);
//
//        companyService.addAdmin(employee, company);
//    }
//
//    public void addCompany(Company company) {
//        companyService.addCompany(company);
//    }
//
//    public void addEmployee(EmployeeWithPw employee, Long companyId) {
//        Company company = companyService.getCompany(companyId);
//
//        companyService.addEmployee(employee, company);
//    }
//
//    public void deleteAdmin(Long companyId, Long adminId) {
//        Company company = companyService.getCompany(companyId);
//        Employee employee = employeeService.getEmployee(adminId);
//
//        if (companyService.isAdminOfCompany(employee, company))
//            companyService.deleteEmployee(employee);
//    }
//
//    public void deleteCompany(Long companyId) {
//        Company company = companyService.getCompany(companyId);
//
//        companyService.deleteCompany(company);
//    }
//
//    public void deleteEmployee(Long companyId, Long employeeId) {
//        Company company = companyService.getCompany(companyId);
//        Employee employee = employeeService.getEmployee(employeeId);
//
//        if (companyService.employeeBelongsToCompany(employee, company))
//            companyService.deleteEmployee(employee);
//    }
//
//    public Page<Employee> getAdminsOfCompany(Long companyId, Pageable pageable) {
//        Company company = companyService.getCompany(companyId);
//
//        return companyService.getAdminsOfCompany(company, pageable);
//    }
//
//    public Page<Company> getAllCompanies(Pageable pageable) {
//        return companyService.getAllCompanies(pageable);
//    }
//
//    public Page<Company> getCompaniesByName(Pageable pageable, String search) {
//        return companyService.getCompaniesByName(pageable, search);
//    }
//
//    public Company getCompany(Long companyId) {
//        return companyService.getCompany(companyId);
//    }
//
//    public Page<Employee> getAllEmployeesOfCompany(Pageable pageable, Long companyId) {
//        Company company = companyService.getCompany(companyId);
//
//        return companyService.getAllEmployeesOfCompany(pageable, company);
//    }
//
//    public Page<Employee> getAllEmployeesOfCompanyByName(Pageable pageable, Long companyId, String search) {
//        Company company = companyService.getCompany(companyId);
//
//        return companyService.getAllEmployeesOfCompanyByName(pageable, company, search);
//    }
//
//    public void updateCompany(Company company) {
//        Company oldCompany = companyService.getCompany(company.getId());
//
//        companyService.updateCompany(company, oldCompany);
//    }
//
//
//}
