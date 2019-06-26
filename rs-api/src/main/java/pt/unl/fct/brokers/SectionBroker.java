//package pt.unl.fct.ecma.brokers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.models.Proposal;
//import pt.unl.fct.ecma.models.Section;
//import pt.unl.fct.ecma.services.ProposalService;
//import pt.unl.fct.ecma.services.SectionService;
//
//@Service
//public class SectionBroker {
//
//    @Autowired
//    private SectionService sectionService;
//
//    @Autowired
//    private ProposalService proposalService;
//
//    public void addSection(Section section) {
//        proposalService.getProposal(section.getProposal().getId());
//        sectionService.addSection(section);
//    }
//
//    public void deleteSection(Long proposalId, Long sectionId) {
//        Section section = sectionService.getSection(sectionId);
//        Proposal proposal = proposalService.getProposal(proposalId);
//
//        if (sectionService.sectionBelongsToProposal(section, proposal))
//            sectionService.deleteSection(section);
//    }
//
//    public Page<Section> getProposalsSections(Pageable pageable, Long proposalId) {
//        Proposal proposal = proposalService.getProposal(proposalId);
//
//        return sectionService.getProposalsSections(pageable, proposal);
//    }
//
//    public void updateSection(Section section) {
//
//        Section oldSection = sectionService.getSection(section.getId());
//        Proposal oldProposal = proposalService.getProposal(section.getProposal().getId());
//
//        if (sectionService.sectionBelongsToProposal(oldSection, oldProposal))
//            sectionService.updateSection(section, oldSection);
//    }
//}
//
