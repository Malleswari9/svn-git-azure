package org.activityinfo.client.common.filter;

import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;

import org.activityinfo.client.command.CommandService;
import org.activityinfo.shared.dto.AdminEntityModel;
import org.activityinfo.shared.dto.AdminLevelModel;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
/*
 * @author Alex Bertram
 */

public class AdminTreeLoader extends BaseTreeLoader<AdminEntityModel> {

    private List<AdminLevelModel> hierarchy = new ArrayList<AdminLevelModel>();

    public AdminTreeLoader(CommandService service) {
        super(new AdminTreeProxy(service, Collections.<AdminLevelModel>emptyList()));

    }

    public void setHierarchy(List<AdminLevelModel> hierarchy) {
        this.hierarchy = hierarchy;
        ((AdminTreeProxy)this.proxy).setHierarchy(hierarchy);
    }

    @Override
    public boolean hasChildren(AdminEntityModel parent) {
        if(hierarchy.size()<=1)
            return false;

        return this.hierarchy.get(hierarchy.size()-1).getId() != parent.getLevelId();
    }
}
