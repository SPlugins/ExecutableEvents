package com.ssomar.executableevents.executableevents;

import com.ssomar.executableevents.ExecutableEvents;
import com.ssomar.executableevents.configs.Message;
import com.ssomar.executableevents.configs.api.PlaceholderAPI;
import com.ssomar.executableevents.editor.ExecutableEventsEditor;
import com.ssomar.executableevents.executableevents.activators.ActivatorEEFeature;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import com.ssomar.score.configs.messages.MessageMain;
import com.ssomar.score.features.FeatureInterface;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.activators.activator.SActivator;
import com.ssomar.score.features.custom.activators.group.ActivatorsFeature;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.ColoredStringFeature;
import com.ssomar.score.features.types.MaterialFeature;
import com.ssomar.score.features.types.list.ListWorldFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.sobject.SObjectWithActivators;
import com.ssomar.score.sobject.SObjectWithFile;
import com.ssomar.score.sobject.SObjectWithFileEditable;
import com.ssomar.score.sobject.menu.NewSObjectsManagerEditor;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.FixedMaterial;
import com.ssomar.score.utils.messages.SendMessage;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
@Setter
public class ExecutableEvent extends SObjectWithFileEditable<ExecutableEvent, ExecutableEventEditor, ExecutableEventEditorManager> implements com.ssomar.score.api.executableevents.config.ExecutableEventInterface, SObjectWithActivators {

    /**
     * Features
     **/
    BooleanFeature enabled;
    ColoredStringFeature displayName;
    private MaterialFeature editorIcon;
    /**
     * Activators / triggers
     **/
    private ActivatorsFeature activatorsFeature;

    private ListWorldFeature disabledWorlds;


    /**
     * For the clone method, the parent is the real instance
     **/
    public ExecutableEvent(FeatureParentInterface parent, String id, String path) {
        super(ExecutableEvents.plugin, id, parent, FeatureSettingsSCore.EXECUTABLEEVENT, path, ExecutableEventLoader.getInstance());
        reset();
    }

    public ExecutableEvent(String id, String path) {
        super(ExecutableEvents.plugin, id, null, FeatureSettingsSCore.EXECUTABLEEVENT, path, ExecutableEventLoader.getInstance());
        reset();
    }

    @Override
    public boolean isPremium() {
        return !ExecutableEvents.plugin.isLotOfWork();
    }

    @Override
    public ItemStack getIconItem() {
        return new ItemStack(editorIcon.getValue().get());
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§7ID: §f" + getId());
        description.add("§7Enabled: §f" + enabled.getValue());
        ;
        description.add("§7Path: §f" + getPath());
        description.add("§7Activators: ");
        for (SActivator activator : activatorsFeature.getActivators().values()) {
            description.add("§7- " + activator.getId());
        }
        return description;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<FeatureInterface>(Arrays.asList(enabled, editorIcon, displayName, disabledWorlds, activatorsFeature));

        return features;
    }

    @Override
    public String getParentInfo() {
        return "(Event: " + getId() + ")";
    }


    @Override
    public void reload() {
        /* an activator may have been added, edited or deleted in the editor: registerCommand */
        com.ssomar.executableevents.executableevents.activators.RegisteredCommandActivators.requestSync();
        if (getParent() instanceof ExecutableEvent) {
            ExecutableEvent item = (ExecutableEvent) getParent();
            item.setEnabled(enabled);
            item.setEditorIcon(editorIcon);
            item.setActivatorsFeature(activatorsFeature);
            item.setDisplayName(displayName);
            item.setDisabledWorlds(disabledWorlds);
        }
    }

    @Override
    public ExecutableEvent clone(FeatureParentInterface parent) {
        ExecutableEvent clone = new ExecutableEvent(this, getId(), getPath());
        clone.setEnabled(enabled.clone(clone));
        clone.setEditorIcon(editorIcon.clone(clone));
        clone.setActivatorsFeature(activatorsFeature.clone(clone));
        clone.setDisplayName(displayName.clone(clone));
        clone.setDisabledWorlds(disabledWorlds.clone(clone));

        return clone;
    }


