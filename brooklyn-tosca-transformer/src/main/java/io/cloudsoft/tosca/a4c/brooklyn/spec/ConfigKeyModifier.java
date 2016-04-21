package io.cloudsoft.tosca.a4c.brooklyn.spec;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import io.cloudsoft.tosca.a4c.brooklyn.ToscaApplication;
import io.cloudsoft.tosca.a4c.brooklyn.ToscaFacade;
import org.apache.brooklyn.api.entity.Entity;
import org.apache.brooklyn.api.entity.EntitySpec;
import org.apache.brooklyn.api.mgmt.ManagementContext;
import org.apache.brooklyn.core.config.ConfigKeys;
import org.apache.brooklyn.util.collections.MutableSet;
import org.apache.brooklyn.util.core.config.ConfigBag;
import org.apache.brooklyn.util.core.flags.FlagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ConfigKeyModifier extends AbstractSpecModifier {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigKeyModifier.class);

    public ConfigKeyModifier(ManagementContext mgmt, ToscaFacade<? extends ToscaApplication> alien4CloudFacade) {
        super(mgmt, alien4CloudFacade);
    }

    protected void configureConfigKeysSpec(EntitySpec spec, ConfigBag bag) {
        configureConfigKeysSpec(spec, bag, false);
    }

    protected void updateConfigKeySpec(EntitySpec spec, ConfigBag bag) {
        configureConfigKeysSpec(spec, bag, true);
    }

    protected void configureConfigKeysSpec(EntitySpec spec, ConfigBag bag, boolean joinWithOldValues) {
        Set<String> keyNamesUsed = new LinkedHashSet<>();
        configureWithAllRecords(findAllFlagsAndConfigKeys(spec, bag), spec, keyNamesUsed, joinWithOldValues);
        setUnusedKeysAsAnonymousKeys(spec, keyNamesUsed, bag);
    }

    private void configureWithAllRecords(Collection<FlagUtils.FlagConfigKeyAndValueRecord> records,
                                         EntitySpec spec,
                                         Set<String> keyNamesUsed,
                                         boolean joinWithOldValues) {

        for (FlagUtils.FlagConfigKeyAndValueRecord r : records) {
            if (r.getFlagMaybeValue().isPresent()) {
                configureWithResolvedFlag(r, spec, keyNamesUsed, joinWithOldValues);
            }
            if (r.getConfigKeyMaybeValue().isPresent()) {
                configureWithResolvedConfigKey(r, spec, keyNamesUsed, joinWithOldValues);
            }
        }
    }

    private void configureWithResolvedFlag(FlagUtils.FlagConfigKeyAndValueRecord r,
                                           EntitySpec spec,
                                           Set<String> keyNamesUsed,
                                           boolean joinWithOldValues) {
        Optional<Object> resolvedValue = resolveValue(r.getFlagMaybeValue().get(), Optional.<TypeToken>absent());
        if (joinWithOldValues) {
            Optional<Object> oldValue = findFlagValue(spec, r);
            resolvedValue = joinOldAndNewSpecConfigValues(oldValue, resolvedValue);
        }
        if (resolvedValue.isPresent()) {
            spec.configure(r.getFlagName(), resolvedValue.get());
        }
        keyNamesUsed.add(r.getFlagName());
    }


    private void configureWithResolvedConfigKey(FlagUtils.FlagConfigKeyAndValueRecord r,
                                                EntitySpec spec,
                                                Set<String> keyNamesUsed,
                                                boolean joinWithOldValues) {
        try {
            Optional<Object> resolvedValue = resolveValue(r.getConfigKeyMaybeValue().get(),
                    Optional.<TypeToken>of(r.getConfigKey().getTypeToken()));
            if (joinWithOldValues) {
                Optional<Object> oldValue = getConfigKeyValue(spec, r);
                resolvedValue = joinOldAndNewSpecConfigValues(oldValue, resolvedValue);
            }

            if (resolvedValue.isPresent()) {
                spec.configure(r.getConfigKey(), resolvedValue.get());
            }
            // todo: Should this be in the if block?
            keyNamesUsed.add(r.getConfigKey().getName());
        } catch (Exception e) {
            String message = String.format("Cannot set config key %s, could not coerce %s to %s",
                    r.getConfigKey(), r.getConfigKeyMaybeValue(), r.getConfigKey().getTypeToken());
            LOG.warn(message, e);
        }
    }

    private Optional<Object> joinOldAndNewSpecConfigValues(Optional<Object> oldValue,
                                                           Optional<Object> newValue) {
        Optional<Object> result;

        if ((newValue.isPresent()) && (oldValue.isPresent())) {
            result = Optional.of(
                    joinOldAndNewValues(oldValue.get(), newValue.get()));
        } else if (newValue.isPresent()) {
            result = newValue;
        } else if (oldValue.isPresent()) {
            result = oldValue;
        } else {
            result = Optional.absent();
        }
        return result;
    }

    private Object joinOldAndNewValues(Object oldValue, Object newValue) {
        if ((oldValue instanceof Map) && (newValue instanceof Map)) {
            return combineCurrentAndResolvedValueMaps((Map) oldValue, (Map) newValue);
        } else if ((oldValue instanceof List) && (newValue instanceof List)) {
            return combineCurrentAndResolvedValueList((List) oldValue, (List) newValue);
        } else {
            return newValue;
        }
    }


    private Map combineCurrentAndResolvedValueMaps(Map currentValue, Map resolvedVaue) {
        currentValue.putAll(resolvedVaue);
        return currentValue;
    }

    private List combineCurrentAndResolvedValueList(List currentValue, List resolvedVaue) {
        currentValue.addAll(resolvedVaue);
        return currentValue;
    }

    private Optional<Object> getConfigKeyValue(EntitySpec spec,
                                               FlagUtils.FlagConfigKeyAndValueRecord r) {
        Object configValue = spec.getConfig().get(r.getConfigKey());
        return (configValue == null)
                ? Optional.absent()
                : Optional.of(configValue);
    }

    private Optional<Object> findFlagValue(EntitySpec spec,
                                           FlagUtils.FlagConfigKeyAndValueRecord r) {
        Object flagValue = spec.getFlags().get(r.getFlagName());
        return (flagValue == null)
                ? Optional.absent()
                : Optional.of(flagValue);
    }


    private void setUnusedKeysAsAnonymousKeys(EntitySpec spec, Set<String> keyNamesUsed, ConfigBag bag) {
        // they aren't flags or known config keys, so must be passed as config keys in order for
        // EntitySpec to know what to do with them (as they are passed to the spec as flags)
        for (String key : MutableSet.copyOf(bag.getUnusedConfig().keySet())) {
            // we don't let a flag with the same name as a config key override the config key
            // (that's why we check whether it is used)
            if (!keyNamesUsed.contains(key)) {
                //Object transformed = new BrooklynComponentTemplateResolver.SpecialFlagsTransformer(loader).apply(bag.getStringKey(key));
                spec.configure(ConfigKeys.newConfigKey(Object.class, key.toString()), bag.getStringKey(key));
            }
        }
    }

    /**
     * Searches for config keys in the type, additional interfaces and the implementation (if specified)
     */
    private Collection<FlagUtils.FlagConfigKeyAndValueRecord> findAllFlagsAndConfigKeys(EntitySpec<? extends Entity> spec, ConfigBag bagFlags) {
        Set<FlagUtils.FlagConfigKeyAndValueRecord> allKeys = MutableSet.of();
        allKeys.addAll(FlagUtils.findAllFlagsAndConfigKeys(null, spec.getType(), bagFlags));
        if (spec.getImplementation() != null) {
            allKeys.addAll(FlagUtils.findAllFlagsAndConfigKeys(null, spec.getImplementation(), bagFlags));
        }
        for (Class<?> iface : spec.getAdditionalInterfaces()) {
            allKeys.addAll(FlagUtils.findAllFlagsAndConfigKeys(null, iface, bagFlags));
        }
        return allKeys;
    }

}
