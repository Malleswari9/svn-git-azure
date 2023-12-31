/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import org.sigmah.client.dispatch.remote.Authentication;
import org.sigmah.client.ui.SigmahTheme;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * @author Raphaël Calabro (rcalabro@ideia.fr)
 */
public class SigmahEntryPoint implements EntryPoint {

    /**
	 * This is the entry point method.
	 */
    @Override
	public void onModuleLoad() {

        Log.info("Application: onModuleLoad starting");

        if(!GWT.isScript()) {
            Log.setCurrentLogLevel(Log.LOG_LEVEL_TRACE);
        }
        if(Log.isErrorEnabled()) {
            GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
                @Override
                public void onUncaughtException(Throwable e) {
                    Log.error("Uncaught exception", e);
                }
            });
        }

        GXT.setDefaultTheme(SigmahTheme.SIGMAH, true);

        Log.trace("Application: GXT theme set");
        
        // Initialization
        SigmahInjector injector = GWT.create(SigmahInjector.class);

        injector.getEventBus();
        injector.getHistoryManager();
        injector.getNavigationHandler();

        final Authentication authentication = injector.getAuthentication();

        if(authentication != null) {
            // Sigmah
            injector.registerProjectPageLoader();
            injector.registerDashboardPageLoader();
            injector.registerProjectPageLoader();
            injector.registerOrgUnitPageLoader();
            injector.registerAdminPageLoader();

            // ActivityInfo
            injector.registerDataEntryLoader();
            injector.registerMapLoader();
            injector.registerChartLoader();
            injector.registerConfigLoader();
            injector.registerPivotLoader();
            injector.registerReportLoader();
        }
        
        // Password reset
        injector.registerPasswordResetPageLoader();
      

        Log.info("Application: everyone plugged, firing Init event");

        injector.getEventBus().fireEvent(AppEvents.Init);
        //injector.getEventBus().fireEvent(new NavigationEvent(NavigationHandler.NavigationRequested, new ProjectListState()));
    }

}
