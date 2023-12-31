/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.common.filter;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.form.DateField;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.icon.IconImageBundle;
import org.sigmah.shared.dao.Filter;

import java.util.Date;

/**
 * UI Component for selecting a range of dates to be used with
 * a {@link org.sigmah.shared.dao.Filter}
 *
 * @author Alex Bertram
 */
public class DateRangePanel extends ContentPanel {
    private DateField date1;
    private DateField date2;

    public DateRangePanel() {
        super();

        setHeading(I18N.CONSTANTS.filterByDate());
        setIcon(IconImageBundle.ICONS.filter());
        setBodyStyle("padding:5px");

        add(new Html(I18N.CONSTANTS.fromDate()));

        date1 = new DateField();
        add(date1);

        add(new Html(I18N.CONSTANTS.toDate()));

        date2 = new DateField();
        add(date2);
    }

    public Date getMinDate() {
        return date1.getValue();
    }

    public Date getMaxDate() {
        return date2.getValue();
    }

    /**
     * Updates the given filter with the user's choice.
     *
     * @param filter  the filter to update
     */
    public void updateFilter(Filter filter) {
        filter.setMinDate(date1.getValue());
        filter.setMaxDate(date2.getValue());
    }
}
