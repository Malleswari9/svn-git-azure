/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

/**
 * Application icons
 */
package org.sigmah.client.icon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * Provides access to the application's icons through GWT's magic ImageBundle
 * generator.
 */
@SuppressWarnings("deprecation")
public interface IconImageBundle extends ImageBundle {
    IconImageBundle ICONS = (IconImageBundle) GWT.create(IconImageBundle.class);

    AbstractImagePrototype add();

    AbstractImagePrototype delete();

    AbstractImagePrototype editPage();

    AbstractImagePrototype save();

    AbstractImagePrototype database();

    AbstractImagePrototype design();

    AbstractImagePrototype addDatabase();

    AbstractImagePrototype editDatabase();

    AbstractImagePrototype excel();

    AbstractImagePrototype activity();

    AbstractImagePrototype addActivity();

    AbstractImagePrototype deleteActivity();

    @Resource(value = "editPage.png")
    AbstractImagePrototype editActivity();
    
    @Resource(value = "link_edit.png")
    AbstractImagePrototype editLinkedProject();

    AbstractImagePrototype user();

    AbstractImagePrototype editUser();

    AbstractImagePrototype addUser();

    AbstractImagePrototype deleteUser();

    /**
     * 
     * @return Icon for a user group
     */
    AbstractImagePrototype group();

    AbstractImagePrototype table();

    AbstractImagePrototype report();

    AbstractImagePrototype sum();

    AbstractImagePrototype curveChart();

    AbstractImagePrototype map();

    AbstractImagePrototype filter();

    @Resource(value = "key.png")
    AbstractImagePrototype login();

    AbstractImagePrototype cancel();

    AbstractImagePrototype barChart();

    @Resource(value = "barChart.png")
    AbstractImagePrototype analysis();

    @Resource(value = "keyboard.png")
    AbstractImagePrototype dataEntry();

    @Resource(value = "ruler.png")
    AbstractImagePrototype indicator();

    AbstractImagePrototype attributeGroup();

    AbstractImagePrototype attribute();

    AbstractImagePrototype refresh();

    @Resource(value = "wrench_orange.png")
    AbstractImagePrototype setup();

    AbstractImagePrototype mapped();

    AbstractImagePrototype unmapped();

    @Resource(value = "gs.png")
    AbstractImagePrototype graduatedSymbol();

    AbstractImagePrototype ppt();

    AbstractImagePrototype image();

    AbstractImagePrototype msword();

    AbstractImagePrototype pdf();

    AbstractImagePrototype pieChart();

    AbstractImagePrototype checked();

    AbstractImagePrototype unchecked();

    AbstractImagePrototype offline();

    AbstractImagePrototype onlineSynced();

    AbstractImagePrototype onlineSyncing();

    AbstractImagePrototype up();

    AbstractImagePrototype down();

    AbstractImagePrototype attach();

    AbstractImagePrototype remove();

    @Resource(value = "bullet_green.png")
    AbstractImagePrototype elementCompleted();

    @Resource(value = "bullet_red.png")
    AbstractImagePrototype elementUncompleted();

    @Resource(value = "bullet_star_new.png")
    AbstractImagePrototype activate();

    @Resource(value = "bullet_star_black.png")
    AbstractImagePrototype close();

    @Resource(value = "cog.png")
    AbstractImagePrototype create();

    @Resource(value = "page_edit.png")
    AbstractImagePrototype rename();

    @Resource(value = "link.png")
    AbstractImagePrototype select();

    @Resource(value = "information.png")
    AbstractImagePrototype info();

    AbstractImagePrototype expand();

    AbstractImagePrototype collapse();

    @Resource(value = "filter-check.png")
    AbstractImagePrototype checkboxChecked();

    @Resource(value = "filter-uncheck.png")
    AbstractImagePrototype checkboxUnchecked();

    @Resource(value = "hourglass.png")
    AbstractImagePrototype history();

    @Resource(value = "points2.png")
    AbstractImagePrototype openedPoint();

    @Resource(value = "points3.png")
    AbstractImagePrototype overduePoint();

    @Resource(value = "points1.png")
    AbstractImagePrototype closedPoint();
    
    @Resource(value = "rappels2.png")
    AbstractImagePrototype openedReminder();

    @Resource(value = "rappels3.png")
    AbstractImagePrototype overdueReminder();

    @Resource(value = "rappels1.png")
    AbstractImagePrototype closedReminder();
    
    @Resource(value = "delete_icon.png")
    AbstractImagePrototype deleteIcon();
    
    @Resource(value = "control_fastforward.png")
    AbstractImagePrototype forward();
    
    @Resource(value = "control_rewind.png")
    AbstractImagePrototype back();
    
    AbstractImagePrototype ods();
    
    AbstractImagePrototype csv();
}
