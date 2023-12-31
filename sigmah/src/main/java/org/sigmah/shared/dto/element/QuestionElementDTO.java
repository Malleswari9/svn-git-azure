/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.dto.element;

import java.util.ArrayList;
import java.util.List;

import org.sigmah.client.i18n.I18N;
import org.sigmah.client.page.project.category.CategoryIconProvider;
import org.sigmah.client.ui.FlexibleGrid;
import org.sigmah.client.util.HistoryTokenText;
import org.sigmah.shared.command.result.ValueResult;
import org.sigmah.shared.command.result.ValueResultUtils;
import org.sigmah.shared.dto.category.CategoryTypeDTO;
import org.sigmah.shared.dto.element.handler.RequiredValueEvent;
import org.sigmah.shared.dto.element.handler.ValueEvent;
import org.sigmah.shared.dto.history.HistoryTokenListDTO;
import org.sigmah.shared.dto.quality.QualityCriterionDTO;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.DOM;

/**
 * 
 * @author Denis Colliot (dcolliot@ideia.fr)
 * 
 */
public class QuestionElementDTO extends FlexibleElementDTO {

    private static final long serialVersionUID = 8520711106031085130L;

    @Override
    public String getEntityName() {
        // Gets the entity name mapped by the current DTO starting from the
        // "server.domain" package name.
        return "element.QuestionElement";
    }

    // Question choices list
    public List<QuestionChoiceElementDTO> getChoicesDTO() {
        return get("choicesDTO");
    }

    public void setChoicesDTO(List<QuestionChoiceElementDTO> choicesDTO) {
        set("choicesDTO", choicesDTO);
    }

    // Question multiple mode
    public Boolean getIsMultiple() {
        return get("isMultiple");
    }

    public void setIsMultiple(Boolean isMultiple) {
        set("isMultiple", isMultiple);
    }

    // Question category type
    public CategoryTypeDTO getCategoryTypeDTO() {
        return get("categoryTypeDTO");
    }

    public void setCategoryTypeDTO(CategoryTypeDTO categoryTypeDTO) {
        set("categoryTypeDTO", categoryTypeDTO);
    }

    // Question quality criterion
    public QualityCriterionDTO getQualityCriterionDTO() {
        return get("qualityCriterionDTO");
    }

    public void setQualityCriterionDTO(QualityCriterionDTO qualityCriterionDTO) {
        set("qualityCriterionDTO", qualityCriterionDTO);
    }

