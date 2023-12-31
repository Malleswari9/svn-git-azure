/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.report.renderer.html;

import org.sigmah.server.report.util.HtmlWriter;
import org.sigmah.shared.report.content.FilterDescription;

import java.util.List;

public class HtmlReportUtil {
	
	public static void generateFilterDescriptionHtml(HtmlWriter html, List<FilterDescription> descs) {
		
		for(FilterDescription desc : descs) {
			html.div()
				.styleName(CssStyles.FILTER_DESCRIPTION)
				.styleName(CssStyles.DIMENSION(desc.getDimensionType()))
				.text( desc.getLabels(), ", ");
		}
	}
}
