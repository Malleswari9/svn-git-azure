/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.charts;

import com.extjs.gxt.charts.client.Chart;
import com.extjs.gxt.charts.client.model.ChartModel;
import com.extjs.gxt.charts.client.model.axis.XAxis;
import com.extjs.gxt.charts.client.model.axis.YAxis;
import com.extjs.gxt.charts.client.model.charts.FilledBarChart;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import org.sigmah.client.i18n.I18N;
import org.sigmah.shared.report.content.PivotChartContent;
import org.sigmah.shared.report.content.PivotTableData;

import java.util.List;

/**
 * A view of the PivotChartElement using Open Flash Charts
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class ChartOFCView extends ContentPanel implements ChartView {

    private Chart chart;
    private ChartModel chartModel;

    public ChartOFCView() {
        setHeaderVisible(false);
        setLayout(new FitLayout());
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);

        if(chartModel != null) {
            createChart();
        }
    }

    private void createChart() {
        chart = new Chart(GWT.getModuleBaseURL() + "/gxt211/chart/open-flash-chart.swf");
        chart.setBorders(false);
        chart.setChartModel(chartModel);

        if(!chart.isLoaded()) {
            this.el().mask(I18N.CONSTANTS.loading());
            chart.addListener(Events.Ready, new Listener<BaseEvent>() {
                public void handleEvent(BaseEvent be) {
                    chart.removeListener(Events.Ready, this);
                    ChartOFCView.this.el().unmask();
                }
            });
        }
        add(chart, new MarginData(20));
    }

    private void setModel(ChartModel chartModel) {
        this.chartModel = chartModel;

        if(this.isRendered() && chart == null) {
            createChart();
            layout();
        } else {
            chart.setChartModel(chartModel);
        }       
    }

    /**
     * Updates the view to the given PivotChartContent
     *
     * @param content
     */
    public void setContent(PivotChartContent content) {
        ChartModel cm = new ChartModel();
        cm.setBackgroundColour("#fffff");
        cm.setNumDecimals(0);

        PivotTableData table = content.getData();

        List<PivotTableData.Axis> categories = table.getRootRow().getLeaves();
        List<PivotTableData.Axis> series = table.getRootColumn().getLeaves();

        cm.setDecimalSeparatorComma(true);
        XAxis xa = new XAxis();
        xa.setLabels(PivotTableData.flattenLabels(categories));
        xa.getLabels().setColour("009900");
        xa.setGridColour("eeffee");
        xa.setColour("009900");
        cm.setXAxis(xa);

        YAxis ya = new YAxis();
        double maxValue = table.getRootRow().getMaxValue();
        ya.setRange(0, maxValue);
        ya.setSteps(maxValue / 10);
        ya.setGridColour("eeffee");
        ya.setColour("009900");
        cm.setYAxisLabelStyle(10, "009900");
        cm.setYAxis(ya);

        FilledBarChart bchart = new FilledBarChart("6666ff", "000066");
        bchart.setTooltip("#val#");
        for (PivotTableData.Axis category : categories) {

            PivotTableData.Cell cell = category.getCell(series.get(0));
            bchart.addValues(cell == null ? 0 : cell.getValue());
        }
        cm.addChartConfig(bchart);

        setModel(cm);
    }
}
