/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.command.handler;

import org.sigmah.shared.command.Command;
import org.sigmah.shared.command.result.CommandResult;
import org.sigmah.shared.domain.User;
import org.sigmah.shared.exception.CommandException;

/**
 * Command executors are the server half of {@link Command}s defined in the
 * client package. Each {@link Command} has its corresponding executor which
 * is responsible for carrying out the command on the server.
 *
 * @author Alex Bertram
 */
public interface CommandHandler<CommandT extends Command> {

    /*
      * TODO: is there anyway the return type can be automatically parameratized
      * with the type parameter of CommandT ? (and without adding a second type
      * parameter to CommandHandler
      */


    /**
     * Execute a command received from the client
     *
     * @param <T> Result type
     * @param cmd Command received from the server
     * @return The result of command if successful. If the command is not successful, an exception should be thrown.
     * @throws org.sigmah.shared.exception.CommandException
     *
     */
    public CommandResult execute(CommandT cmd, User user) throws CommandException;


}
