/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.dispatch.loader;

import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import org.sigmah.shared.command.BatchCommand;
import org.sigmah.shared.command.Command;

public class CommandLoadEvent extends LoadEvent {

    private BatchCommand batch = new BatchCommand();

    public CommandLoadEvent(Loader<?> loader) {
        super(loader);
    }

    public CommandLoadEvent(Loader<?> loader, Object config) {
        super(loader, config);
    }

    public CommandLoadEvent(Loader<?> loader, Object config, Object data) {
        super(loader, config, data);
    }

    public CommandLoadEvent(Loader<?> loader, Object config, Throwable t) {
        super(loader, config, t);
    }

    public BatchCommand getBatch() {
        return batch;
    }

    public void addCommandToBatch(Command command) {
        batch.add(command);
    }

}