    @Override
    protected Component getComponent(ValueResult valueResult, boolean enabled) {

        // Question's component.
        final Component component;

        // Creates choices store.
        final ListStore<QuestionChoiceElementDTO> store = new ListStore<QuestionChoiceElementDTO>();
        store.add(getChoicesDTO());

        // Creates the listener of selection changes.
        final ComboBoxSelectionListener listener = new ComboBoxSelectionListener();

        // Single selection case.
        if (!Boolean.TRUE.equals(getIsMultiple())) {

            final ComboBox<QuestionChoiceElementDTO> comboBox = new ComboBox<QuestionChoiceElementDTO>();
            comboBox.setEmptyText(I18N.CONSTANTS.flexibleElementQuestionEmptyChoice());

            comboBox.setStore(store);
            comboBox.setFieldLabel(getLabel());
            comboBox.setDisplayField("label");
            comboBox.setValueField("id");
            comboBox.setLabelSeparator("");
            comboBox.setTriggerAction(TriggerAction.ALL);
            comboBox.setEditable(false);
            comboBox.setAllowBlank(true);

            if (getCategoryTypeDTO() != null) {

                for (final QuestionChoiceElementDTO choiceDTO : store.getModels()) {
                    if (choiceDTO.getCategoryElementDTO() != null) {
                        choiceDTO.getCategoryElementDTO().setIconHtml(
                                CategoryIconProvider.getIconHtml(choiceDTO.getCategoryElementDTO(), false));
                    }
                }

                comboBox.setTemplate(CategoryIconProvider.getComboboxIconTemplate());
            }

            if (valueResult != null && valueResult.isValueDefined()) {

                final String idChoice = (String) valueResult.getValueObject();

                for (QuestionChoiceElementDTO choiceDTO : getChoicesDTO()) {
                    if (idChoice.equals(String.valueOf(choiceDTO.getId()))) {
                        comboBox.setValue(choiceDTO);
                        break;
                    }
                }
            }

            // Listens to the selection changes.
            comboBox.addSelectionChangedListener(listener);

            comboBox.setEnabled(enabled);

            component = comboBox;
        }
        // Multiple selection case.
        else {

            // Selection model.
            final CheckBoxSelectionModel<QuestionChoiceElementDTO> sm = new CheckBoxSelectionModel<QuestionChoiceElementDTO>();
            sm.setSelectionMode(SelectionMode.MULTI);
            sm.addListener(Events.SelectionChange, listener);

            // Defines grid column model.
            final ColumnConfig labelColumn = new ColumnConfig();
            labelColumn.setId("label");
            labelColumn.setHeader(I18N.CONSTANTS.flexibleElementQuestionMutiple());
            labelColumn.setWidth(500);
            if (getCategoryTypeDTO() != null) {
                labelColumn.setRenderer(new GridCellRenderer<QuestionChoiceElementDTO>() {

                    @Override
                    public Object render(QuestionChoiceElementDTO model, String property, ColumnData config,
                            int rowIndex, int colIndex, ListStore<QuestionChoiceElementDTO> store,
                            Grid<QuestionChoiceElementDTO> grid) {

                        final com.google.gwt.user.client.ui.Grid panel = new com.google.gwt.user.client.ui.Grid(1, 2);
                        panel.setCellPadding(0);
                        panel.setCellSpacing(0);

                        panel.setWidget(0, 0, CategoryIconProvider.getIcon(model.getCategoryElementDTO()));
                        panel.setText(0, 1, (String) model.get(property));

                        DOM.setStyleAttribute(panel.getCellFormatter().getElement(0, 1), "paddingLeft", "5px");
                        DOM.setStyleAttribute(panel.getCellFormatter().getElement(0, 1), "paddingTop", "1px");

                        return panel;
                    }
                });
            }

            // Grid used as a list box.
            final FlexibleGrid<QuestionChoiceElementDTO> multipleQuestion = new FlexibleGrid<QuestionChoiceElementDTO>(
                    store, sm, sm.getColumn(), labelColumn);
            multipleQuestion.setAutoExpandColumn("label");
            multipleQuestion.setVisibleElementsCount(5);

            final ContentPanel cp = new ContentPanel();
            cp.setHeaderVisible(true);
            cp.setBorders(true);
            cp.setHeading(getLabel());
            cp.setTopComponent(null);
            cp.add(multipleQuestion);

            // Selects the already selected choices.
            if (valueResult != null && valueResult.isValueDefined()) {

                final List<Long> selectedChoicesId = ValueResultUtils.splitValuesAsLong(valueResult.getValueObject());
                final ArrayList<QuestionChoiceElementDTO> selectedChoices = new ArrayList<QuestionChoiceElementDTO>();

                for (Long id : selectedChoicesId) {
                    for (QuestionChoiceElementDTO choiceDTO : getChoicesDTO()) {
                        if (id == choiceDTO.getId()) {
                            selectedChoices.add(choiceDTO);
                        }
                    }
                }

                sm.select(selectedChoices, false);
            }

            multipleQuestion.getSelectionModel().setLocked(!enabled);

            component = cp;
        }

        // If the component is a category and/or a quality criterion.
        if (getCategoryTypeDTO() != null) {

            if (getQualityCriterionDTO() != null) {
                component.setToolTip(I18N.MESSAGES.flexibleElementQuestionCategory(getCategoryTypeDTO().getLabel())
                        + "<br/>" + I18N.MESSAGES.flexibleElementQuestionQuality(getQualityCriterionDTO().getInfo()));
            } else {
                component.setToolTip(I18N.MESSAGES.flexibleElementQuestionCategory(getCategoryTypeDTO().getLabel()));
            }

        } else if (getQualityCriterionDTO() != null) {
            component.setToolTip(I18N.MESSAGES.flexibleElementQuestionQuality(getQualityCriterionDTO().getInfo()));
        }

        return component;
    }

