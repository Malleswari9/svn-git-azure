/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.dispatch.remote;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.sigmah.client.dispatch.CommandProxy;
import org.sigmah.client.dispatch.remote.cache.ProxyResult;
import org.sigmah.client.mock.MockEventBus;
import org.sigmah.shared.command.Command;
import org.sigmah.shared.command.GetSchema;
import org.sigmah.shared.command.RemoteCommandServiceAsync;
import org.sigmah.shared.command.result.CommandResult;
import org.sigmah.shared.dto.SchemaDTO;
import org.sigmah.shared.exception.CommandException;

import java.util.Collections;

import static org.easymock.EasyMock.*;

public class RemoteDispatcherTest {

    private static final String AUTH_TOKEN = "XYZ";

    private RemoteCommandServiceAsync service;
    private RemoteDispatcher dispatcher;
    private CommandProxy proxy;

    private Capture<AsyncCallback> remoteCallback = new Capture<AsyncCallback>();


    @Before
    public void setUp() {
        service = createMock("remoteService", RemoteCommandServiceAsync.class);
        proxy = createMock("proxy", CommandProxy.class);

        dispatcher = new RemoteDispatcher(service, new MockEventBus(),
                new Authentication(1, AUTH_TOKEN, "alex@alex.com"));
    }

    @Test
    public void commandShouldBeSentToServerIfThereAreNoProxiesAndNoPendingCommands() {

        // define our expectations
        expectRemoteCall(new GetSchema());
        replay(service);

        // trigger a call
        dispatcher.execute(new GetSchema(), null, makeNullCallback());
        dispatcher.processPendingCommands();

        // verify that the command was dispatched to the server
        verify(service);
    }

    @Test
    public void duplicateCommandsShouldBeMergedWithPendingRequests() {

        expectRemoteCall(new GetSchema());
        replay(service);

        // simulate successive dispatches of the same command from different
        // components of the application
        dispatcher.execute(new GetSchema(), null, makeNullCallback());
        dispatcher.execute(new GetSchema(), null, makeNullCallback());
        dispatcher.processPendingCommands();

        // verify that only one command was sent
        verify(service);
    }

    @Test
    public void duplicateCommandsShouldBeMergedWithExecutingRequests() {

        expectRemoteCall(new GetSchema());
        replay(service);

        // simulate successive dispatches of the same command from different
        // components of the application
        dispatcher.execute(new GetSchema(), null, makeNullCallback());
        dispatcher.processPendingCommands();
        dispatcher.execute(new GetSchema(), null, makeNullCallback());

        // verify that only one command was sent
        verify(service);
    }

    @Test
    public void mergedCommandsShouldEachReceiveACallback() {

        expectRemoteCall(new GetSchema());
        andCallbackWihSuccess(new SchemaDTO());
        replay(service);

        AsyncCallback callback1 = makeCallbackThatExpectsNonNullSuccess();
        AsyncCallback callback2 = makeCallbackThatExpectsNonNullSuccess();

        // simulate successive dispatches of the same command from different
        // components of the application
        dispatcher.execute(new GetSchema(), null, callback1);
        dispatcher.execute(new GetSchema(), null, callback2);
        dispatcher.processPendingCommands();

        // verify that only one command was sent
        verify(callback1);
        verify(callback2);
    }

    @Test
    public void commandsSuccessfullyExecutedThroughProxiesShouldNotBeSentToServer() {

        GetSchema command = new GetSchema();

        expect(proxy.maybeExecute(eq(command))).andReturn(new ProxyResult(new SchemaDTO()));
        replay(proxy);

        replay(service);   // no calls should be made to the remote service

        AsyncCallback callback = makeCallbackThatExpectsNonNullSuccess();

        dispatcher.registerProxy(GetSchema.class, proxy);
        dispatcher.execute(new GetSchema(), null, callback);
        dispatcher.processPendingCommands();

        verify(proxy, service, callback);
    }


    @Test
    public void commandsUnsuccessfullyExecutedThroughProxiesShouldBeSentToServer() {

        GetSchema command = new GetSchema();

        expect(proxy.maybeExecute(eq(command))).andReturn(ProxyResult.couldNotExecute());
        replay(proxy);

        expectRemoteCall(command);
        andCallbackWihSuccess(new SchemaDTO());
        replay(service);

        AsyncCallback callback = makeCallbackThatExpectsNonNullSuccess();

        dispatcher.registerProxy(GetSchema.class, proxy);
        dispatcher.execute(new GetSchema(), null, callback);
        dispatcher.processPendingCommands();

        verify(proxy, service, callback);
    }

    @Test
    public void commandExceptionsShouldBeCalledBackWithFailure() {

        expectRemoteCall(new GetSchema());
        andCallbackWihSuccess(new CommandException());  // remote call succeeded, command failed
        replay(service);

        AsyncCallback callback = makeCallbackThatExpectsFailure();

        dispatcher.execute(new GetSchema(), null, callback);
        dispatcher.processPendingCommands();

        verify(service, callback);
    }

    /**
     * The RemoteDispatcher will group and bundle commands together-- we
     * need to make sure that different components remain isolated from
     * failures within other components.
     */
    @Test
    public void exceptionsThrownByCallbacksDoNotDistubOthers() {

        expectRemoteCall(new GetSchema());
        andCallbackWihSuccess(new SchemaDTO());
        replay(service);

        // Here we set up one component that will call request a command
        // but something will go wrong when the command return (successfully)
        // the error is unrelated to the remote command -- it just happens to be
        // there.
        dispatcher.execute(new GetSchema(), null, new AsyncCallback<SchemaDTO>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(SchemaDTO result) {
                throw new Error();
            }
        });

        // the second command independently requests the same command,
        // we need to make sure we receive a result
        AsyncCallback secondCallback = makeCallbackThatExpectsNonNullSuccess();
        dispatcher.execute(new GetSchema(), null, secondCallback);

        dispatcher.processPendingCommands();

        verify(secondCallback);
    }


    private AsyncCallback<SchemaDTO> makeNullCallback() {
        return new AsyncCallback<SchemaDTO>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(SchemaDTO o) {
            }
        };
    }

    private AsyncCallback makeCallbackThatExpectsNonNullSuccess() {
        AsyncCallback callback = createMock(AsyncCallback.class);
        callback.onSuccess(notNull());
        replay(callback);
        return callback;
    }

    private AsyncCallback makeCallbackThatExpectsFailure() {
        AsyncCallback callback = createMock(AsyncCallback.class);
        callback.onFailure(isA(Throwable.class));
        replay(callback);
        return callback;
    }


    private void expectRemoteCall(GetSchema command) {
        service.execute(
                eq(AUTH_TOKEN),
                eq(Collections.<Command>singletonList(command)),
                capture(remoteCallback));
    }

    private void andCallbackWihSuccess(final CommandResult result) {
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                ((AsyncCallback) getCurrentArguments()[2]).onSuccess(Collections.singletonList(result));
                return null;
            }
        });
    }


}