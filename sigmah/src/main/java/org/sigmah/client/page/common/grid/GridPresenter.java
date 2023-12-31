/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.common.grid;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.Record;
import org.sigmah.client.page.common.toolbar.ActionListener;

public interface GridPresenter<T extends ModelData> extends ActionListener {


    public void onSelectionChanged(T selectedItem);

    public int getPageSize();

    public void onDirtyFlagChanged(boolean isDirty);

    public boolean beforeEdit(Record record, String property);

}
