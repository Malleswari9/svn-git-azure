/*
 * All Sigmah code is released under the GNU General Public License v3 See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;

import org.sigmah.client.EventBus;
import org.sigmah.client.dispatch.AsyncMonitor;
import org.sigmah.client.event.NavigationEvent;
import org.sigmah.client.event.NavigationEvent.NavigationError;
import org.sigmah.client.inject.Root;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Coordinates navigation between pages. PageManager listens for NavigationEvents, fired either by an individual
 * component, or from the HistoryManager, and
 */
@Singleton
public class NavigationHandler {

    private final EventBus eventBus;
    private final Frame root;
    private final Map<PageId, PageLoader> pageLoaders = new HashMap<PageId, PageLoader>();

    public static final EventType NavigationRequested = new EventBus.NamedEventType("NavigationRequested");
    public static final EventType NavigationAgreed = new EventBus.NamedEventType("NavigationAgreed");

    private NavigationAttempt activeNavigation;

    @Inject
    public NavigationHandler(final EventBus eventBus, final @Root Frame root) {
        this.eventBus = eventBus;
        this.root = root;

        eventBus.addListener(NavigationRequested, new Listener<NavigationEvent>() {

            @Override
            public void handleEvent(NavigationEvent be) {
                NavigationAttempt oldNavigation = activeNavigation;
                onNavigationRequested(be);
                be.setNavigationError(activeNavigation.getNavigationError());
                if (activeNavigation.getNavigationError() != NavigationError.NONE) {
                    activeNavigation = oldNavigation;
                }
            }
        });

        Log.debug("PageManager: connected to EventBus and listening.");
    }

    private void onNavigationRequested(NavigationEvent be) {
        if (activeNavigation == null || !activeNavigation.getPlace().equals(be.getPlace())) {
            activeNavigation = new NavigationAttempt(be.getPlace(), be, this);
            activeNavigation.go();
        }
    }

    public NavigationAttempt getActiveNavigation() {
        return activeNavigation;
    }

    public void setActiveNavigation(NavigationAttempt activeNavigation) {
        this.activeNavigation = activeNavigation;
    }

    public void registerPageLoader(PageId pageId, PageLoader loader) {
        pageLoaders.put(pageId, loader);
        Log.debug("PageManager: Registered loader for pageId '" + pageId + "'");
    }

    private PageLoader getPageLoader(PageId pageId) {
        // Looks for an ID separator
        int index = pageId.toString().indexOf('!');
        if (index != -1)
            // Removes the ID from the PageId in order to retrieves the correct
            // PageLoader from the map.
            pageId = new PageId(pageId.toString().substring(0, index));

        PageLoader loader = pageLoaders.get(pageId);
        if (loader == null) {
            Log.debug("PageManager: no loader for " + pageId);
            throw new Error("PageManager: no loader for " + pageId);
        }
        return loader;
    }

    /**
     * Encapsulates a single navigation attempt.
     */
    public class NavigationAttempt {

        private final PageState place;
        private final NavigationEvent event;
        private NavigationHandler handler;

        private Iterator<PageId> pageHierarchyIt;
        private Frame frame;
        private Page currentPage;
        private PageId targetPage;
        private AsyncMonitor loadingPlaceHolder;
        private NavigationError navigationError;

        public NavigationAttempt(PageState place, NavigationEvent event, NavigationHandler handler) {
            this.place = place;
            this.event = event;
            this.handler = handler;
        }

        public PageState getPlace() {
            return place;
        }

        public NavigationError getNavigationError() {
            if (navigationError == null)
                return NavigationError.EXECUTION_ERROR;
            return navigationError;
        }

        public void setNavigationError(NavigationError navigationError) {
            this.navigationError = navigationError;
        }

        public void go() {
            startAtRoot();
            confirmPageChange();
        }

        private void startAtRoot() {
            assertViewPathIsNotEmpty();
            pageHierarchyIt = place.getEnclosingFrames().iterator();
            currentPage = root;
            descend();
        }

        private void descend() {
            assertPageIsFrame(currentPage);
            frame = (Frame) currentPage;
            targetPage = pageHierarchyIt.next();
            currentPage = frame.getActivePage();
        }

        /**
         * After each asynchronous call we need to check that the user has not requested to navigate elsewhere. For
         * example, a page loader may make an asynchronous call, which means that an additional JavaScript fragment has
         * to be downloaded from the server and parsed before we can continue. During that time, the user may have grown
         * tired of waiting and hit the back button or chosen another place to go to.
         */
        private boolean isStillActive() {
            return this == activeNavigation;
        }

        protected void confirmPageChange() {
            if (thereIsNoCurrentPage()) {
                proceedWithNavigation();

            } else if (targetPageIsAlreadyActive()) {
                // ok, no change required.
                // descend if necessary

                if (hasChildPage()) {
                    descend();
                    confirmPageChange();
                } else {
                    askPermissionToChange();
                }
            } else {
                askPermissionToChange();
            }
        }

