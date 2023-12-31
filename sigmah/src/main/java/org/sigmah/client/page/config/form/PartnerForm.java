/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.config.form;

import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.GWT;
import org.sigmah.client.i18n.UIConstants;

public class PartnerForm extends FormPanel {

    private FormBinding binding;

	public PartnerForm() {
		super();

        binding = new FormBinding(this);
		
		UIConstants constants = GWT.create(UIConstants.class);
		
		TextField<String> nameField = new TextField<String>();
		nameField.setFieldLabel(constants.name());
		nameField.setMaxLength(16);
		nameField.setAllowBlank(false);
        binding.addFieldBinding(new FieldBinding(nameField, "name"));
		this.add(nameField);
		
		TextField<String> fullField = new TextField<String> ();
		fullField.setFieldLabel(constants.fullName());
		fullField.setMaxLength(64);
        binding.addFieldBinding(new FieldBinding(fullField, "fullName"));
		this.add(fullField);
		
		CheckBox operationalField = new CheckBox();
		operationalField.setName("operational");
		operationalField.setFieldLabel(constants.operational());
		this.add(operationalField);


	}

    public FormBinding getBinding() {
        return binding;
    }
}
