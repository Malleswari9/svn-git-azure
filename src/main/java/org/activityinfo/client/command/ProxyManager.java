package org.activityinfo.client.command;

import org.activityinfo.shared.command.Command;
import org.activityinfo.shared.command.result.CommandResult;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.core.client.GWT;

public class ProxyManager implements CommandEventSource {

    private Map<Class<? extends Command>, List<CommandListener>> listeners =
            new HashMap<Class<? extends Command>, List<CommandListener>>();

    private Map<Class<? extends Command>, List<CommandProxy>> proxies =
            new HashMap<Class<? extends Command>, List<CommandProxy>>();

    public ProxyManager() {

    }

    @Override
    public <T extends Command> void registerListener(Class<T> commandClass, CommandListener<T> listener) {
        List<CommandListener> classListeners = listeners.get(commandClass);
        if (classListeners == null) {
            classListeners = new ArrayList<CommandListener>();
            listeners.put(commandClass, classListeners);
        }
        classListeners.add(listener);
    }

    @Override
    public <T extends Command> void registerProxy(Class<T> commandClass, CommandProxy<T> proxy) {
        List<CommandProxy> classProxies = proxies.get(commandClass);
        if (classProxies == null) {
            classProxies = new ArrayList<CommandProxy>();
            proxies.put(commandClass, classProxies);
        }
        classProxies.add(proxy);
    }

    public void notifyListenersOfSuccess(Command cmd, CommandResult result) {

        List<CommandListener> classListeners = listeners.get(cmd.getClass());
        if (classListeners != null) {
            for (CommandListener listener : classListeners) {
                try {
                    listener.onSuccess(cmd, result);
                } catch (Exception e) {
                    GWT.log("ProxyManager: listener threw exception during onSuccess notification.",e);
                }
            }
        }
    }

    public void notifyListenersBefore(Command cmd) {

        List<CommandListener> classListeners = listeners.get(cmd.getClass());
        if (classListeners != null) {
            for (CommandListener listener : classListeners) {
                try {
                    listener.beforeCalled(cmd);
                } catch (Exception e) {
                    GWT.log("ProxyManager: listener threw exception during beforeCalled notification", e);
                }
            }
        }
    }

    /**
     * Attempts to execute the command locally using one of the registered
     * proxies
     *
     * @param cmd
     * @return
     */
    public CommandProxyResult execute(Command cmd) {

        List<CommandProxy> classProxies = proxies.get(cmd.getClass());

        if (classProxies != null) {

            for (CommandProxy proxy : classProxies) {


                try {
                    CommandProxyResult r = proxy.execute(cmd);
                    if (r.couldExecute) {

                        GWT.log("ProxyManager: EXECUTED (!!) " + cmd.toString() + " locally with proxy "
                                + proxy.getClass().getName(), null);


                        return r;
                    } else {
                           GWT.log("ProxyManager: Failed to execute " + cmd.toString() + " locally with proxy "
                                + proxy.getClass().getName(), null);


                        return r;
                    }
                } catch (Exception e) {
                    GWT.log("ProxyManager: proxy threw exception during call to execute", e);
                }
            }
        }

        return CommandProxyResult.couldNotExecute();
    }
}