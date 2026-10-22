package com.ssomar.executableevents;


import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;
import com.ssomar.executableevents.api.load.ExecutableEventsPostLoadEvent;
import com.ssomar.executableevents.executableevents.manager.ExecutableEventsManager;
import com.ssomar.executableevents.commands.CommandsClass;
import com.ssomar.executableevents.configs.GeneralConfig;
import com.ssomar.executableevents.configs.Message;
import com.ssomar.executableevents.events.EventsHandler;
import com.ssomar.executableevents.events.optimize.OptimizedEventsHandler;
import com.ssomar.executableevents.executableevents.ExecutableEventLoader;
import com.ssomar.score.SCore;
import com.ssomar.score.configs.messages.MessageInterface;
import com.ssomar.score.configs.messages.MessageMain;
import com.ssomar.score.usedapi.Dependency;
import com.ssomar.score.utils.logging.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;


public class ExecutableEvents extends JavaPlugin {

    public static final String NAME = "ExecutableEvents";
    public static final String NAME_COLOR = "&dExecutableEvents";

    public static SExecutableEvents plugin;
    private CommandsClass commandClass;

    public static ClassLoader scoreClassLoader;

    public static SExecutableEvents getPluginSt() {
        return plugin;
    }

    @Override
    public void onLoad() {
        if(Bukkit.getPluginManager().getPlugin("SCore") == null) {
            BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);
            bukkitLibraryManager.addJitPack();

            String scoreVersion = "main-2ff812dab0-1";

            Library lib = Library.builder()
                    .groupId("com{}github{}Ssomar-Developement") // "{}" is replaced with ".", useful to avoid unwanted changes made by maven-shade-plugin
                    .artifactId("SCore")
                    .version(scoreVersion)
                    .resolveTransitiveDependencies(true)
                    .build();

            bukkitLibraryManager.loadLibrary(lib);

            try {
                URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new File(this.getDataFolder() + "/lib/com/github/Ssomar-Developement/SCore/" + scoreVersion + "/SCore-" + scoreVersion + ".jar").toURI().toURL()});
                scoreClassLoader = urlClassLoader;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public void onEnable() {
        if(!Dependency.SCORE.isInstalled()) {
            SCore.initLibPartOfSCore(this, scoreClassLoader);
        }
        else if(!Dependency.SCORE.isEnabled()) {
            Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin("SCore") );
        }
        plugin = new SExecutableEvents(this);
        sendPluginName();

        /* Expose the implementation through the public SCore API */
        com.ssomar.score.api.executableevents.ExecutableEventsAPI.register(ExecutableEventsManager.getInstance());

        commandClass = new CommandsClass(plugin);
        plugin.getPlugin().saveDefaultConfig();

        if (plugin.isLotOfWork() && SCore.is1v11Less()) {
            ExecutableEvents.plugin.getPlugin().getServer().getLogger().severe(ExecutableEvents.plugin.getNameDesign() + " ExecutableEvents for 1.11 and less is only for the premium version !");
            sendPluginName();
            Bukkit.getServer().getPluginManager().disablePlugin(ExecutableEvents.plugin.getPlugin());
            return;
        }

        /* Events instance part */
        EventsHandler.getInstance().setup(this);

        /* Load of the general config */
        GeneralConfig.getInstance();

        /* Delete old backups */
        File backupfolder;
        if ((backupfolder = new File(plugin.getPlugin().getDataFolder() + "/backups")).exists()) {
            if (backupfolder.isDirectory()) {
                for (File f : backupfolder.listFiles()) {
                    try {
                        String name = f.getName();
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");
                        LocalDateTime now = LocalDateTime.now();
                        if (LocalDateTime.from(dtf.parse(name)).plusDays(GeneralConfig.getInstance().getIntSetting(GeneralConfig.Setting.deleteBackupsAfterDays.name(), 30)).isBefore(now)) {
                            //System.out.println("DELETE BACKUP: " + f.getName());
                            for (File f2 : f.listFiles()) {
                                f2.delete();
                            }
                            f.delete();
                        }
                        //else System.out.println("NOT DELETE BACKUP: " + f.getName());
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        }

        /* Items parts */
        /* To create backup at each restart / reload but not at /ei reload */
        ExecutableEventLoader.getInstance().setCreateBackup(true);
        ExecutableEventLoader.getInstance().load();

        MessageMain.getInstance().loadMessagesOf(plugin.getPlugin(), new ArrayList<>(Arrays.asList(Message.values())));

        /* Commands part */
        this.getCommand("ee").setExecutor(commandClass);

        /* BStats part */
        int pluginId = 16889;
        @SuppressWarnings("unused")
        MetricsLite metrics = new MetricsLite(this, pluginId);

        sendPluginName();

        ExecutableEventsManager.getInstance().setLoaded(true);
        /* Legacy plugin-side event, kept for backward compatibility */
        Bukkit.getPluginManager().callEvent(new ExecutableEventsPostLoadEvent());
        /* Public SCore API event */
        Bukkit.getPluginManager().callEvent(new com.ssomar.score.api.executableevents.load.ExecutableEventsPostLoadEvent(ExecutableEventsManager.getInstance()));
    }

    public void onReload(boolean PluginCommand) {
        sendPluginName();
        plugin.getPlugin().saveDefaultConfig();

        OptimizedEventsHandler.getInstance().reload();

        /* Items instance part */
        ExecutableEventLoader.getInstance().reload();

        GeneralConfig.getInstance().reload();

        /* Message instance part */
        MessageMain.getInstance().loadMessagesOf(plugin.getPlugin(), new ArrayList<>(Arrays.asList(Message.values())));

        if (PluginCommand) {
        }

        //OptimizedEventsVerification.getInstance().displayOptimisation();

        sendPluginName();
    }

    public void sendPluginName() {
       if (plugin.isLotOfWork())
            Utils.sendConsoleMsg("&7================ " + NAME_COLOR + " &7================");
        else Utils.sendConsoleMsg("&7========&e*&7======== " + NAME_COLOR + " &7========&e*&7========");
    }

    @Override
    public void onDisable() {
        try {
            /* the commands registered for the activators (registerCommand) */
            com.ssomar.executableevents.executableevents.activators.RegisteredCommandActivators.unregisterAll();
        } catch (Throwable ignored) {
        }
        try {
            if (SCore.plugin.isEnabled()) SCore.plugin.getServer().getPluginManager().disablePlugin(SCore.plugin);
        } catch (Error ignored) {
            //Probably already disabled
            //System.out.println("SCore not found");
        }
    }


}
