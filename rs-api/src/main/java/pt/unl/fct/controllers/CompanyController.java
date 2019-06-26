//package pt.unl.fct.ecma.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import pt.unl.fct.ecma.api.CompaniesApi;
//import pt.unl.fct.ecma.brokers.CompanyBroker;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.models.Company;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.models.EmployeeWithPw;
//import pt.unl.fct.ecma.security.annotations.IsAdminOfCompany;
//import pt.unl.fct.ecma.security.annotations.isSuperAdminOrAdmin;
//
//import javax.validation.Valid;
//
//@RestController
//public class CompanyController implements CompaniesApi {
//
//    @Autowired
//    private CompanyBroker companyBroker;
//
//
//    @isSuperAdminOrAdmin
//    @Override
//    public void addAdmin(@Valid @RequestBody EmployeeWithPw employee,
//                         @PathVariable("companyId") Long companyId) {
//        companyBroker.addAdmin(employee, companyId);
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @Override
//    public void addCompany(@Valid @RequestBody Company company) {
//
//        if (company.getId() != null)
//            throw new BadRequestException("Can not define id of new company");
//
//        companyBroker.addCompany(company);
//    }
//
//    @IsAdminOfCompany
//    @Override
//    public void addEmployee(@Valid @RequestBody EmployeeWithPw employee,
//                            @PathVariable("companyId") Long companyId) {
//        companyBroker.addEmployee(employee, companyId);
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @Override
//    public void deleteAdmin(@PathVariable("companyId") Long companyId,
//                            @PathVariable("adminId") Long adminId) {
//        companyBroker.deleteAdmin(companyId, adminId);
//    }
//
//    @isSuperAdminOrAdmin
//    @Override
//    public void deleteCompany(@PathVariable("companyId") Long companyId) {
//        companyBroker.deleteCompany(companyId);
//    }
//
//    @IsAdminOfCompany
//    @Override
//    public void fireEmployee(@PathVariable("companyId") Long companyId,
//                             @PathVariable("employeeId") Long employeeId) {
//        companyBroker.deleteEmployee(companyId, employeeId);
//    }
//
//    @Override
//    public Page<Employee> getAdminsOfCompany(Pageable pageable,
//                                             @PathVariable("companyId") Long companyId) {
//        return companyBroker.getAdminsOfCompany(companyId, pageable);
//    }
//
//    @Override
//    public Page<Company> getCompanies(Pageable pageable,
//                                      @Valid @RequestParam(value = "search", required = false) String search) {
//        if (search == null)
//            return companyBroker.getAllCompanies(pageable);
//        else
//            return companyBroker.getCompaniesByName(pageable, search);
//    }
//
//    @Override
//    public Company getCompany(@PathVariable("companyId") Long companyId) {
//        return companyBroker.getCompany(companyId);
//    }
//
//    @Override
//    public Page<Employee> getEmployeesOfCompany(Pageable pageable,
//                                                @PathVariable("companyId") Long companyId,
//                                                @Valid @RequestParam(value = "search", required = false) String search) {
//        if (search == null)
//            return companyBroker.getAllEmployeesOfCompany(pageable, companyId);
//        else
//            return companyBroker.getAllEmployeesOfCompanyByName(pageable, companyId, search);
//    }
//
//    @IsAdminOfCompany
//    @Override
//    public void updateCompany( @Valid @RequestBody Company company,
//                               @PathVariable("companyId") Long companyId) {
//
//        if (!company.getId().equals(companyId))
//            throw new BadRequestException("Ids of company do not match");
//
//        companyBroker.updateCompany(company);
//    }
//}
