package com.ssomar.executableevents.executableevents.activators;

import com.ssomar.executableevents.ExecutableEvents;
import com.ssomar.executableevents.events.EventsManager;
import com.ssomar.executableevents.executableevents.ExecutableEvent;
import com.ssomar.executableevents.executableevents.manager.ExecutableEventsManager;
import com.ssomar.score.SCore;
import com.ssomar.score.features.custom.activators.activator.SActivator;
import com.ssomar.score.features.types.list.ListDetailedEntityFeature;
import com.ssomar.score.sobject.sactivator.EventInfo;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.*;

/**
 * LOOP_ENTITY: on each cycle of the LoopManager (same delay / delayInTick as LOOP_SERVER), the activators run once for each
 * entity of the loaded worlds that passes their filters. Players (they have the player activators) and NPCs are skipped.
 * <p>
 * The activators due on the same cycle share one pass over the entities of each world. The cheap filters (world, entity type
 * of detailedEntities) are checked before anything is built for the entity, the rest (entityConditions, cooldown per entity,
 * placeholders, entityCommands) is the usual run of the activator.
 */
public class LoopEntityActivators {

    /* Real Folia (or a fork): the entities live on region threads. SCore.isFolia() is also true on Paper 1.20+, where the
     * loop task already runs on the main thread and the region tasks would only delay the activations and cost a task each */
    private static final boolean REGIONIZED = hasClass("io.papermc.paper.threadedregions.RegionizedServer");

    /* Activators already warned for their missing type filter (once per server run, not on each reload) */
    private static final Set<String> noFilterWarned = Collections.synchronizedSet(new HashSet<>());

    /**
     * Called when the activator is loaded: a LOOP_ENTITY without entity type runs on every entity of the server on each cycle
     */
    public static void warnIfNoTypeFilter(ActivatorEEFeature activator) {
        if (activator.getOption() != Option.LOOP_ENTITY) return;
        if (!acceptsAllTypes(activator.getDetailedEntities())) return;
        String key = activator.getParentObjectId() + ":" + activator.getId();
        if (!noFilterWarned.add(key)) return;
        ExecutableEvents.plugin.getPlugin().getLogger().warning("LOOP_ENTITY: the activator " + activator.getId() + " of the event "
                + activator.getParentObjectId() + " has no entity type in detailedEntities, it runs on every entity of the server on each loop."
                + " Add the types you need in detailedEntities (ex: ZOMBIE) to keep it cheap.");
    }

    private static boolean acceptsAllTypes(ListDetailedEntityFeature detailedEntities) {
        for (String s : detailedEntities.getWhiteListValue()) {
            if (!s.equals("*")) return false;
        }
        return true;
    }

    public static void run(List<SActivator> activators, Event loopEvent) {
        /* The ExecutableEvent of each activator, resolved once per cycle */
        List<Target> targets = new ArrayList<>();
        for (ExecutableEvent executableEvent : ExecutableEventsManager.getInstance().getAllObjects()) {
            if (!executableEvent.getEnabled().getValue()) continue;
            for (SActivator activator : executableEvent.getActivators().getActivators(Option.LOOP_ENTITY, Collections.emptyList(), activators)) {
                targets.add(new Target(executableEvent, (ActivatorEEFeature) activator));
            }
        }
        if (targets.isEmpty()) return;

        for (World world : Bukkit.getWorlds()) {
            /* disabledWorlds of the config and of the ExecutableEvent: checked once per world, not per entity */
            List<Target> worldTargets = new ArrayList<>(targets.size());
            for (Target target : targets) {
                if (EventsManager.getInstance().isValidWorld(world, target.executableEvent)) worldTargets.add(target);
            }
            if (worldTargets.isEmpty()) continue;

            if (REGIONIZED) {
                /* Folia: the entities of the world are read on its region, each activation runs on the region of its entity */
                SCore.schedulerHook.runLocationTask(() -> runWorld(world, worldTargets, loopEvent, true), world.getSpawnLocation(), 0);
            } else runWorld(world, worldTargets, loopEvent, false);
        }
    }

    private static void runWorld(World world, List<Target> targets, Event loopEvent, boolean folia) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player) continue;
            EntityType type = entity.getType();
            List<Target> valid = null;
            for (Target target : targets) {
                if (!target.isValidType(type)) continue;
                if (target.needFullCheck && !target.activator.getDetailedEntities().isValidEntity(entity)) continue;
                if (valid == null) {
                    /* NPC checked only for the entities that an activator wants */
                    if (entity.hasMetadata("NPC")) break;
                    valid = new ArrayList<>(2);
                }
                valid.add(target);
            }
            if (valid == null) continue;

            if (folia) {
                final List<Target> toRun = valid;
                SCore.schedulerHook.runEntityTaskAsap(() -> runEntity(entity, world, toRun, loopEvent), null, entity);
            } else runEntity(entity, world, valid, loopEvent);
        }
    }

    private static void runEntity(Entity entity, World world, List<Target> targets, Event loopEvent) {
        if (!entity.isValid()) return;
        for (Target target : targets) {
            EventInfo eInfo = new EventInfo(loopEvent);
            eInfo.setOption(Option.LOOP_ENTITY);
            eInfo.setEntity(Optional.of(entity));
            eInfo.setWorld(Optional.of(world));
            target.activator.runWithException(target.executableEvent, eInfo);
        }
    }

    private static boolean hasClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static class Target {
        private final ExecutableEvent executableEvent;
        private final ActivatorEEFeature activator;
        /* Entity types of the whitelist of detailedEntities, null = no filter on the type */
        private final Set<EntityType> types;
        /* Blacklist, NBT tags or MythicMobs: isValidEntity must be called for the entities that pass the type */
        private final boolean needFullCheck;

        private Target(ExecutableEvent executableEvent, ActivatorEEFeature activator) {
            this.executableEvent = executableEvent;
            this.activator = activator;

            ListDetailedEntityFeature detailedEntities = activator.getDetailedEntities();
            boolean hasMythicMobs = !detailedEntities.extractWhiteListMMCondition().isEmpty() || !detailedEntities.extractBlackListMMCondition().isEmpty();
            boolean hasTagsOrBlacklist = !detailedEntities.getBlackListValue().isEmpty();
            for (String s : detailedEntities.getWhiteListValue()) {
                if (s.contains("{")) hasTagsOrBlacklist = true;
            }
            this.needFullCheck = hasMythicMobs || hasTagsOrBlacklist;

            /* A MythicMob can be of any type: no filter on the type, isValidEntity decides */
            if (acceptsAllTypes(detailedEntities) || !detailedEntities.extractWhiteListMMCondition().isEmpty()) this.types = null;
            else {
                Set<EntityType> set = EnumSet.noneOf(EntityType.class);
                set.addAll(detailedEntities.extractWhiteListCondition().keySet());
                this.types = set;
            }
        }

        private boolean isValidType(EntityType type) {
            return types == null || types.contains(type);
        }
    }
}
