package com.ssomar.executableevents.executableevents.activators;

import com.ssomar.executableevents.executableevents.ExecutableEvent;
import com.ssomar.executableevents.executableevents.manager.ExecutableEventsManager;
import com.ssomar.score.commands.activator.ActivatorCommandsRegistry;
import com.ssomar.score.features.custom.activators.activator.SActivator;
import com.ssomar.score.features.custom.activators.group.ActivatorsFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The commands of the PLAYER_WRITE_COMMAND activators with registerCommand: true, registered on the server by
 * SCore (no "Unknown command", tab completion). Synced at the next tick after a load, a reload, an editor save
 * or a deletion.
 */
public class RegisteredCommandActivators {

    public static final String OWNER = "ExecutableEvents";

    public static void requestSync() {
        try {
            ActivatorCommandsRegistry.getInstance().requestSync(OWNER, RegisteredCommandActivators::collect);
        } catch (Throwable ignored) {
            /* SCore without the registry (older SCore): the option has no effect */
        }
    }

    public static void unregisterAll() {
        try {
            ActivatorCommandsRegistry.getInstance().unregisterAll(OWNER);
        } catch (Throwable ignored) {
        }
    }

    public static Collection<String> collect() {
        List<String> result = new ArrayList<>();
        for (ExecutableEvent item : new ArrayList<>(ExecutableEventsManager.getInstance().getAllObjects())) {
            ActivatorsFeature activators = item.getActivators();
            if (activators == null || activators.getActivators() == null) continue;
            for (SActivator activator : new ArrayList<>(activators.getActivators().values())) {
                if (!(activator instanceof ActivatorEEFeature)) continue;
                ActivatorEEFeature a = (ActivatorEEFeature) activator;
                if (a.getRegisterCommand() == null || !a.getRegisterCommand().getValue()) continue;
                if (!Option.getOptionWithCommand().contains(a.getOption())) continue;
                result.addAll(a.getDetailedCommands().getValue());
            }
        }
        return result;
    }
}