    @Override
    public void openEditor(@NotNull Player player) {
        ExecutableEventEditorManager.getInstance().startEditing(player, this);
    }

    @Override
    public void openBackEditor(@NotNull Player player) {
        NewSObjectsManagerEditor.getInstance().startEditing(player, new ExecutableEventsEditor());
    }

    @Override
    public List<String> load(SPlugin sPlugin, ConfigurationSection configurationSection, boolean premiumLoading) {
        List<String> errors = new ArrayList<>();
        for (FeatureInterface feature : getFeatures()) {
            errors.addAll(feature.load(sPlugin, configurationSection, premiumLoading));
        }

        return errors;
    }

    @Override
    public void save(ConfigurationSection configurationSection) {
        for (FeatureInterface feature : getFeatures()) {
            feature.save(configurationSection);
        }
    }

    @Override
    public ExecutableEvent getValue() {
        return this;
    }

    @Override
    public ExecutableEvent initItemParentEditor(GUI gui, int i) {
        return null;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public void reset() {
        this.enabled = new BooleanFeature(this,  true, FeatureSettingsSCore.enabled);

        this.editorIcon = new MaterialFeature(this,  Optional.of(Material.LEVER), FeatureSettingsSCore.editorIcon, true);

        this.activatorsFeature = new ActivatorsFeature(this, new ActivatorEEFeature(null, "null"));

        this.displayName = new ColoredStringFeature(this,  Optional.of("&eDefault name"), FeatureSettingsSCore.name);

        this.disabledWorlds = new ListWorldFeature(this,  new ArrayList<>(), FeatureSettingsSCore.disabledWorlds);
    }

    @Override
    public @Nullable SActivator getActivator(String s) {
        for (SActivator acti : activatorsFeature.getActivators().values()) {
            if (acti.getId().equalsIgnoreCase(getId())) {
                return acti;
            }
        }
        return null;
    }

    @Override
    public ActivatorsFeature getActivators() {
        return activatorsFeature;
    }


    public ItemStack buildItem(int quantity, Optional<Player> playerOpt, Optional<Integer> usageOpt, Optional<ItemStack> itemStackOpt, Map<String, String> variables) {
        return new ItemStack(editorIcon.getValue().get());
    }


    public void addCooldown(Player player,int cooldown, boolean isInTicks) {
        for (SActivator activator : activatorsFeature.getActivators().values()) {
            if (activator instanceof ActivatorEEFeature) {
                ActivatorEEFeature activatorEE = (ActivatorEEFeature) activator;
                SsomarDev.testMsg("addCooldown: "+player.getName()+" "+this.getId()+" "+cooldown+" "+isInTicks, true);
                activatorEE.getCooldown().addCooldown(player, this, cooldown, isInTicks);
            }
        }
    }

    public void addCooldown(Player player, int cooldown, boolean isInTicks, String activatorId) {
        for (SActivator activator : activatorsFeature.getActivators().values()) {
            if (activator instanceof ActivatorEEFeature) {
                if (activatorId.equalsIgnoreCase(activator.getId())) {
                    ActivatorEEFeature activatorEE = (ActivatorEEFeature) activator;
                    activatorEE.getCooldown().addCooldown(player, this, cooldown, isInTicks);
                    break;
                }
            }
        }
    }

    public void addGlobalCooldown(int i, boolean b) {
        for (SActivator activator : activatorsFeature.getActivators().values()) {
            if (activator instanceof ActivatorEEFeature) {
                ActivatorEEFeature activatorEI = (ActivatorEEFeature) activator;
                activatorEI.getCooldown().addGlobalCooldown(i, b);
            }
        }
    }

    public void addGlobalCooldown(int i, boolean b, String activatorId) {
        for (SActivator activator : activatorsFeature.getActivators().values()) {
            if (activator instanceof ActivatorEEFeature) {
                if (activatorId.equalsIgnoreCase(activator.getId())) {
                    ActivatorEEFeature activatorEE = (ActivatorEEFeature) activator;
                    activatorEE.getCooldown().addGlobalCooldown(i, b);
                    break;
                }
            }
        }
    }

    public boolean hasCustomModel() {
        return false;
    }
}
