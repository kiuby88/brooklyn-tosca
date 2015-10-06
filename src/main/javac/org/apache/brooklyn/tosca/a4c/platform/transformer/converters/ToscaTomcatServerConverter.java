package org.apache.brooklyn.tosca.a4c.platform.transformer.converters;

import alien4cloud.model.components.AbstractPropertyValue;
import alien4cloud.model.topology.NodeTemplate;
import org.apache.brooklyn.api.entity.EntitySpec;
import org.apache.brooklyn.api.mgmt.ManagementContext;
import org.apache.brooklyn.entity.webapp.jboss.JBoss7Server;
import org.apache.brooklyn.util.text.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ToscaTomcatServerConverter {

    private static final Logger log = LoggerFactory.getLogger(ToscaComputeToVanillaConverter.class);

    @SuppressWarnings("unused")
    private ManagementContext mgmt;

    public ToscaTomcatServerConverter(ManagementContext mgmt) {
        this.mgmt = mgmt;
    }

    public EntitySpec<JBoss7Server> toSpec(String id, NodeTemplate t) {
        EntitySpec<JBoss7Server> spec = EntitySpec.create(JBoss7Server.class);

        if (Strings.isNonBlank(t.getName())) {
            spec.displayName(t.getName());
        } else {
            spec.displayName(id);
        }

        spec.configure("tosca.node.type", t.getType());
        Map<String, AbstractPropertyValue> properties = t.getProperties();

        for(Map.Entry<String, AbstractPropertyValue> entry: properties.entrySet()){
            spec.configure(entry.getKey(), entry.getValue());
        }


        return spec;
    }


}