        /**
         * We need to give the current page an opportunity to cancel the navigation. For example, the user may have made
         * changes to the page and we don't want to navigate away until we're sure that they can be saved.
         */
        private void askPermissionToChange() {
            currentPage.requestToNavigateAway(place, new NavigationCallback() {

                @Override
                public void onDecided(NavigationError error) {
                    switch (error) {
                        case WORK_NOT_SAVED:
                        case EXECUTION_ERROR:
                            navigationError = error;
                            Log.debug("Navigation to '"
                                + place.toString()
                                + "' refused by "
                                + currentPage.toString()
                                + " ("
                                + error.toString()
                                + ")");
                            break;
                        case NONE:
                            if (isStillActive() || event.getNavigationError() == NavigationError.WORK_NOT_SAVED) {
                                proceedWithNavigation();
                                navigationError = NavigationError.NONE;
                                handler.setActiveNavigation(NavigationAttempt.this);
                            } else {
                                navigationError = NavigationError.EXECUTION_ERROR;
                            }
                            break;
                        default:
                            Log.debug("Navigation error \"" + error.toString() + "\" not yet implemented.");
                    }
                }
            });
        }

        private boolean hasChildPage() {
            return pageHierarchyIt.hasNext();
        }

        private boolean targetPageIsAlreadyActive() {
            return currentPage.getPageId().equals(targetPage);
        }

        private boolean thereIsNoCurrentPage() {
            return currentPage == null;
        }

        private void proceedWithNavigation() {
            navigationError = NavigationError.NONE;
            fireAgreedEvent();
            startAtRoot();
            changePage();
        }

        private void fireAgreedEvent() {
            NavigationEvent navEvent = new NavigationEvent(NavigationAgreed, place, event, event.getParentObject());
            navEvent.setNavigationError(NavigationError.NONE);
            eventBus.fireEvent(navEvent);
        }

        protected void changePage() {

            refreshMessage();

            /*
             * First see if this view is already the active view, in which case we can just descend in the path
             */
            if (!thereIsNoCurrentPage() && targetPageIsAlreadyActive() && currentPage.navigate(place)) {

                changeChildPageIfNecessary();

            } else {
                shutDownCurrentPageIfThereIsOne();
                showPlaceHolder();
                schedulePageLoadAfterEventProcessing();
            }
        }

        private void refreshMessage() {
            final String id = "alert";
            if (RootPanel.get(id) != null) {
                Dictionary d = null;
                try {
                    d = Dictionary.getDictionary("AlertMessage");
                } catch (MissingResourceException e) {
                    d = null;
                }
                RootPanel.get(id).clear();
                if (d != null && Boolean.valueOf(d.get("active"))) {
                    RootPanel.get(id).setVisible(true);
                    final HTML html = new HTML(d.get("message"));
                    html.addStyleName("message");
                    RootPanel.get(id).add(html);
                } else {
                    RootPanel.get(id).setVisible(false);
                }
            }
        }

        private void shutDownCurrentPageIfThereIsOne() {
            if (!thereIsNoCurrentPage()) {
                currentPage.shutdown();
            }
        }

        private void showPlaceHolder() {
            loadingPlaceHolder = frame.showLoadingPlaceHolder(targetPage, place);
        }

        /**
         * Schedules the loadPage() after all UI events in the browser have had a chance to run. This assures that the
         * loading placeholder has a chance to be added to the page.
         */
        private void schedulePageLoadAfterEventProcessing() {
            if (GWT.isClient()) {
                DeferredCommand.addCommand(new Command() {

                    @Override
                    public void execute() {
                        if (isStillActive()) {
                            loadPage();
                        }
                    }
                });
            } else {
                loadPage();
            }
        }

        /**
         * Delegates the creation of the Page component to a registered page loader.
         */
        private void loadPage() {
            PageLoader loader = getPageLoader(targetPage);
            loader.load(targetPage, place, new AsyncCallback<Page>() {

                @Override
                public void onFailure(Throwable caught) {
                    onPageFailedToLoad(caught);
                }

                @Override
                public void onSuccess(Page page) {
                    if (isStillActive()) {
                        onPageLoaded(page);
                    }
                }
            });
        }

        private void onPageFailedToLoad(Throwable caught) {
            loadingPlaceHolder.onConnectionProblem();
            Log.error("PageManager: could not load page " + targetPage, caught);
        }

        private void onPageLoaded(Page page) {
            makeCurrent(page);
            changeChildPageIfNecessary();
        }

        private void makeCurrent(Page page) {
            frame.setActivePage(page);
            currentPage = page;
        }

        private void changeChildPageIfNecessary() {
            if (hasChildPage()) {
                descend();
                changePage();
            }
        }

        private void assertViewPathIsNotEmpty() {
            assert place.getEnclosingFrames().size() != 0 : "PageState " + place.toString() + " has an empty viewPath!";
        }

        private void assertPageIsFrame(Page page) {
            assert page instanceof Frame : "Cannot load page "
                + pageHierarchyIt.next()
                + " into "
                + page.toString()
                + " because "
                + page.getClass().getName()
                + " does not implement the PageFrame interface.";
        }
    }
}
