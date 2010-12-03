/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.dto.element;

import org.sigmah.client.EventBus;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.dispatch.remote.Authentication;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.icon.IconImageBundle;
import org.sigmah.client.ui.HistoryWindow;
import org.sigmah.shared.command.GetHistory;
import org.sigmah.shared.command.result.HistoryResult;
import org.sigmah.shared.command.result.ValueResult;
import org.sigmah.shared.dto.EntityDTO;
import org.sigmah.shared.dto.element.handler.RequiredValueEvent;
import org.sigmah.shared.dto.element.handler.RequiredValueHandler;
import org.sigmah.shared.dto.element.handler.ValueEvent;
import org.sigmah.shared.dto.element.handler.ValueHandler;
import org.sigmah.shared.dto.history.HistoryTokenDTO;
import org.sigmah.shared.dto.history.HistoryTokenManager;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author Denis Colliot (dcolliot@ideia.fr)
 * 
 */
public abstract class FlexibleElementDTO extends BaseModelData implements EntityDTO, HistoryTokenManager {

    private static final long serialVersionUID = 8520711106031085130L;

    protected transient HandlerManager handlerManager;

    protected transient Dispatcher dispatcher;

    protected transient EventBus eventBus;

    protected transient Authentication authentication;

    protected transient FlexibleElementContainer currentContainerDTO;

    protected transient int preferredWidth;

    private transient Menu historyMenu;

