package org.activityinfo.client.page.map;

import org.activityinfo.client.page.PageId;
import org.activityinfo.client.page.PageState;
import org.activityinfo.client.page.PageStateParser;

import java.util.Arrays;
import java.util.List;

/**
 * @author Alex Bertram
 */
public class MapPageState implements PageState {

    public PageId getPageId() {
        return Maps.Maps;
    }

    public String serializeAsHistoryToken() {
        return null;
    }

    public List<PageId> getEnclosingFrames() {
        return Arrays.asList(getPageId());
    }

    public static class Parser implements PageStateParser {
        public PageState parse(String token) {
            return new MapPageState();
        }
    }
}
