/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.inject;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Cookies;
import com.google.inject.Provider;
import org.sigmah.client.dispatch.remote.Authentication;

/**
 * Supplies user <code>Authentication</code> information from
 * the <code>authToken</code> and <code>email</code> cookies, or
 * failing that, from the <code>UserInfo</code> dictionary.
 *
 * @author Alex Bertram
 */
public class AuthProvider implements Provider<Authentication> {

    public Authentication get() {

        if (Cookies.getCookie("authToken") != null &&
                Cookies.getCookie("userId") != null &&
                Cookies.getCookie("email") != null) {

            return new Authentication(
                    Integer.parseInt(Cookies.getCookie("userId")),
                    Cookies.getCookie("authToken"),
                    Cookies.getCookie("email"));

        }

        Dictionary userInfo;
        try {
            userInfo = Dictionary.getDictionary("UserInfo");
            return new Authentication(
                    Integer.parseInt(userInfo.get("userId")),
                    userInfo.get("authToken"),
                    userInfo.get("email"));
        } catch (Exception e) {
            Log.fatal("DictionaryAuthenticationProvider: exception retrieving dictionary from page", e);
            throw new Error();
        }
    }
}
