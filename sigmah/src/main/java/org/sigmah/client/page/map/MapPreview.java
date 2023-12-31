/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.map;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.maps.client.MapType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.sigmah.client.dispatch.monitor.MaskingAsyncMonitor;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.map.GcIconFactory;
import org.sigmah.client.map.IconFactory;
import org.sigmah.client.map.MapApiLoader;
import org.sigmah.client.map.MapTypeFactory;
import org.sigmah.shared.map.BaseMap;
import org.sigmah.shared.report.content.Content;
import org.sigmah.shared.report.content.Extents;
import org.sigmah.shared.report.content.MapContent;
import org.sigmah.shared.report.content.MapMarker;
import org.sigmah.shared.report.model.MapElement;
import org.sigmah.shared.report.model.ReportElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the content of a MapElement using Google Maps.
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
class MapPreview extends ContentPanel {
    private MapWidget map = null;
    private String currentBaseMapId = null;
    private LatLngBounds pendingZoom = null;

    /**
     * List of <code>Overlay</code>s that have been added to the map.
     */
    private List<Overlay> overlays = new ArrayList<Overlay>();
    private Status status;

    private MapElement element;
    private MapContent content;

    /**
     * True if the Google Maps API is not loaded AND
     * an attempt to load the API has already failed.
     */
    private boolean apiLoadFailed = false;


    public MapPreview() {

        setHeading(I18N.CONSTANTS.preview());
        setLayout(new FlowLayout());
        setScrollMode(Style.Scroll.AUTO);

        status = new Status();
        ToolBar toolBar = new ToolBar();
        toolBar.add(status);
        setBottomComponent(toolBar);

        // seems like a good time to preload the MapsApi if
        // we haven't already done so

        MapApiLoader.load();

        addListener(Events.AfterLayout, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                if (map != null) {
                    map.checkResizeAndCenter();
                }
                if (pendingZoom != null) {
                    Log.debug("MapPreview: zooming to " + map.getBoundsZoomLevel(pendingZoom));
                    map.setCenter(pendingZoom.getCenter(),
                            map.getBoundsZoomLevel(pendingZoom));
                }
            }
        });
    }

    private void zoomToBounds(LatLngBounds bounds) {

        int zoomLevel = map.getBoundsZoomLevel(bounds);
        if (zoomLevel == 0) {
            Log.debug("MapPreview: deferring zoom.");
            pendingZoom = bounds;
        } else {
            Log.debug("MapPreview: zooming to level " + zoomLevel);
            map.setCenter(bounds.getCenter(), zoomLevel);
            pendingZoom = null;
        }
    }

    public void setContent(ReportElement element, Content content) {
        this.element = (MapElement) element;
        this.content = (MapContent) content;

        if (!apiLoadFailed) {

            clearOverlays();

            if (content instanceof MapContent) {
                createMapIfNeededAndUpdateMapContent();
            }
        }
    }

    private LatLngBounds llBoundsForExtents(Extents extents) {
        return LatLngBounds.newInstance(
                LatLng.newInstance(extents.getMinLat(), extents.getMinLon()),
                LatLng.newInstance(extents.getMaxLat(), extents.getMaxLon()));
    }


    public void createMapIfNeededAndUpdateMapContent() {

        if (map == null) {
            MapApiLoader.load(new MaskingAsyncMonitor(this, I18N.CONSTANTS.loadingMap()),
                    new AsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {

                            apiLoadFailed = false;

                            map = new MapWidget();
                            map.setDraggable(false);

                            changeBaseMapIfNeeded(content.getBaseMap());

                            // clear the error message content
                            removeAll();

                            add(map);

                            updateMapToContent();
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            handleApiLoadFailure();
                        }
                    });

        } else {
            clearOverlays();
            changeBaseMapIfNeeded(content.getBaseMap());
            updateMapToContent();
        }
    }

    private void changeBaseMapIfNeeded(BaseMap baseMap) {
        if (currentBaseMapId == null || !currentBaseMapId.equals(baseMap.getId())) {
            MapType baseMapType = MapTypeFactory.mapTypeForBaseMap(baseMap);
            map.addMapType(baseMapType);
            map.setCurrentMapType(baseMapType);
            map.removeMapType(MapType.getNormalMap());
            map.removeMapType(MapType.getHybridMap());
            currentBaseMapId = baseMap.getId();
        }
    }

    /**
     * Clears all existing content from the map
     */
    private void clearOverlays() {
        for (Overlay overlay : overlays) {
            map.removeOverlay(overlay);
        }
        overlays.clear();
    }


    /**
     * Updates the size of the map and adds Overlays to reflect the content
     * of the current
     */
    private void updateMapToContent() {

        map.setWidth(element.getWidth() + "px");
        map.setHeight(element.getHeight() + "px");

        Log.debug("MapPreview: Received content, extents are = " + content.getExtents().toString());

        zoomToBounds(llBoundsForExtents(content.getExtents()));

        layout();

        map.checkResizeAndCenter();


        // TODO: i18n
        status.setStatus(content.getUnmappedSites().size() + " " + I18N.CONSTANTS.siteLackCoordiantes(), null);

        GcIconFactory iconFactory = new GcIconFactory();
        iconFactory.primaryColor = "#0000FF";

        for (MapMarker marker : content.getMarkers()) {
            Icon icon = IconFactory.createIcon(marker);
            LatLng latLng = LatLng.newInstance(marker.getLat(), marker.getLng());

            MarkerOptions options = MarkerOptions.newInstance();
            options.setIcon(icon);

            Marker overlay = new Marker(latLng, options);

            map.addOverlay(overlay);
            overlays.add(overlay);
        }
    }

    /**
     * Handles the failure of the Google Maps API to load.
     */
    private void handleApiLoadFailure() {

        apiLoadFailed = true;

        if (this.getItemCount() == 0) {

            add(new Html(I18N.CONSTANTS.cannotLoadMap()));
            add(new Button(I18N.CONSTANTS.retry(), new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    createMapIfNeededAndUpdateMapContent();
                }
            }));
            layout();
        }
    }
}
