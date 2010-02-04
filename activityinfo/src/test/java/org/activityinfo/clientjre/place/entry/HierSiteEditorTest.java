package org.activityinfo.clientjre.place.entry;

import org.activityinfo.client.AppEvents;
import org.activityinfo.client.event.SiteEvent;
import org.activityinfo.client.mock.DummyData;
import org.activityinfo.client.mock.MockCommandService;
import org.activityinfo.client.mock.MockEventBus;
import org.activityinfo.client.page.entry.HierSiteEditor;
import org.activityinfo.client.mock.MockStateManager;
import org.activityinfo.server.util.DateUtilCalendarImpl;
import org.activityinfo.shared.command.GetAdminEntities;
import org.activityinfo.shared.command.result.AdminEntityResult;
import org.activityinfo.shared.dto.AdminEntityModel;
import org.activityinfo.shared.dto.Schema;
import org.activityinfo.shared.dto.SiteModel;
import static org.easymock.EasyMock.createNiceMock;
import org.junit.Test;
/*
 * @author Alex Bertram
 */

public class HierSiteEditorTest {

    @Test
    public void testOnSiteCreated() {

        // Test Data
        Schema schema = DummyData.PEARPlus();

        // Collaborator:
        MockEventBus eventBus = new MockEventBus();

        // Collaborator
        MockCommandService service = new MockCommandService();
        service.setResult(new GetAdminEntities(1, null, 11), DummyData.PEARPlus_Provinces());
        service.setResult(new GetAdminEntities(2, null, 11), DummyData.PEARPlus_ZS());
        service.setResult(new GetAdminEntities(3, null, 11), new AdminEntityResult());

        // Collaborator:
        HierSiteEditor.View view = createNiceMock(HierSiteEditor.View.class);

        // CLASS Under test
        HierSiteEditor editor = new HierSiteEditor(eventBus, service, new MockStateManager(),
                new DateUtilCalendarImpl(), schema.getActivityById(11), view);

        editor.getTreeStore().getLoader().load();

        // VERIFY that when a site is created, it is added to the store
        SiteModel newSite = new SiteModel(3);
        newSite.setActivityId(11);
        newSite.setAdminEntity(1, new AdminEntityModel(1, 1, "Ituri"));
        newSite.setAdminEntity(2, new AdminEntityModel(2, 11, 1, "Banana"));
        newSite.setAdminEntity(3, new AdminEntityModel(3, 113, 11, "Zengo"));

        eventBus.fireEvent(new SiteEvent(AppEvents.SiteCreated, this, newSite));

//        ModelData root = editor.getTreeStore().getRootItems().get(0);
//        Assert.assertEquals("root node is correct", "Ituri", root.get("name"));
//        Assert.assertEquals("ituri children are loaded", 2, editor.getTreeStore().getChildren(root).size());
//
    }

}
