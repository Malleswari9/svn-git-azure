/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.dispatch.loader;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.SortInfo;
import org.sigmah.shared.command.GetListCommand;
import org.sigmah.shared.command.PagingGetCommand;

/**
 * Adapter that exposes {@link GetListCommand} and {@link org.sigmah.shared.command.PagingGetCommand}s as
 * standard GXT {@link com.extjs.gxt.ui.client.data.PagingLoadConfig}
 *
 * @author Alex Bertram 
 */
public class PagingGetCommandConfig extends BaseModelData implements PagingLoadConfig {

    private int offset;
    private int limit;
    private String sortField;
    private Style.SortDir sortDir;

    public PagingGetCommandConfig(GetListCommand cmd) {
        setSortInfo(cmd.getSortInfo());

        if (cmd instanceof PagingGetCommand) {
            this.offset = ((PagingGetCommand) cmd).getOffset();
            this.limit = ((PagingGetCommand) cmd).getLimit();
        }
    }


    @Override
    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int getLimit() {
        return this.limit;
    }

    @Override
    public int getOffset() {
        return this.offset;
    }

    @Override
    public Style.SortDir getSortDir() {
        return this.sortDir;
    }

    @Override
    public String getSortField() {
        return this.sortField;
    }

    @Override
    public SortInfo getSortInfo() {
        return new SortInfo(sortField, sortDir);
    }

    @Override
    public void setSortDir(Style.SortDir sortDir) {
        this.sortDir = sortDir;
    }

    @Override
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    @Override
    public void setSortInfo(SortInfo info) {
        this.sortField = info.getSortField();
        this.sortDir = info.getSortDir();
    }
}
