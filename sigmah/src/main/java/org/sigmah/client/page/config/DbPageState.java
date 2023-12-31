/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.config;

import org.sigmah.client.page.Frames;
import org.sigmah.client.page.PageId;
import org.sigmah.client.page.PageState;
import org.sigmah.client.page.PageStateParser;
import org.sigmah.client.page.common.grid.AbstractPagingGridPageState;

import java.util.Arrays;
import java.util.List;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.page.TabPage;
/*
 * @author Alex Bertram
 */

public class DbPageState extends AbstractPagingGridPageState implements TabPage {

    private PageId pageId;
    private int databaseId;

    protected DbPageState() {
    }

    public DbPageState(PageId pageId, int databaseId) {
        this.pageId = pageId;
        this.databaseId = databaseId;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public PageId getPageId() {
        return pageId;
    }

    public String serializeAsHistoryToken() {
        StringBuilder sb = new StringBuilder();
        sb.append(databaseId);
        appendGridStateToken(sb);
        return sb.toString();
    }

    public List<PageId> getEnclosingFrames() {
        return Arrays.asList(Frames.ConfigFrameSet, pageId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DbPageState dbPlace = (DbPageState) o;

        if (databaseId != dbPlace.databaseId) {
            return false;
        }
        if (pageId != dbPlace.pageId) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        return databaseId;
    }

    @Override
    public String getTabTitle() {
        return I18N.CONSTANTS.database();
    }

    public static class Parser implements PageStateParser {

        private PageId pageId;

        public Parser(PageId pageId) {
            this.pageId = pageId;
        }

        public PageState parse(String token) {
            return new DbPageState(pageId, Integer.parseInt(token));
        }
    }


}
