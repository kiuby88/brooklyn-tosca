package org.apache.brooklyn.tosca.a4c;


import alien4cloud.model.components.Csar;
import alien4cloud.model.topology.Topology;
import alien4cloud.tosca.parser.ParsingResult;
import org.apache.brooklyn.api.mgmt.ManagementContext;
import org.apache.brooklyn.tosca.a4c.platform.Alien4CloudToscaPlatform;
import org.apache.brooklyn.util.core.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;

public class AbstractAlien4CloudPlatformTest extends AbstractAlien4CloudToscaTest{

    private static final Logger log = LoggerFactory.getLogger(AbstractAlien4CloudPlatformTest.class);

    private ManagementContext mgmt;
    protected Alien4CloudToscaPlatform platform;

    @BeforeMethod
    public void setup() throws Exception {
        super.setup();
        Alien4CloudToscaPlatform.grantAdminAuth();
        platform = Alien4CloudToscaPlatform.newInstance();
        platform.loadNodeTypes();
    }

    public Alien4CloudToscaPlatform getPlatform(){
        return platform;
    }

    public Topology getTopolofyFromClassPathTemplate(String templateName){
        String templateUrl = getClasspathUrlForResource(templateName);
        return getTopologyFromTemplate(templateUrl, templateName);
    }

    public Topology getTopologyFromTemplate(String templateUrl, String templateName){
        ParsingResult<Csar> tp = platform.uploadSingleYaml(new ResourceUtils(platform).getResourceFromUrl(templateUrl), templateName);
        return platform.getTopologyOfCsar(tp.getResult());
    }

}