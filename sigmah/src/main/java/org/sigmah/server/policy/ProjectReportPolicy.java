/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.policy;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.sigmah.server.dao.ProjectReportDAO;
import org.sigmah.server.dao.Transactional;
import org.sigmah.server.endpoint.gwtrpc.handler.UpdateProjectHandler;
import org.sigmah.shared.domain.Project;
import org.sigmah.shared.domain.User;
import org.sigmah.shared.domain.element.FlexibleElement;
import org.sigmah.shared.domain.element.ReportElement;
import org.sigmah.shared.domain.report.KeyQuestion;
import org.sigmah.shared.domain.report.ProjectReport;
import org.sigmah.shared.domain.report.ProjectReportModel;
import org.sigmah.shared.domain.report.ProjectReportModelSection;
import org.sigmah.shared.domain.report.RichTextElement;
import org.sigmah.shared.domain.value.Value;

/**
 *
 * @author Raphaël Calabro (rcalabro@ideia.fr)
 */
public class ProjectReportPolicy implements EntityPolicy<ProjectReport> {
    private final ProjectReportDAO dao;
    private final UpdateProjectHandler updateProjectHandler;

    @Inject
    public ProjectReportPolicy(ProjectReportDAO dao, UpdateProjectHandler updateProjectHandler) {
        this.dao = dao;
        this.updateProjectHandler = updateProjectHandler;
    }

    @Override
    public Object create(User user, PropertyMap properties) {
        return createReport(user, properties).getId();
    }

    private void iterateOnSection(ProjectReportModelSection section, List<RichTextElement> elements, ProjectReport report) {
        int areaCount = section.getNumberOfTextarea();

        // Key questions
        List<KeyQuestion> keyQuestions = section.getKeyQuestions();
        if(keyQuestions == null)
            keyQuestions = Collections.emptyList();

        for(int index = 0; index < keyQuestions.size(); index++) {
            final RichTextElement element = new RichTextElement();
            element.setIndex(index);
            element.setSectionId(section.getId());
            element.setReport(report);
            elements.add(element);
        }

        int index = 0;
        
        // Sub sections and rich text elements
        List<ProjectReportModelSection> subSections = section.getSubSections();
        if(subSections == null)
            subSections = Collections.emptyList();

        for(final ProjectReportModelSection subSection : subSections) {
            while(index < subSection.getIndex() && areaCount > 0) {
                // New rich text element
                final RichTextElement element = new RichTextElement();
                element.setIndex(index + keyQuestions.size());
                element.setSectionId(section.getId());
                element.setReport(report);
                elements.add(element);

                index++;
                areaCount--;
            }
            
            iterateOnSection(subSection, elements, report);
        }

        while(areaCount > 0) {
            // New rich text element
            final RichTextElement element = new RichTextElement();
            element.setIndex(index + keyQuestions.size());
            element.setSectionId(section.getId());
            element.setReport(report);
            elements.add(element);

            index++;
            areaCount--;
        }
    }

    @Transactional
    public ProjectReport createReport(User user, PropertyMap properties) {
        final ProjectReport report = new ProjectReport();

        // Defining the common properties
        report.setEditor(user);
        report.setName((String) properties.get("name"));
        report.setLastEditDate(new Date());
        report.setPhaseName((String) properties.get("phaseName"));

        final ProjectReportModel model = dao.findModelById((Integer) properties.get("reportModelId"));
        report.setModel(model);

        final Integer projectId = (Integer) properties.get("projectId");
        if(projectId != null) {
            final Project project = new Project();
            project.setId(projectId);
            report.setProject(project);
        }

        final Integer flexibleElementId = (Integer) properties.get("flexibleElementId");
        if(flexibleElementId != null) {
            final ReportElement element = new ReportElement();
            element.setId(flexibleElementId.longValue());
            report.setFlexibleElement(element);
        }

        // RichTextElements
        final ArrayList<RichTextElement> elements = new ArrayList<RichTextElement>();

        for(final ProjectReportModelSection section : model.getSections())
            iterateOnSection(section, elements, report);

        report.setTexts(elements);

        // Saving
        dao.persist(report);

        // Updating the flexible element
        final Integer containerId = (Integer) properties.get("containerId");
        if(flexibleElementId != null && containerId != null) {
            final Value value = updateProjectHandler.retrieveValue(containerId, flexibleElementId, user);
            value.setValue(report.getId().toString());
            dao.merge(value);
        }

        return report;
    }

    @Override
    public void update(User user, Object entityId, PropertyMap changes) {
        
        for(Map.Entry<String, Object> entry : changes.entrySet()) {
            if("currentPhase".equals(entry.getKey())) {
                final ProjectReport report = dao.findReportById((Integer) entityId);
                report.setPhaseName((String) entry.getValue());
                report.setEditor(user);
                report.setLastEditDate(new Date());
                dao.merge(report);

            } else {
                final RichTextElement element = dao.findRichTextElementById(new Integer(entry.getKey()));
                element.setText((String) entry.getValue());
                dao.merge(element);
            }
        }
    }
    
}