    /**
     * Sets the dispatcher to be used in the
     * {@link #getElementComponent(ValueResult)} method.
     * 
     * @param dispatcher
     *            The presenter's dispatcher.
     */
    public void setService(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Sets the event bus to be used by
     * {@link #getElementComponent(ValueResult)}.
     * 
     * @param eventBus
     *            The presenter's event bus.
     */
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Sets the authentication provider to be used in the
     * {@link #getElementComponent(ValueResult)} method.
     * 
     * @param authentication
     *            The authentication provider.
     */
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    /**
     * Sets the current container (not model, but instance) using this flexible
     * element to be used in the {@link #getElementComponent(ValueResult)}
     * method.
     * 
     * @param currentContainerDTO
     *            The current container using this flexible element.
     */
    public void setCurrentContainerDTO(FlexibleElementContainer currentContainerDTO) {
        this.currentContainerDTO = currentContainerDTO;
    }

    /**
     * Method called just before the
     * {@link FlexibleElementDTO#getElementComponent(ValueResult)} method to
     * ensure the instantiation of the attributes used by the client-side.<br/>
     * This method can be override by subclasses.
     */
    public void init() {

        // Checks preconditions.
        assert dispatcher != null;
        assert authentication != null;
        assert currentContainerDTO != null;
        handlerManager = new HandlerManager(this);
    }

    /**
     * Gets the widget of a flexible element with its value.
     * 
     * @param valueResult
     *            value of the flexible element, or {@code null} to display the
     *            element without its value.
     * 
     * @return the widget corresponding to the flexible element.
     */
    public Component getElementComponent(ValueResult valueResult) {
        return getComponentWithHistory(valueResult, true);
    }

    /**
     * Gets the widget of a flexible element with its value.
     * 
     * @param valueResult
     *            value of the flexible element, or {@code null} to display the
     *            element without its value.
     * @param enabled
     *            If the component is enabled.
     * 
     * @return the widget corresponding to the flexible element.
     */
    public Component getElementComponent(ValueResult valueResult, boolean enabled) {
        return getComponentWithHistory(valueResult, enabled);
    }

    /**
     * Gets the widget of a flexible element with its value. This method manages
     * the history of the element.
     * 
     * @param valueResult
     *            value of the flexible element, or {@code null} to display the
     *            element without its value.
     * @param enabled
     *            If the component is enabled.
     * @return
     */
    private Component getComponentWithHistory(ValueResult valueResult, boolean enabled) {

        final Component component = getComponent(valueResult, enabled);

        // Adds the history menu if needed.
        if (isHistorable()) {

            // Builds the menu.
            if (historyMenu == null) {

                final MenuItem historyItem = new MenuItem(I18N.CONSTANTS.historyShow(),
                        IconImageBundle.ICONS.history(), new SelectionListener<MenuEvent>() {

                            @Override
                            public void componentSelected(MenuEvent ce) {

                                dispatcher.execute(new GetHistory(getId()), null, new AsyncCallback<HistoryResult>() {

                                    @Override
                                    public void onFailure(Throwable e) {

                                        Log.error("[execute] The history cannot be fetched.", e);
                                        MessageBox.alert(I18N.CONSTANTS.historyError(),
                                                I18N.CONSTANTS.historyErrorDetails(), null);
                                    }

                                    @Override
                                    public void onSuccess(HistoryResult result) {
                                        HistoryWindow.show(result.getTokens(), FlexibleElementDTO.this);
                                    }
                                });
                            }
                        });

                historyMenu = new Menu();
                historyMenu.add(historyItem);
            }

            // Attaches it to the element.
            component.setContextMenu(historyMenu);
        }

        return component;
    }

    /**
     * Gets the widget of a flexible element with its value.
     * 
     * @param valueResult
     *            value of the flexible element, or {@code null} to display the
     *            element without its value.
     * @param enabled
     *            If the component is enabled.
     * 
     * @return the widget corresponding to the flexible element.
     */
    protected abstract Component getComponent(ValueResult valueResult, boolean enabled);

    /**
     * Adds a {@link ValueHandler} to the flexible element.
     * 
     * @param handler
     *            a {@link ValueHandler} object
     */
    public void addValueHandler(ValueHandler handler) {
        handlerManager.addHandler(ValueEvent.getType(), handler);
    }

    /**
     * Adds a {@link RequiredValueHandler} to the flexible element.
     * 
     * @param handler
     *            a {@link RequiredValueHandler} object
     */
    public void addRequiredValueHandler(RequiredValueHandler handler) {
        handlerManager.addHandler(RequiredValueEvent.getType(), handler);
    }

    /**
     * Gets the most adapted width to display this component.
     * 
     * @return The preferred width of this element.
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }

    // Flexible element id
    @Override
    public int getId() {
        return (Integer) get("id");
    }

    public void setId(int id) {
        set("id", id);
    }

    // Flexible element label
    public String getLabel() {
        return get("label");
    }

    public void setLabel(String label) {
        set("label", label);
    }

    // Flexible element validates
    public boolean getValidates() {
        return (Boolean) get("validates");
    }

    public void setValidates(boolean validates) {
        set("validates", validates);
    }

    public boolean isFilledIn() {
        return (Boolean) get("filledIn");
    }

    public void setFilledIn(boolean filledIn) {
        set("filledIn", filledIn);
    }

    public boolean isHistorable() {
        return (Boolean) get("historable");
    }

    public void setHistorable(boolean historable) {
        set("historable", historable);
    }

    protected void ensureHistorable() {
        if (!isHistorable()) {
            throw new IllegalStateException("The current flexible element '" + getClass().getName() + "' #" + getId()
                    + " doesn't manage an history.");
        }
    }

    /**
     * Assigns a value to a flexible element.
     * 
     * @param result
     *            The value.
     */
    public void assignValue(ValueResult result) {
        setFilledIn(isCorrectRequiredValue(result));
    }

    /**
     * Returns if a value can be considered as a correct required value for this
     * specific flexible element.
     * 
     * @param result
     *            The value.
     * @return If the value can be considered as a correct required value.
     */
    public abstract boolean isCorrectRequiredValue(ValueResult result);

    @Override
    public String getElementLabel() {
        return getLabel();
    }

    @Override
    public Object renderHistoryToken(HistoryTokenDTO token) {
        ensureHistorable();
        return token.getValue();
    }
}
