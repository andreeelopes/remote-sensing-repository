//package pt.unl.fct.ecma.services;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import pt.unl.fct.ecma.errors.BadRequestException;
//import pt.unl.fct.ecma.errors.NotFoundException;
//import pt.unl.fct.ecma.models.Proposal;
//import pt.unl.fct.ecma.models.Section;
//import pt.unl.fct.ecma.repositories.ProposalRepository;
//import pt.unl.fct.ecma.repositories.SectionRepository;
//
//import java.util.Optional;
//
//@Service
//public class SectionService {
//
//    @Autowired
//    private SectionRepository sectionRepository;
//
//    public void deleteSection(Section section) {
//        sectionRepository.delete(section);
//    }
//
//    public void addSection(Section section) {
//        sectionRepository.save(section);
//    }
//
//    public void updateSection(Section section, Section oldSection) {
//        oldSection.setText(section.getText());
//        oldSection.setType(section.getType());
//
//        sectionRepository.save(oldSection);
//    }
//
//    public Page<Section> getProposalsSections(Pageable pageable, Proposal proposal) {
//        return sectionRepository.findAllByProposal_Id(proposal.getId(), pageable);
//    }
//
//    public Section getSection(Long sectionId) {
//        Optional<Section> section = sectionRepository.findById(sectionId);
//        if (section.isPresent()) {
//            return section.get();
//        } else
//            throw new NotFoundException(String.format("Section with id %d does not exist", sectionId));
//    }
//
//
//    public boolean sectionBelongsToProposal(Section section, Proposal proposal) {
//        if (section.getProposal().equals(proposal))
//            return true;
//        else
//            throw new BadRequestException(String.format("Section with id %d does not belong to proposal with id %d",
//                    section.getId(), proposal.getId()));
//    }
//}
