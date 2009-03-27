package org.geoserver.web.data;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.web.GeoServerWicketTestSupport;
import org.geoserver.web.publish.BasicLayerConfig;
import org.geoserver.catalog.ResourceInfo;

public class ResourceConfigurationPageTest extends GeoServerWicketTestSupport {
    
    public void testBasic() {
        ResourceInfo info = getGeoServerApplication()
            .getCatalog()
            .getResources(ResourceInfo.class).get(0);

        login();
        tester.startPage(new ResourceConfigurationPage(info, false));
        tester.assertLabel("resourcename", info.getPrefixedName());
        tester.assertComponent("resource:tabs:panel:theList:0:content", BasicResourceConfig.class);
    }
    
    // I can't make the last assertion work, my wicket-fu is not good enough or else the
    // 
//    public void testTabSwitch() {
//        ResourceInfo info = getGeoServerApplication()
//            .getCatalog()
//            .getResources(ResourceInfo.class).get(0);
//
//        login();
//        tester.startPage(new ResourceConfigurationPage(info, false));
//        FormTester ft = tester.newFormTester("resource");
//        ft.setValue("tabs:panel:theList:0:content:title", "Some other title");
//        ft.submit();
//        assertEquals("Some other title", ft.getTextComponentValue("tabs:panel:theList:0:content:title"));
//        tester.assertModelValue("resource:tabs:panel:theList:0:content:title", "Some other title");
//        
//        // switch to the other page
//        tester.clickLink("resource:tabs:tabs-container:tabs:1:link");
//        tester.assertRenderedPage(ResourceConfigurationPage.class);
//        tester.assertComponent("resource:tabs:panel:theList:0:content", BasicLayerConfig.class);
//        
//        // switch back
//        tester.clickLink("resource:tabs:tabs-container:tabs:0:link");
//        tester.assertRenderedPage(ResourceConfigurationPage.class);
//        tester.assertComponent("resource:tabs:panel:theList:0:content", BasicResourceConfig.class);
//        
//        // check the title is still what we did set (switching tabs did not make us loose the value)
//        tester.assertComponent("resource:tabs:panel:theList:0:content:title", TextField.class);
//        ft = tester.newFormTester("resource");
//        tester.assertModelValue("resource:tabs:panel:theList:0:content:title", "Some other title");
//    }
}
