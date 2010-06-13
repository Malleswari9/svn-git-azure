package org.activityinfo.client.page.config;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.client.dispatch.Dispatcher;
import org.activityinfo.client.dispatch.callback.Got;
import org.activityinfo.client.inject.AppInjector;
import org.activityinfo.client.page.*;
import org.activityinfo.client.page.common.nav.NavigationPanel;
import org.activityinfo.client.page.common.widget.VSplitFrameSet;
import org.activityinfo.client.page.config.design.Designer;
import org.activityinfo.shared.command.GetSchema;
import org.activityinfo.shared.dto.SchemaDTO;
import org.activityinfo.shared.dto.UserDatabaseDTO;

public class ConfigLoader implements PageLoader {

    private final AppInjector injector;
    private final Dispatcher service;

    @Inject
    public ConfigLoader(AppInjector injector, NavigationHandler pageManager, PageStateSerializer placeSerializer) {
        this.injector = injector;
        this.service = injector.getService();

        pageManager.registerPageLoader(Frames.ConfigFrameSet, this);
        pageManager.registerPageLoader(AccountEditor.Account, this);
        pageManager.registerPageLoader(DbConfigPresenter.DatabaseConfig, this);
        pageManager.registerPageLoader(DbListPresenter.DatabaseList, this);
        pageManager.registerPageLoader(DbUserEditor.DatabaseUsers, this);
        pageManager.registerPageLoader(DbPartnerEditor.DatabasePartners, this);
        pageManager.registerPageLoader(Designer.Design, this);

        placeSerializer.registerStatelessPlace(AccountEditor.Account, new AccountPageState());
        placeSerializer.registerStatelessPlace(DbListPresenter.DatabaseList, new DbListPageState());
        placeSerializer.registerParser(DbConfigPresenter.DatabaseConfig, new DbPageState.Parser(DbConfigPresenter.DatabaseConfig));
        placeSerializer.registerParser(DbUserEditor.DatabaseUsers, new DbPageState.Parser(DbUserEditor.DatabaseUsers));
        placeSerializer.registerParser(DbPartnerEditor.DatabasePartners, new DbPageState.Parser(DbPartnerEditor.DatabasePartners));
        placeSerializer.registerParser(Designer.Design, new DbPageState.Parser(Designer.Design));
    }

    @Override
    public void load(final PageId pageId, final PageState pageState, final AsyncCallback<Page> callback) {


        GWT.runAsync(new RunAsyncCallback() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess() {

                if (Frames.ConfigFrameSet.equals(pageId)) {
                    NavigationPanel navPanel = new NavigationPanel(injector.getEventBus(),
                            injector.getConfigNavigator());
                    VSplitFrameSet frameSet = new VSplitFrameSet(pageId, navPanel);
                    callback.onSuccess(frameSet);

                } else if (AccountEditor.Account.equals(pageId)) {
                    callback.onSuccess(injector.getAccountEditor());

                } else if (DbListPresenter.DatabaseList.equals(pageId)) {
                    callback.onSuccess(injector.getDbListPage());

                } else if (pageState instanceof DbPageState) {
                    final DbPageState dPlace = (DbPageState) pageState;

                    /// the schema needs to be loaded before we can continue
                    service.execute(new GetSchema(), null, new Got<SchemaDTO>() {

                        @Override
                        public void got(SchemaDTO schema) {

                            UserDatabaseDTO db = schema.getDatabaseById(dPlace.getDatabaseId());

                            if (DbConfigPresenter.DatabaseConfig.equals(pageId)) {
                                DbConfigPresenter presenter = injector.getDbConfigPresenter();
                                presenter.go(db);
                                callback.onSuccess(presenter);

                            } else if (Designer.Design.equals(pageId)) {
                                Designer presenter = injector.getDesigner();
                                presenter.go(db);
                                callback.onSuccess(presenter);

                            } else if (DbUserEditor.DatabaseUsers.equals(pageId)) {
                                DbUserEditor editor = injector.getDbUserEditor();
                                editor.go(db, dPlace);
                                callback.onSuccess(editor);

                            } else if (DbPartnerEditor.DatabasePartners.equals(pageId)) {
                                DbPartnerEditor presenter = injector.getDbPartnerEditor();
                                presenter.go(db);
                                callback.onSuccess(presenter);
//                            } else if(Pages.DatabaseTargets.equals(pageId)) {
//                                TargetEditor editor = injector.getTargetEditor();
//                                editor.go(db);
//                                callback.onSuccess(editor);
                            } else {
                                callback.onFailure(new Exception("ConfigLoader didn't know how to handle " + pageState.toString()));
                            }
                        }
                    });
                } else {
                    callback.onFailure(new Exception("ConfigLoader didn't know how to handle " + pageState.toString()));
                }
            }
        });

    }
}
