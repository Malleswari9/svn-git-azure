/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.report.renderer.html;

import com.google.inject.Inject;
import org.sigmah.server.report.util.HtmlWriter;
import org.sigmah.shared.report.model.*;

import java.io.IOException;

public class HtmlReportRenderer implements HtmlRenderer<Report> {

    private final HtmlPivotTableRenderer pivotTableRenderer;
    private final HtmlChartRenderer pivotChartRenderer;
    private final HtmlTableRenderer tableRenderer;
    private final HtmlMapRenderer mapRenderer;
    private final HtmlStaticRenderer staticRenderer;

    @Inject
    public HtmlReportRenderer(HtmlPivotTableRenderer pivotTableRenderer, HtmlChartRenderer pivotChartRenderer,
                              HtmlTableRenderer tableRenderer, HtmlMapRenderer mapRenderer, HtmlStaticRenderer staticRenderer) {
        this.pivotTableRenderer = pivotTableRenderer;
        this.pivotChartRenderer = pivotChartRenderer;
        this.tableRenderer = tableRenderer;
        this.mapRenderer = mapRenderer;
        this.staticRenderer = staticRenderer;
    }

    @Override
	public void render(HtmlWriter html, ImageStorageProvider isp, Report report) throws IOException {
		
		
		html.startDiv().styleName(CssStyles.REPORT_CONTAINER);
			
		/*
		 * Generate the title of the report
		 */
	
		html.startHeader(1).styleName(CssStyles.REPORT_TITLE);
		html.text(report.getTitle());
		html.close();

		
		/*
		 * View the filter restrictions as sub titles
		 */
		
		HtmlReportUtil.generateFilterDescriptionHtml(html,
				report.getContent().getFilterDescriptions());
		
		
		/* 
		 * Generate the body elements of the report 
		 */
		
		html.startDiv().styleName("r-body");
		
		for(ReportElement element : report.getElements()) {

			if(element instanceof PivotTableElement) {
				
				pivotTableRenderer.render(html, isp, (PivotTableElement) element);
							
			} else if(element instanceof PivotChartElement) {
				
				pivotChartRenderer.render(html, isp, (PivotChartElement) element);
			
			} else if(element instanceof TableElement) {
				
				tableRenderer.render(html, isp, (TableElement) element);
			
			} else if(element instanceof MapElement) {

                mapRenderer.render(html, isp, (MapElement) element);
                
            } else if(element instanceof StaticElement) {

                staticRenderer.render(html, isp, (StaticElement) element);
            }

		}
		html.close();
	}

}
