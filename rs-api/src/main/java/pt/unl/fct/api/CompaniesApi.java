///**
// * NOTE: This class is auto generated by the swagger code generator program (2.3.1).
// * https://github.com/swagger-api/swagger-codegen
// * Do not edit the class manually.
// */
//package pt.unl.fct.ecma.api;
//
//
//import io.swagger.annotations.*;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.web.bind.annotation.*;
//import pt.unl.fct.ecma.models.Company;
//import pt.unl.fct.ecma.models.Employee;
//import pt.unl.fct.ecma.models.EmployeeWithPw;
//
//import javax.validation.Valid;
//
//@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-25T09:46:01.754Z")
//
//@Api(value = "companies", description = "the companies API")
//public interface CompaniesApi {
//
//    @ApiOperation(value = "Add a new admin to the company", nickname = "addAdmin", notes = "", tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Added a new Admin to the company"),
//            @ApiResponse(code = 405, message = "Invalid input"),
//            @ApiResponse(code = 409, message = "Employee already exists")})
//    @RequestMapping(value = "/companies/{companyId}/admins",
//            produces = {"application/json"},
//            consumes = {"application/json"},
//            method = RequestMethod.POST)
//    void addAdmin(@ApiParam(value = "Admin object that needs to be added to the collection", required = true)
//                  @Valid @RequestBody EmployeeWithPw employee,
//                  @ApiParam(value = "Company ID", required = true) @PathVariable("companyId") Long companyId);
//
//
//    @ApiOperation(value = "Add a new partner company to the collection", nickname = "addCompany", notes = "", tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Added a new company"),
//            @ApiResponse(code = 405, message = "Invalid input"),
//            @ApiResponse(code = 409, message = "Company already exists")})
//    @RequestMapping(value = "/companies",
//            produces = {"application/json"},
//            consumes = {"application/json"},
//            method = RequestMethod.POST)
//    void addCompany(@ApiParam(value = "Company object that needs to be added to the collection", required = true)
//                    @Valid @RequestBody Company company);
//
//
//    @ApiOperation(value = "Add a new employee to the collection", nickname = "addEmployee", notes = "", tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Added a new Employee to the company"),
//            @ApiResponse(code = 405, message = "Invalid input"),
//            @ApiResponse(code = 409, message = "Employee already exists")})
//    @RequestMapping(value = "/companies/{companyId}/employees",
//            produces = {"application/json"},
//            consumes = {"application/json"},
//            method = RequestMethod.POST)
//    void addEmployee(@ApiParam(value = "Employee object that needs to be added to the collection", required = true)
//                     @Valid @RequestBody EmployeeWithPw employee, @ApiParam(value = "Company ID", required = true)
//                     @PathVariable("companyId") Long companyId);
//
//
//    @ApiOperation(value = "Delete Admin with the ID provided", nickname = "deleteAdmin", notes = "", tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation"),
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Company not found")})
//    @RequestMapping(value = "/companies/{companyId}/admins/{adminId}",
//            method = RequestMethod.DELETE)
//    void deleteAdmin(@ApiParam(value = "Proposal ID", required = true) @PathVariable("companyId") Long companyId,
//                     @ApiParam(value = "Partner ID", required = true) @PathVariable("adminId") Long adminId);
//
//
//    @ApiOperation(value = "Delete company with the ID provided", nickname = "deleteCompany", notes = "", tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation"),
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Company not found")})
//    @RequestMapping(value = "/companies/{companyId}",
//            method = RequestMethod.DELETE)
//    void deleteCompany(@ApiParam(value = "ID of the company to delete", required = true) @PathVariable("companyId") Long companyId);
//
//
//    @ApiOperation(value = "Delete employee with the ID provided", nickname = "fireEmployee", notes = "", tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation"),
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Company not found")})
//    @RequestMapping(value = "/companies/{companyId}/employees/{employeeId}",
//            method = RequestMethod.DELETE)
//    void fireEmployee(@ApiParam(value = "Proposal ID", required = true) @PathVariable("companyId") Long companyId,
//                      @ApiParam(value = "Partner ID", required = true) @PathVariable("employeeId") Long employeeId);
//
//
//    @ApiOperation(value = "Get the list of all employees of a company", nickname = "getAdminsOfCompany", notes = "", response = Employee.class, responseContainer = "List", tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation", response = Employee.class, responseContainer = "List")})
//    @RequestMapping(value = "/companies/{companyId}/admins",
//            produces = {"application/json"},
//            method = RequestMethod.GET)
//    Page<Employee> getAdminsOfCompany(Pageable pageable,
//                                      @ApiParam(value = "Company ID", required = true) @PathVariable("companyId") Long companyId);
//
//
//    @ApiOperation(value = "Get the list of all companies", nickname = "getCompanies", notes = "", response = Company.class, responseContainer = "List", tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation", response = Company.class, responseContainer = "List")})
//    @RequestMapping(value = "/companies",
//            produces = {"application/json"},
//            method = RequestMethod.GET)
//    Page<Company> getCompanies(Pageable pageable, @ApiParam(value = "Filter companies by name")
//    @Valid @RequestParam(value = "search", required = false) String search);
//
//
//    @ApiOperation(value = "Find company by ID", nickname = "getCompany", notes = "", response = Company.class, tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation", response = Company.class),
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Company not found")})
//    @RequestMapping(value = "/companies/{companyId}",
//            produces = {"application/json"},
//            method = RequestMethod.GET)
//    Company getCompany(@ApiParam(value = "ID of company to return", required = true) @PathVariable("companyId") Long companyId);
//
//
//    @ApiOperation(value = "Get the list of all employees of a company", nickname = "getEmployeesOfCompany", notes = "", response = Employee.class, responseContainer = "List", tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation", response = Employee.class, responseContainer = "List")})
//    @RequestMapping(value = "/companies/{companyId}/employees",
//            produces = {"application/json"},
//            method = RequestMethod.GET)
//    Page<Employee> getEmployeesOfCompany(Pageable pageable, @ApiParam(value = "Company ID", required = true)
//    @PathVariable("companyId") Long companyId, @ApiParam(value = "Filter employees by name")
//    @Valid @RequestParam(value = "search", required = false) String search);
//
//
//    @ApiOperation(value = "Update an existing company", nickname = "updateCompany", notes = "", tags = {"companies",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Company not found"),
//            @ApiResponse(code = 405, message = "Validation exception")})
//    @RequestMapping(value = "/companies/{companyId}",
//            consumes = {"application/json"},
//            method = RequestMethod.PUT)
//    void updateCompany(@ApiParam(value = "Company object that needs to be updated in the collection", required = true)
//                       @Valid @RequestBody Company company,
//                       @ApiParam(value = "ID of company to return", required = true) @PathVariable("companyId") Long companyId);
//
//}
