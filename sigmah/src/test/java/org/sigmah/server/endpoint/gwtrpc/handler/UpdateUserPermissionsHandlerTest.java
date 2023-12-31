/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.endpoint.gwtrpc.handler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.sigmah.MockDb;
import org.sigmah.server.dao.PartnerDAO;
import org.sigmah.server.mail.Invitation;
import org.sigmah.server.mail.Mailer;
import org.sigmah.shared.command.UpdateUserPermissions;
import org.sigmah.shared.dao.UserDAO;
import org.sigmah.shared.dao.DAO;
import org.sigmah.shared.dao.UserDatabaseDAO;
import org.sigmah.shared.dao.UserPermissionDAO;
import org.sigmah.shared.domain.OrgUnit;
import org.sigmah.shared.domain.User;
import org.sigmah.shared.domain.UserDatabase;
import org.sigmah.shared.domain.UserPermission;
import org.sigmah.shared.dto.PartnerDTO;
import org.sigmah.shared.dto.UserPermissionDTO;
import org.sigmah.shared.exception.IllegalAccessCommandException;
import org.sigmah.shared.dao.DAO;

/**
 * @author Alex Bertram
 */
public class UpdateUserPermissionsHandlerTest {

    private OrgUnit NRC;
    private OrgUnit IRC;
    private PartnerDTO NRC_DTO;

    private MockDb db = new MockDb();
    protected Mailer<Invitation> mailer;
    protected UpdateUserPermissionsHandler handler;
    protected User owner;

    @Before
    public void setup() {

        NRC = new OrgUnit();
        NRC.setId(1);
        NRC.setName("NRC");
        NRC.setFullName("Norwegian Refugee Council");
        db.persist(NRC);

        IRC = new OrgUnit();
        IRC.setId(2);
        IRC.setName("IRC");
        IRC.setFullName("International Rescue Committee");
        db.persist(IRC);

        NRC_DTO = new PartnerDTO(1, "NRC");

        mailer = createMock("InvitationMailer", Mailer.class);

        handler = new UpdateUserPermissionsHandler(
                db.getDAO(UserDatabaseDAO.class), db.getDAO(PartnerDAO.class), db.getDAO(UserDAO.class),
                db.getDAO(UserPermissionDAO.class), mailer);


        owner = new User();
        owner.setId(99);
        owner.setName("Alex");
        db.persist(owner);

        UserDatabase udb = new UserDatabase(1, "PEAR");
        udb.setOwner(owner);
        db.persist(udb);

    }

    @Test
    public void ownerCanAddUser() throws Exception {

        mailer.send(isA(Invitation.class), isA(Locale.class));
        replay(mailer);

        UserPermissionDTO user = new UserPermissionDTO();
        user.setEmail("other@foobar");
        user.setPartner(NRC_DTO);
        user.setAllowView(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, user);

        handler.execute(cmd, owner);

        verify(mailer);
    }


    /**
     * Asserts that someone with ManageUsersPermission will
     * be permitted to grant some one edit rights.
     */
    @Test
    public void testVerifyAuthorityForViewPermissions() throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.setPartner(NRC);
        executingUserPermissions.setAllowManageUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPartner(NRC_DTO);
        dto.setAllowView(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }

    /**
     * Asserts that someone with ManageUsersPermission will
     * be permitted to grant some one edit rights.
     */
    @Test
    public void testVerifyAuthorityForEditPermissions() throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.setPartner(NRC);
        executingUserPermissions.setAllowManageUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPartner(NRC_DTO);
        dto.setAllowView(true);
        dto.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }

    @Test(expected = IllegalAccessCommandException.class)
    public void testFailingVerifyAuthorityForView() throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.setPartner(IRC);
        executingUserPermissions.setAllowManageUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPartner(NRC_DTO);
        dto.setAllowView(true);
        dto.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }

    @Test
    public void testVerifyAuthorityForViewByOtherPartner() throws IllegalAccessCommandException {

        UserPermission executingUserPermissions = new UserPermission();
        executingUserPermissions.setPartner(IRC);
        executingUserPermissions.setAllowManageUsers(true);
        executingUserPermissions.setAllowManageAllUsers(true);

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPartner(NRC_DTO);
        dto.setAllowView(true);
        dto.setAllowEdit(true);

        UpdateUserPermissions cmd = new UpdateUserPermissions(1, dto);

        UpdateUserPermissionsHandler.verifyAuthority(cmd, executingUserPermissions);
    }


//
//
//    /**
//     * Verifies that a user with the manageUsers permission can
//     * add another user to the UserDatabase
//     *
//     * @throws CommandException
//     */
//    @Test
//    public void testAuthorizedCreate() throws CommandException {
//
//        populate("schema1");
//
//        setUser(2);
//
//        UserPermissionDTO user = new UserPermissionDTO();
//        user.setEmail("ralph@lauren.com");
//        user.setName("Ralph");
//        user.setPartner(new PartnerDTO(1, "NRC"));
//        user.setAllowView(true);
//        user.setAllowEdit(true);
//
//        UpdateUserPermissions cmd = new UpdateUserPermissions(1, user);
//        execute(cmd);
//
//        UserResult result = execute(new GetUsers(1));
//        Assert.assertEquals(1, result.getTotalLength());
//        Assert.assertEquals("ralph@lauren.com", result.getData().get(0).getEmail());
//        Assert.assertTrue("edit permissions", result.getData().get(0).getAllowEdit());
//    }
//
//    /**
//     * Verifies that the owner of a database can update an existing users
//     * permission
//     *
//     * @throws CommandException
//     */
//    @Test
//    public void testOwnerUpdate() throws CommandException {
//        populate("schema1");
//        setUser(1);
//
//        UserPermissionDTO user = new UserPermissionDTO();
//        user.setEmail("bavon@nrcdrc.org");
//        user.setPartner(new PartnerDTO(1, "NRC"));
//        user.setAllowView(true);
//        user.setAllowViewAll(false);
//        user.setAllowEdit(true);
//        user.setAllowEdit(false);
//        user.setAllowDesign(true);
//
//        execute(new UpdateUserPermissions(1, user));
//
//        UserResult result = execute(new GetUsers(1));
//        UserPermissionDTO reUser = result.getData().get(0);
//        Assert.assertEquals("bavon@nrcdrc.org", reUser.getEmail());
//        Assert.assertTrue("design rights", user.getAllowDesign());
//
//    }


}
