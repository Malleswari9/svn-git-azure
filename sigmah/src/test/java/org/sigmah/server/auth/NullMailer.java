/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.auth;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.sigmah.server.mail.MailSender;

public class NullMailer implements MailSender {

    public void send(Email message) throws EmailException {
        // NOOP
    }
}
