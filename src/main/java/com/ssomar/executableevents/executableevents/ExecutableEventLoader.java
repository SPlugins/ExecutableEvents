package com.ssomar.executableevents.executableevents;

import com.ssomar.executableevents.ExecutableEvents;
import com.ssomar.executableevents.configs.GeneralConfig;
import com.ssomar.executableevents.configs.api.PlaceholderAPI;
import com.ssomar.executableevents.executableevents.manager.ExecutableEventsManager;
import com.ssomar.executableevents.utils.ZipUtility;
import com.ssomar.score.events.loop.LoopManager;
import com.ssomar.score.sobject.SObjectWithFileLoader;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExecutableEventLoader extends SObjectWithFileLoader<ExecutableEvent> {

    private static ExecutableEventLoader instance;
    @Getter
    @Setter
    private boolean createBackup;


    public ExecutableEventLoader() {
            super(ExecutableEvents.plugin, "/com/ssomar/executableevents/configs/events/", ExecutableEventsManager.getInstance(), ExecutableEvents.plugin.getMaxSObjectsLimit());
        createBackup = false;
    }

    public static ExecutableEventLoader getInstance() {
        if (instance == null) instance = new ExecutableEventLoader();
        return instance;
    }

    @Override
    public void load() {
        LoopManager.getInstance(ExecutableEvents.plugin.getPlugin()).resetLoopActivators(ExecutableEvents.plugin);
        ExecutableEventsManager.getInstance().setDefaultObjects(new ArrayList<>());
        //if (!GeneralConfig.getInstance().isDisableTestItems()) {
        if (PlaceholderAPI.isLotOfWork()) {
            this.loadDefaultPremiumObjects();
        }
        //this.loadDefaultEncodedPremiumObjects();
        //}

        // ITEMS CONFIG
        ExecutableEventsManager.getInstance().setLoadedObjects(new ArrayList<>());
        /* also when no event is loaded anymore (setLoadedObjects does not notify the manager) */
        ExecutableEventsManager.getInstance().onLoadedObjectsChanged();

        this.resetCpt();
        File itemsDirectory;
        if ((itemsDirectory = new File(ExecutableEvents.plugin.getPlugin().getDataFolder() + "/events")).exists()) {
            /* create backup at each restart / reload but not at /ei reload */
            if (createBackup && !GeneralConfig.getInstance().getBooleanSetting(GeneralConfig.Setting.disableBackup.name())) {
                ZipUtility zipUtility = new ZipUtility();
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");
                    LocalDateTime now = LocalDateTime.now();
                    new File(ExecutableEvents.plugin.getPlugin().getDataFolder() + "/backups/" + dtf.format(now)).mkdirs();
                    zipUtility.zip(new String[]{itemsDirectory.getPath()}, ExecutableEvents.plugin.getPlugin().getDataFolder() + "/backups/" + dtf.format(now) + "/backup.zip");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                createBackup = false;
            }

            this.loadObjectsInFolder(itemsDirectory, !PlaceholderAPI.isLotOfWork());
            Utils.sendConsoleMsg(ExecutableEvents.NAME_COLOR + " &7Amount of Executable Events configurations loaded: &e" + getCpt());
        } else {
            itemsDirectory.mkdirs();
            this.createDefaultObjectsFile(!PlaceholderAPI.isLotOfWork());
            this.load();
        }
    }

    @Override
    public void configVersionsConverter(File file) {
        // disabled
        ConfigConverter.updateTo(file);
    }

    @Override
    public Optional<ExecutableEvent> getObject(FileConfiguration itemConfig, String id, boolean showError, boolean isPremiumLoading, String path) {

        List<String> errors = new ArrayList<>();
        ExecutableEvent item = new ExecutableEvent(id, path);
        errors.addAll(item.load(ExecutableEvents.plugin, itemConfig, isPremiumLoading));

        if (showError) {
            for (String s : errors) {
                Utils.sendConsoleMsg(ExecutableEvents.NAME_COLOR + " " + StringConverter.coloredString(s));
            }
        }
        return Optional.ofNullable(item);
    }

}