    @Override
    public boolean isCorrectRequiredValue(ValueResult result) {

        if (result == null || !result.isValueDefined()) {
            return false;
        }

        // Single selection case.
        if (!Boolean.TRUE.equals(getIsMultiple())) {
            try {
                final String value = (String) result.getValueObject();
                return value != null && !"".equals(value);
            } catch (ClassCastException e) {
                return false;
            }
        }
        // Multiple selection case.
        else {
            final List<Long> selectedChoicesId = ValueResultUtils.splitValuesAsLong(result.getValueObject());
            return selectedChoicesId != null && selectedChoicesId.size() > 0;
        }
    }

    /**
     * Basic selection changes listener implementation to fire value changes
     * events of the current flexible element.
     * 
     * @author tmi
     * 
     */
    private class ComboBoxSelectionListener extends SelectionChangedListener<QuestionChoiceElementDTO> {

        @Override
        public void selectionChanged(SelectionChangedEvent<QuestionChoiceElementDTO> se) {

            String value = null;
            final boolean isValueOn;

            // Single selection case.
            if (!Boolean.TRUE.equals(getIsMultiple())) {

                // Gets the selected choice.
                final QuestionChoiceElementDTO choice = se.getSelectedItem();

                // Checks if the choice isn't the default empty choice.
                isValueOn = choice != null && choice.getId() != -1;

                if (choice != null) {
                    value = String.valueOf(choice.getId());
                }
            }
            // Multiple selection case.
            else {

                // Gets the selected choices.
                final List<QuestionChoiceElementDTO> choices = se.getSelection();

                isValueOn = choices != null && choices.size() != 0;

                if (choices != null) {
                    value = ValueResultUtils.mergeValues(choices);
                }
            }

            if (value != null) {
                // Fires value change event.
                handlerManager.fireEvent(new ValueEvent(QuestionElementDTO.this, value));
            }

            // Required element ?
            if (getValidates()) {
                handlerManager.fireEvent(new RequiredValueEvent(isValueOn));
            }
        }

    }

    @Override
    public Object renderHistoryToken(HistoryTokenListDTO token) {

        final String value;

        // Single selection case.
        if (!Boolean.TRUE.equals(getIsMultiple())) {

            final QuestionChoiceElementDTO singleChoice = pickChoice(Integer.valueOf(token.getTokens().get(0)
                    .getValue()));

            if (singleChoice != null) {
                value = singleChoice.getLabel();
            } else {
                value = "";
            }

            return new HistoryTokenText(value);
        }
        // Multiple selection case.
        else {
            final List<Long> selectedChoicesId = ValueResultUtils
                    .splitValuesAsLong(token.getTokens().get(0).getValue());

            final ArrayList<String> labels = new ArrayList<String>();
            for (final Long id : selectedChoicesId) {
                final QuestionChoiceElementDTO choice = pickChoice(id.intValue());
                if (choice != null) {
                    labels.add(choice.getLabel());
                }
            }

            return new HistoryTokenText(labels);
        }
    }

    /**
     * Select a choice among the list of the choices.
     * 
     * @param id
     *            The wanted choice's id.
     * @return The choice if it exists, <code>null</code> otherwise.
     */
    private QuestionChoiceElementDTO pickChoice(int id) {

        if (getChoicesDTO() != null) {
            for (final QuestionChoiceElementDTO choice : getChoicesDTO()) {
                if (choice.getId() == id) {
                    return choice;
                }
            }
        }

        return null;
    }
}
