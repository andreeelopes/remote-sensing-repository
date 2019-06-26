//package pt.unl.fct.ecma.api;
//
//import io.swagger.annotations.*;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RequestPart;
//import org.springframework.web.multipart.MultipartFile;
//import pt.unl.fct.ecma.models.Section;
//
//import javax.validation.Valid;
//import javax.validation.constraints.*;
//import java.util.List;
//
//@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-11-03T10:56:43.867Z")
//
//@Api(value = "sections", description = "the sections API")
//public interface SectionsApi {
//
//
//    @ApiOperation(value = "Add a new section to the proposal", nickname = "addSection", notes = "", tags = {"sections",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Added a new Section"),
//            @ApiResponse(code = 405, message = "Invalid input")})
//    @RequestMapping(value = "/proposals/{proposalId}/sections/",
//            consumes = {"application/json"},
//            method = RequestMethod.POST)
//    void addSection(@ApiParam(value = "Proposal ID", required = true) @PathVariable("proposalId") Long proposalId,
//                    @ApiParam(value = "Section object to add to the proposal", required = true)
//                    @Valid @RequestBody Section section);
//
//
//    @ApiOperation(value = "Delete section with the ID provided", nickname = "deleteSection", notes = "", tags = {"sections",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation"),
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Proposal not found")})
//    @RequestMapping(value = "/proposals/{proposalId}/sections/{sectionId}",
//            method = RequestMethod.DELETE)
//    void deleteSection(@ApiParam(value = "Proposal ID", required = true) @PathVariable("proposalId") Long proposalId,
//                       @ApiParam(value = "Section ID", required = true) @PathVariable("sectionId") Long sectionId);
//
//
//    @ApiOperation(value = "Get all sections by the proposal ID", nickname = "getProposalSections",
//            notes = "Returns all sections", response = Section.class, responseContainer = "List", tags = {"sections",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful operation", response = Section.class, responseContainer = "List"),
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Proposal not found")})
//    @RequestMapping(value = "/proposals/{proposalId}/sections/",
//            produces = {"application/json"},
//            method = RequestMethod.GET)
//    Page<Section> getProposalSections(Pageable pageable, @ApiParam(value = "Proposal ID", required = true) @PathVariable("proposalId") Long proposalId);
//
//
//    @ApiOperation(value = "Update sections with ID provided", nickname = "updateSection", notes = "", tags = {"sections",})
//    @ApiResponses(value = {
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Proposal or section not found"),
//            @ApiResponse(code = 405, message = "Validation exception")})
//    @RequestMapping(value = "/proposals/{proposalId}/sections/{sectionId}",
//            consumes = {"application/json"},
//            method = RequestMethod.PUT)
//    void updateSection(@ApiParam(value = "Section object that needs to be updated in the collection", required = true)
//                       @Valid @RequestBody Section section, @ApiParam(value = "Section ID", required = true)
//    @PathVariable("sectionId") Long sectionId, @ApiParam(value = "ID of proposal to return", required = true) @PathVariable("proposalId") Long proposalId);
//
//}
//
