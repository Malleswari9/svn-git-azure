/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.command;

import org.sigmah.shared.command.result.RenderResult;
import org.sigmah.shared.report.model.ReportElement;

/**
 *
 * Renders a {@link org.sigmah.shared.report.model.ReportElement} in the
 * specified format, saves the file to the server, and returns the name of the
 * temporary file that can be used to initiate a download.
 *
 * See also: {@link org.sigmah.server.endpoint.gwtrpc.DownloadServlet}
 *
 * @author Alex Bertram
 */
public class RenderElement implements Command<RenderResult> {

    public enum Format {
        PNG,
        Excel,
        Excel_Data,
        PowerPoint,
        PDF,
        Word
    }

    private Format format;
    private ReportElement element;

    public RenderElement() {
    }

    public RenderElement(ReportElement element, Format format) {
        this.element = element;
        this.format = format;
    }

    /**
     *
     * @return The format into which to render the element.
     */
    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    /**
     *
     * @return  The element to be rendered
     */
    public ReportElement getElement() {
        return element;
    }

    public void setElement(ReportElement element) {
        this.element = element;
    }
}
