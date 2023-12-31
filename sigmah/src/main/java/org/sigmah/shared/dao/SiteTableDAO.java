/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.dao;

import org.sigmah.shared.domain.User;

import java.util.List;

/**       
 * 
 * Data Access Object for projections based on the {@link org.sigmah.shared.domain.Site Site} domain object.
 * 
 * Information associated with Sites is stored across several entities, including
 * {@link org.sigmah.shared.domain.Location Location},
 * {@link org.sigmah.shared.domain.OrgUnit},
 * {@link org.sigmah.shared.domain.AttributeValue},
 * {@link org.sigmah.shared.domain.ReportingPeriod ReportingPeriod}, and
 * {@link org.sigmah.shared.domain.IndicatorValue}, but often we need this information in
 * a table format with all the different data in columns, and this class does the heavy lifting.
 *
 * 
 * @author Alex Bertram
 */
public interface SiteTableDAO {

    int RETRIEVE_ALL = 0xFF;
    int RETRIEVE_NONE = 0x00;
    int RETRIEVE_ADMIN = 0x01;
    int RETRIEVE_INDICATORS = 0x02;
    int RETRIEVE_ATTRIBS = 0x04;


    <RowT> List<RowT> query(
            User user,
            Filter filter,
            List<SiteOrder> orderings,
            SiteProjectionBinder<RowT> binder,
            int retrieve,
            int offset,
            int limit);

    int queryCount(User user, Filter filter);

    int queryPageNumber(User user, Filter filter, List<SiteOrder> orderings, int pageSize, int siteId);
}
