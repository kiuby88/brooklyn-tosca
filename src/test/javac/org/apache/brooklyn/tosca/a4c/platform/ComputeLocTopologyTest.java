package org.apache.brooklyn.tosca.a4c.platform;

import alien4cloud.model.components.AbstractPropertyValue;
import alien4cloud.model.topology.NodeTemplate;
import alien4cloud.model.topology.Topology;
import org.apache.brooklyn.tosca.a4c.AbstractAlien4CloudToscaTest;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class ComputeLocTopologyTest extends AbstractAlien4CloudToscaTest {

    String TEMPLATE ="templates/computeLoc-template.yaml";
    String COMPUTE_LOC_TYPE= "tosca.nodes.ComputeLoc";
    String COMPUTE_LOC_NODE= "compute_loc";

    @Test
    public void testTomcatTopologyParser(){
        Topology topology=getTopolofyFromClassPathTemplate(TEMPLATE);

        assertNotNull(topology);
        assertEquals(topology.getNodeTemplates().size(), 1);

        NodeTemplate tomcatNode = topology.getNodeTemplates().get(COMPUTE_LOC_NODE);
        assertEquals(tomcatNode.getType(),COMPUTE_LOC_TYPE);

        Map<String, AbstractPropertyValue> tomcatProperties = tomcatNode.getProperties();
        assertEquals(resolve(tomcatProperties, "location"), "aws-ec2:us-west-2");
        assertNull(resolve(tomcatProperties, "num_cpus"));
        assertNull(resolve(tomcatProperties, "mem_size"));
        assertNull(resolve(tomcatProperties, "disk_size"));
        assertNull(resolve(tomcatProperties, "os_arch"));
        assertNull(resolve(tomcatProperties, "os_type"));
        assertNull(resolve(tomcatProperties, "os_distribution"));
        assertNull(resolve(tomcatProperties, "os_version"));

    }
}
