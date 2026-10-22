package com.ssomar.executableevents.executableevents.activators;

import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.editor.FeatureEditorInterface;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.utils.emums.TypeTarget;

public class ActivatorEEFeatureEditor extends FeatureEditorInterface<ActivatorEEFeature> {

    public ActivatorEEFeature activator;

    public ActivatorEEFeatureEditor(ActivatorEEFeature activator) {
        super(FeatureSettingsSCore.activator, 6 * 9);
        this.activator = activator.clone(activator.getParent());
        load();
    }

    @Override
    public void load() {
        clearAndSetBackground();

        activator.getDisplayName().initAndUpdateItemParentEditor(this, 0);
        activator.getOptionFeature().initAndUpdateItemParentEditor(this, 1);

        activator.getCooldown().initAndUpdateItemParentEditor(this, 2);
        activator.getUsePerDayFeature().initAndUpdateItemParentEditor(this, 4);

        if (activator.getOption().isLoopOption())
            activator.getLoopFeatures().initAndUpdateItemParentEditor(this, 5);
        else if(activator.getOption().isCustomTriggerOption())
            activator.getScheduleFeatures().initAndUpdateItemParentEditor(this, 5);
        else if (Option.getOptionWithDrops().contains(activator.getOption()))
            activator.getDesactiveDrops().initAndUpdateItemParentEditor(this, 5);

        if (Option.getOptionWithDetailedDamageCauses().contains(activator.getOption())) {
            activator.getDetailedDamageCauses().initAndUpdateItemParentEditor(this, 14);
        } else if (Option.getOptionWithCommand().contains(activator.getOption())) {
            activator.getDetailedCommands().initAndUpdateItemParentEditor(this, 14);
            activator.getRegisterCommand().initAndUpdateItemParentEditor(this, 12);
        }
        else if (Option.getOptionWithMessage().contains(activator.getOption())) {
            activator.getDetailedMessagesContains().initAndUpdateItemParentEditor(this, 14);
            activator.getDetailedMessagesEquals().initAndUpdateItemParentEditor(this, 23);
        } else if (Option.getOptionWithDetailedItems().contains(activator.getOption())) {
            activator.getDetailedItems().initAndUpdateItemParentEditor(this, 14);
        }
        else if (Option.getOptionWithDetailedEffects().contains(activator.getOption())) {
            activator.getDetailedEffects().initAndUpdateItemParentEditor(this, 14);
        }
        else if (Option.getOptionWithDetailedInventories().contains(activator.getOption())) {
            activator.getDetailedInventories().initAndUpdateItemParentEditor(this, 14);
            activator.getMustBeItsOwnInventory().initAndUpdateItemParentEditor(this, 23);
        }

        if (Option.getOptionWithConsoleOnlySt().contains(activator.getOption())) {
            activator.getConsoleCommands().initAndUpdateItemParentEditor(this, 8);
        } else if (Option.getOptionWithPlayerSt().contains(activator.getOption())) {
            activator.getPlayerConditions().initAndUpdateItemParentEditor(this, 7);
            activator.getPlayerCommands().initAndUpdateItemParentEditor(this, 8);
            activator.getCustomConditions().initAndUpdateItemParentEditor(this, 43);
        } else if (Option.getOptionWithEntitySt().contains(activator.getOption())) {
            activator.getEntityConditions().initAndUpdateItemParentEditor(this, 7);
            activator.getEntityCommands().initAndUpdateItemParentEditor(this, 8);
            activator.getDetailedEntities().initAndUpdateItemParentEditor(this, 6);
        } else if (Option.getOptionWithBlockSt().contains(activator.getOption())) {
            activator.getBlockConditions().initAndUpdateItemParentEditor(this, 7);
            activator.getBlockCommands().initAndUpdateItemParentEditor(this, 8);
            activator.getDetailedBlocks().initAndUpdateItemParentEditor(this, 6);
        }


        if (Option.getOptionWithOnlyTypeClick().contains(activator.getOption())) {
            activator.getTypeTargetFeature().initAndUpdateItemParentEditor(this, 10);
        } else if (Option.getOptionWithDetailedClick().contains(activator.getOption())) {
            activator.getDetailedClickFeature().initAndUpdateItemParentEditor(this, 10);
        }

        activator.getGlobalCooldown().initAndUpdateItemParentEditor(this, 11);

        if (Option.getOptionWithTargetEntitySt().contains(activator.getOption())) {
            activator.getTargetEntityCommands().initAndUpdateItemParentEditor(this, 17);
            activator.getTargetEntityConditions().initAndUpdateItemParentEditor(this, 16);
            activator.getDetailedTargetEntities().initAndUpdateItemParentEditor(this, 15);
        } else if (Option.getOptionWithTargetPlayerSt().contains(activator.getOption())) {
            activator.getTargetPlayerCommands().initAndUpdateItemParentEditor(this, 17);
            activator.getTargetPlayerConditions().initAndUpdateItemParentEditor(this, 16);
        } else if (
                (!Option.getOptionWithOnlyTypeClick().contains(activator.getOption()) && Option.getOptionWithTargetBlockSt().contains(activator.getOption())) ||
                        (Option.getOptionWithOnlyTypeClick().contains(activator.getOption())) && activator.getTypeTargetFeature().getValue().get().equals(TypeTarget.ONLY_BLOCK)
        ) {
            activator.getTargetBlockCommands().initAndUpdateItemParentEditor(this, 17);
            activator.getTargetBlockConditions().initAndUpdateItemParentEditor(this, 16);
            activator.getDetailedTargetBlocks().initAndUpdateItemParentEditor(this, 15);
        }

        activator.getRequiredGroup().initAndUpdateItemParentEditor(this, 21);

        activator.getWorldConditions().initAndUpdateItemParentEditor(this, 25);

        activator.getCancelEvent().initAndUpdateItemParentEditor(this, 27);

        //activator.getItemConditions().initAndUpdateItemParentEditor(this, 34);
        activator.getSilenceOutput().initAndUpdateItemParentEditor(this, 35);

        activator.getPlaceholderConditions().initAndUpdateItemParentEditor(this, 52);

        // Back
        createItem(RED, 1, 45, GUI.BACK, false, false);

        // Reset menu
        createItem(ORANGE, 1, 46, GUI.RESET, false, false, TM.gA(Text.EDITOR_RESET_DESCRIPTION));

        // change lang menu
        createItem(YELLOW, 1, 47, GUI.CHANGE_LANGUAGE, false, false, GeneralConfig.getInstance().getAvailableLocales("", "&e&oClick here to change the language"));


        // Save menu
        createItem(GREEN, 1, 53, GUI.SAVE, false, false, TM.gA(Text.EDITOR_SAVE_DESCRIPTION));
    }

    @Override
    public ActivatorEEFeature getParent() {
        return activator;
    }
}
