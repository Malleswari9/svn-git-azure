/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.command;


import com.google.gwt.rpc.client.RpcService;
import com.google.gwt.user.client.rpc.RemoteService;

import org.sigmah.shared.command.result.CommandResult;
import org.sigmah.shared.exception.CommandException;

import java.util.List;

public interface RemoteCommandService extends RemoteService {
    
    List<CommandResult> execute(String authToken, List<Command> cmd) throws CommandException;

}
