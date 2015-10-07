package org.apache.brooklyn.tosca.a4c;


import alien4cloud.model.components.AbstractPropertyValue;
import alien4cloud.model.components.Csar;
import alien4cloud.model.components.ScalarPropertyValue;
import alien4cloud.model.topology.Topology;
import alien4cloud.tosca.parser.ParsingResult;
import org.apache.brooklyn.api.mgmt.ManagementContext;
import org.apache.brooklyn.core.entity.Entities;
import org.apache.brooklyn.core.mgmt.internal.LocalManagementContext;
import org.apache.brooklyn.tosca.a4c.platform.Alien4CloudToscaPlatform;
import org.apache.brooklyn.util.core.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.Map;

public class AbstractAlien4CloudToscaTest {


    private static final Logger log = LoggerFactory.getLogger(AbstractAlien4CloudToscaTest.class);

    private ManagementContext mgmt;

    protected Alien4CloudToscaPlatform platform;

    @BeforeMethod
    public void setup() throws Exception {
        mgmt = new LocalManagementContext();
        Alien4CloudToscaPlatform.grantAdminAuth();
        platform = Alien4CloudToscaPlatform.newInstance();
        platform.loadNodeTypes();
    }

    @AfterMethod
    public void shutdown() {
        if (mgmt != null) Entities.destroyAll(mgmt);
    }

    public Alien4CloudToscaPlatform getPlatform(){
        return platform;
    }

    public ManagementContext getMgmt(){
        return mgmt;
    }

    public String getClasspathUrlForResource(String resourceName){
        return "classpath://"+ resourceName;
    }

    public Topology getTopolofyFromClassPathTemplate(String templateName){
        String templateUrl = getClasspathUrlForResource(templateName);
        return getTopologyFromTemplate(templateUrl, templateName);
    }

    public Topology getTopologyFromTemplate(String templateUrl, String templateName){
        ParsingResult<Csar> tp = platform.uploadSingleYaml(new ResourceUtils(platform).getResourceFromUrl(templateUrl), templateName);
        return platform.getTopologyOfCsar(tp.getResult());
    }


    //TODO this method should be moved to a Property Management Class
    public static String resolve(Map<String, AbstractPropertyValue> props, String ...keys) {
        for (String key: keys) {
            AbstractPropertyValue v = props.get(key);
            if (v==null) continue;
            if (v instanceof ScalarPropertyValue) return ((ScalarPropertyValue)v).getValue();
            log.warn("Ignoring unsupported property value "+v);
        }
        return null;
    }




}
