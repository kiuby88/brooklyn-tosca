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

public class ComputeTopologyTest extends AbstractAlien4CloudToscaTest {

    String TEMPLATE ="templates/compute-template.yaml";
    String COMPUTE_TYPE= "tosca.nodes.Compute";
    String COMPUTE_NODE_ID= "my_server";

    @Test
    public void testComputeTopologyParser(){
        Topology topology=getTopolofyFromClassPathTemplate(TEMPLATE);

        assertNotNull(topology);
        assertEquals(topology.getNodeTemplates().size(), 1);

        NodeTemplate computeNode = topology.getNodeTemplates().get(COMPUTE_NODE_ID);
        assertEquals(computeNode.getType(),COMPUTE_TYPE);

        Map<String, AbstractPropertyValue> tomcatProperties = computeNode.getProperties();
        assertEquals(resolve(tomcatProperties, "num_cpus"), "1");
        assertEquals(resolve(tomcatProperties, "mem_size"), "4 MB");
        assertEquals(resolve(tomcatProperties, "disk_size"), "10 GB");
        assertNull(resolve(tomcatProperties, "os_arch"));
        assertNull(resolve(tomcatProperties, "os_type"));
        assertNull(resolve(tomcatProperties, "os_distribution"));
        assertNull(resolve(tomcatProperties, "os_version"));

    }
}
