/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.command;

import org.sigmah.shared.report.content.Content;
import org.sigmah.shared.report.model.ReportElement;

/**
 * Generates and returns to the client the content of an element.
 *
 * Returns: {@link org.sigmah.shared.report.content.Content}
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class GenerateElement<T extends Content> implements Command<T> {

    private ReportElement element;

    protected GenerateElement() {
    }

    public GenerateElement(ReportElement element) {
        this.element = element;
    }

    public ReportElement getElement() {
        return element;
    }

    public void setElement(ReportElement element) {
        this.element = element;
    }
}
