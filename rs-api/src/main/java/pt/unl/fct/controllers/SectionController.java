//package pt.unl.fct.ecma.controllers;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//import pt.unl.fct.ecma.api.SectionsApi;
//import pt.unl.fct.ecma.brokers.SectionBroker;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.models.Section;
//import pt.unl.fct.ecma.security.annotations.BelongsToProposalStaff;
//import pt.unl.fct.ecma.security.annotations.BelongsToProposalTeamOrIsApprover;
//
//import javax.validation.Valid;
//
//@RestController
//public class SectionController implements SectionsApi {
//
//    @Autowired
//    private SectionBroker sectionBroker;
//
//    @BelongsToProposalStaff
//    @Override
//    public void addSection(@PathVariable("proposalId") Long proposalId,
//                           @Valid @RequestBody Section section) {
//
//        if (!section.getProposal().getId().equals(proposalId))
//            throw new BadRequestException("Ids of proposal do not match");
//
//        if (section.getId() != null)
//            throw new BadRequestException("Can not define id of new section");
//
//        sectionBroker.addSection(section);
//    }
//
//    @BelongsToProposalStaff
//    @Override
//    public void deleteSection(@PathVariable("proposalId") Long proposalId,
//                              @PathVariable("sectionId") Long sectionId) {
//        sectionBroker.deleteSection(proposalId, sectionId);
//    }
//
//    @BelongsToProposalTeamOrIsApprover
//    @Override
//    public Page<Section> getProposalSections(Pageable pageable,
//                                             @PathVariable("proposalId") Long proposalId) {
//        return sectionBroker.getProposalsSections(pageable, proposalId);
//    }
//
//    @BelongsToProposalStaff
//    @Override
//    public void updateSection(@Valid @RequestBody Section section,
//                              @PathVariable("sectionId") Long sectionId,
//                              @PathVariable("proposalId") Long proposalId) {
//
//        if (!section.getProposal().getId().equals(proposalId))
//            throw new BadRequestException("Ids of proposal do not match");
//
//        if (!section.getId().equals(sectionId))
//            throw new BadRequestException("Ids of section do not match");
//
//        sectionBroker.updateSection(section);
//    }
//}
