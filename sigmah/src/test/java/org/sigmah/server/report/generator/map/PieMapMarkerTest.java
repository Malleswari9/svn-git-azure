/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.report.generator.map;

import junit.framework.Assert;
import org.junit.Test;
import org.sigmah.shared.dao.SiteTableColumn;
import org.sigmah.server.domain.SiteData;
import org.sigmah.shared.report.content.LatLng;
import org.sigmah.shared.report.content.MapContent;
import org.sigmah.shared.report.content.PieMapMarker;
import org.sigmah.shared.report.model.BubbleMapLayer;
import org.sigmah.shared.report.model.Dimension;
import org.sigmah.shared.report.model.DimensionType;
import org.sigmah.shared.report.model.MapElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Bertram
 */
public class PieMapMarkerTest {


    @Test
    public void testPies() {

        Dimension dim = new Dimension(DimensionType.Indicator);
        dim.setCategoryColor(101, 255);
        dim.setCategoryColor(102, 0x00FF00);
        dim.setCategoryColor(103, 0x0000FF);

        SiteData site1 = new SiteData();
        site1.setValue(SiteTableColumn.id,  1);
        site1.setValue(SiteTableColumn.x, 0d);
        site1.setValue(SiteTableColumn.y, 0d);
        site1.setIndicatorValue(101, 50d);
        site1.setIndicatorValue(102, 40d);
        site1.setIndicatorValue(103, 10d);

        List<SiteData> sites = new ArrayList<SiteData>();
        sites.add(site1);

        BubbleMapLayer layer = new BubbleMapLayer();
        layer.setPie(true);
        layer.addIndicator(101);
        layer.addIndicator(102);
        layer.addIndicator(103);
        layer.getColorDimensions().add(dim);

        MapElement mapElement = new MapElement();
        mapElement.addLayer(layer);

        MapContent content = new MapContent();

        TiledMap map = new TiledMap(640, 480, new LatLng(0, 0), 6);

        BubbleLayerGenerator gtor = new BubbleLayerGenerator(mapElement, layer);
        gtor.generate(sites, map, content);

        Assert.assertEquals(1, content.getMarkers().size());

        PieMapMarker marker = (PieMapMarker) content.getMarkers().get(0);
        Assert.assertEquals(3, marker.getSlices().size());
    }
}
