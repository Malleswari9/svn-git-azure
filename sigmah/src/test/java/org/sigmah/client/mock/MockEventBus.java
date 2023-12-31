/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.mock;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.BaseObservable;
import com.extjs.gxt.ui.client.event.EventType;
import org.junit.Assert;
import org.sigmah.client.EventBus;
import org.sigmah.client.event.NavigationEvent;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class MockEventBus extends BaseObservable implements EventBus {

    public List<BaseEvent> eventLog = new ArrayList<BaseEvent>();

   
    
    @Override
    public boolean fireEvent(EventType eventType, BaseEvent be) {
        
        eventLog.add(be);

        return super.fireEvent(eventType, be);

    }

	@Override
	public boolean fireEvent(BaseEvent event) {
		return fireEvent(event.getType(), event);
	}

    public void assertEventFired(BaseEvent event) {
        Assert.assertTrue(eventLog.contains(event));
    }

    public int getEventCount(EventType type) {
        int count = 0;
        for(BaseEvent event : eventLog) {

            if(event.getType() == type) {
                count++;
            }
       }
        return count;
    }




    public <T> T getLastNavigationEvent(Class<T> placeClass) {
        for(int i=eventLog.size()-1; i>=0;i--) {
            BaseEvent event = eventLog.get(i);
            if(event instanceof NavigationEvent) {
                NavigationEvent nevent = (NavigationEvent)event;
                if(placeClass.isAssignableFrom(nevent.getPlace().getClass())) {
                    return (T)nevent.getPlace();
                }
            }
        }
        return null;
    }

    public void assertNotFired(EventType eventType) {
        assertTrue("eventType" + eventType.toString() + " has not been fired", getEventCount(eventType)==0);
    }
}
