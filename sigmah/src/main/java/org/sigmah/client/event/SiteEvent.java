/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.event;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;
import org.sigmah.shared.dto.SiteDTO;

/**
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class SiteEvent extends BaseEvent {

    private int siteId;
    private SiteDTO site;
    
    public SiteEvent(EventType type, Object source, SiteDTO site) {
        super(type);
        this.setSource(source);
        this.site = site;
        this.siteId = site.getId();
    }

    public SiteEvent(EventType type, Object source, int siteId) {
        super(type);
        this.setSource(source);
        this.siteId = siteId;
    }

    public SiteDTO getSite() {
        return site;
    }

    public int getSiteId() {
        return siteId;
    }
}
