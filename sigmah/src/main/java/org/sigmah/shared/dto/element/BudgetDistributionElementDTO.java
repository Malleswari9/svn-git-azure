/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.dto.element;

import org.sigmah.shared.command.result.ValueResult;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Text;

/**
 * 
 * @author Denis Colliot (dcolliot@ideia.fr)
 * 
 */
public class BudgetDistributionElementDTO extends FlexibleElementDTO {

    private static final long serialVersionUID = 8520711106031085130L;

    @Override
    public String getEntityName() {
        // Gets the entity name mapped by the current DTO starting from the
        // "server.domain" package name.
        return "element.BudgetDistributionElement";
    }

    @Override
    protected Component getComponent(ValueResult valueResult, boolean enabled) {
        return new Text(getLabel());
    }

    @Override
    public boolean isCorrectRequiredValue(ValueResult result) {
        return true;
    }

}
