/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.command.result;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * Result of commands which create a new entity.
 * 
 * @see org.sigmah.shared.command.CreateEntity
 * @see org.sigmah.shared.command.CreateReportDef
 * 
 * @author Alex Bertram
 */
public class CreateResult implements CommandResult {

    private static final long serialVersionUID = -2196195672020302549L;

    protected int newId;

    protected BaseModelData entity;

    protected CreateResult() {

    }

    public CreateResult(int newId) {
        this.newId = newId;
    }

    public CreateResult(BaseModelData entity) {
        this.entity = entity;
    }

    /**
     * Gets the primary key of the new entity.
     * 
     * @return the primary key of the new entity that was generated by the
     *         server.
     */
    public int getNewId() {
        return newId;
    }

    public void setNewId(int newId) {
        this.newId = newId;
    }

    public void setEntity(BaseModelData entity) {
        this.entity = entity;
    }

    public BaseModelData getEntity() {
        return entity;
    }

}
