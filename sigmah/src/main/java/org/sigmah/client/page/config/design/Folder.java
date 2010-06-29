/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.config.design;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.sigmah.shared.dto.ActivityDTO;

public class Folder extends BaseModelData {

    private ActivityDTO activity;

    public Folder(ActivityDTO activity, String name) {
        this.activity = activity;
        set("name", name);
    }

    public ActivityDTO getActivity() {
        return activity;
    }
}
