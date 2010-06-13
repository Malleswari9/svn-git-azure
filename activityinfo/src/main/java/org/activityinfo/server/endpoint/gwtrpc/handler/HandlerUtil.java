/*
 * This file is part of ActivityInfo.
 *
 * ActivityInfo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ActivityInfo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ActivityInfo.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009 Alex Bertram and contributors.
 */

package org.activityinfo.server.endpoint.gwtrpc.handler;


import com.google.inject.Injector;
import org.activityinfo.server.domain.User;
import org.activityinfo.server.domain.UserDatabase;
import org.activityinfo.shared.command.Command;
import org.activityinfo.shared.command.Month;
import org.activityinfo.shared.command.result.CommandResult;
import org.activityinfo.shared.exception.CommandException;
import org.dozer.Mapper;

import java.util.*;

/**
 * Convienence methods for <code>CommandHandler</code>s
 */
public class HandlerUtil {

    /**
     * Returns the entity class for the given entity name.
     * For example, "Site" => org.activityinfo.server.domain.Site.class
     *
     * @param name The name of the entity
     * @return The associated domain entity class
     * @throws ClassNotFoundException if the entity name does not match a domain entity
     */
    protected Class classForEntityName(String name) throws ClassNotFoundException {

        if (name.equals("Database")) {
            return UserDatabase.class;
        }

        return CommandHandler.class.getClassLoader().loadClass("org.activityinfo.server.domain." + name);
    }

    /**
     * Returns the <code>CommandHandler</code> that corresponds to the given <code>Command</code>.
     * <strong>Only</strong> the package org.activityinfo.server.command.handler
     * is searched, so the handler must be there.
     *
     * @param cmd The <code>Command</code> for which a <code>CommandHandler</code> is to be returned
     * @return A <code>CommandHandler</code> capabling of handling the given <code>Command</code>
     */
    @SuppressWarnings("unchecked")
    public static Class<CommandHandler<?>> executorForCommand(Command<?> cmd) {

        String commandName = cmd.getClass().getName().substring(
                cmd.getClass().getPackage().getName().length() + 1);

        String etorName = CommandHandler.class.getPackage().getName() + "." +
                commandName + "Handler";

        try {
            return (Class<CommandHandler<?>>) CommandHandler.class.getClassLoader().loadClass(etorName);

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("No handler class for " + commandName, e);
        }
    }

    public static <T extends CommandResult> T execute(Injector injector, Command<T> cmd, User user) throws CommandException {
        Class<? extends CommandHandler> handlerClass = executorForCommand(cmd);
        CommandHandler handler = injector.getInstance(handlerClass);
        return (T) handler.execute(cmd, user);
    }


    public static <T> List<T> mapList(Mapper mapper, Collection<?> source, Class<T> destinationClass) {
        List<T> list = new ArrayList<T>(source.size());
        for (Object s : source) {
            list.add(mapper.map(s, destinationClass));
        }
        return list;
    }

    static Month monthFromRange(Date date1, Date date2) {

        Calendar c1 = Calendar.getInstance();
        c1.setTime(date1);
        if (c1.get(Calendar.DAY_OF_MONTH) != 1) {
            return null;
        }

        Calendar c2 = Calendar.getInstance();
        c2.setTime(date2);
        if (c2.get(Calendar.DAY_OF_MONTH) != c2.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            return null;
        }

        if (c2.get(Calendar.MONTH) != c1.get(Calendar.MONTH) ||
                c2.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {

            return null;
        }

        return new Month(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH) + 1);

    }
}
