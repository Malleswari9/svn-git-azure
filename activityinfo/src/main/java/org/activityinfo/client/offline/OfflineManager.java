/**
 * Permits offline access to ActivityInfo with the help of Gears
 */
package org.activityinfo.client.offline;


import com.google.inject.Inject;
import org.activityinfo.client.EventBus;
import org.activityinfo.client.dispatch.Dispatcher;
import org.activityinfo.client.dispatch.remote.Authentication;
import org.activityinfo.client.offline.ui.OfflineStatusWindow;

/**
 * With regards to offline functionality, the application can be in one of three
 * states:
 * <p/>
 * <ol>
 * <li>Gears is not installed -- No possibility of going offline. The "Activate Offline Mode" button
 * should simply direct users to Google's Gears web site. See
 * {@link org.activityinfo.client.offline.ui.OfflineMenuDisabled}</li>
 * <p/>
 * <li>Gears is installed, but the user has not initiated offline mode. Just because gears is installed,
 * we can't assume the user is prepared to download their innermost secrets to the computer they happen
 * to be using. When the user clicks "Activate Offline Mode", we set the cookie to the magic value
 * "offline=enabled" and prompt the user to restart.
 * {@link org.activityinfo.client.offline.ui.OfflineMenuDisabled}</li>
 * <p/>
 * <li> Gears is installed, offline is enabled, everything is great! The workaday
 * {@link org.activityinfo.client.Application} class is replaced with
 * {@link org.activityinfo.client.offline.OfflineApplication} Present the user with options to manage
 * offline mode and check the update status.</li>
 * </ol>
 * <p/>
 * Note that this state is handled at COMPILE time and GWT will generate a set of permutations for each
 * of the three possibilities, substituting different implementations according to the rules in
 * Application.gwt.xml
 *
 * @author Alex Bertram
 */
public class OfflineManager {

    private final EventBus eventBus;
    private final Dispatcher service;
    private final Authentication auth;
    private final OfflineStatusWindow statusWindow;

    @Inject
    public OfflineManager(EventBus eventBus, Dispatcher service, Authentication auth,
                          OfflineStatusWindow statusWindow, DatabaseProvider databaseProvider) {
        this.eventBus = eventBus;
        this.service = service;
        this.auth = auth;
        this.statusWindow = statusWindow;


    }


}
