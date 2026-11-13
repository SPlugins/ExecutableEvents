package com.ssomar.executableevents.commands;


import com.ssomar.executableevents.ExecutableEvents;
import com.ssomar.executableevents.SExecutableEvents;
import com.ssomar.executableevents.configs.api.PlaceholderAPI;
import com.ssomar.executableevents.editor.ExecutableEventsEditor;
import com.ssomar.executableevents.events.EventsManager;
import com.ssomar.executableevents.events.optimize.OptimizedEventsHandler;
import com.ssomar.executableevents.executableevents.ExecutableEvent;
import com.ssomar.executableevents.executableevents.ExecutableEventLoader;
import com.ssomar.executableevents.executableevents.manager.ExecutableEventsManager;
import com.ssomar.score.SCore;
import com.ssomar.score.actionbar.ActionbarCommands;
import com.ssomar.score.commands.score.CommandsClassAbstract;
import com.ssomar.score.commands.score.PathCommand;
import com.ssomar.score.commands.score.clear.ClearCommand;
import com.ssomar.score.commands.score.clear.ClearType;
import com.ssomar.score.sobject.menu.NewSObjectsManagerEditor;
import com.ssomar.score.sobject.sactivator.EventInfo;
import com.ssomar.score.sobject.sactivator.OptionGlobal;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.messages.SendMessage;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandsClass extends CommandsClassAbstract<SExecutableEvents> {


    private PathCommand<SExecutableEvents, ExecutableEventsManager, ExecutableEvent> pathCommand;

    public CommandsClass(SExecutableEvents main) {
        super(main);

        addCommand("reload");
        addCommand("enable");
        addCommand("disable");
        addCommand("debug");
        addCommand("checkevents");
        addCommand("default_events");
        addCommand("create");
        addCommand("show");
        addCommand("editor");
        addCommand("edit");
        addCommand("clear");
        addCommand("actionbar");
        addCommand("delete");
        addCommand("run-custom-trigger");

        pathCommand = new PathCommand<>(getSPlugin(), ExecutableEventsManager.getInstance());
        addCommands(pathCommand.getCommands());
    }


    public void runCommand(CommandSender sender, @Nullable  Player player, String command, String[] args, String typedCommand) {

        switch (command) {

            case "reload":
                if (args.length < 1) {
                    getSPlugin().getPlugin().onReload(true);
                    getSm().sendMessage(sender, getSPlugin().getNameDesign() + " &7has been reloaded !");
                    Utils.sendConsoleMsg(getSPlugin().getNameDesign() + " &7Successfully reloaded !");
                    break;
                } else {
                    String idToReload = args[0];

                    if (idToReload.contains("folder:")) {
                        String folderName = idToReload.replace("folder:", "");
                        if (ExecutableEventLoader.getInstance().reloadFolder(folderName))
                            getSm().sendMessage(sender, getSPlugin().getNameDesign() + " &7The folder &e" + folderName + " &7has been reloaded !");
                        else
                            getSm().sendMessage(sender, "&c"+getSPlugin().getNameWithBrackets()+" &cThe folder &6" + folderName + " &cdoes not exist !");
                        /* without it the folder reload went on into enable/disable and disabled every event of the folder */
                        break;
                    } else {
                        if (ExecutableEventsManager.getInstance().isValidID(idToReload)) {
                            ExecutableEventsManager.getInstance().reloadObject(idToReload);
                            getSm().sendMessage(sender, getSPlugin().getNameDesign()+" &e" + idToReload + " &7has been reloaded !");
                            break;
                        } else {
                            getSm().sendMessage(sender, StringConverter.coloredString("&c"+getSPlugin().getNameWithBrackets()+" &cThe ID &6" + idToReload + " &cspecified is not valid !"));
                            break;
                        }
                    }
                }
            case "enable":
            case "disable":
                if (args.length < 1) {
                    getSm().sendMessage(sender, StringConverter.coloredString(getSPlugin().getNameDesign()+" &7To create enable or disable an event &e/ee enable/disable {eventId}"));
                    break;
                } else {
                    String idToEnableOrDisable = args[0];

                    List<ExecutableEvent> eventsToChange = new ArrayList<>();
                    if (idToEnableOrDisable.contains("folder:")) {
                        String folderName = idToEnableOrDisable.replace("folder:", "");
                        eventsToChange.addAll(ExecutableEventLoader.getInstance().getAllLoadedObjectsOfFolder(folderName));
                    } else {
                        ExecutableEventsManager.getInstance().getLoadedObjectWithID(idToEnableOrDisable).ifPresent(eventsToChange::add);
                    }

                    boolean enable = command.equals("enable");
                    for (ExecutableEvent event : eventsToChange) {
                        event.getEnabled().setValue(enable);
                        if (enable)
                            getSm().sendMessage(sender,getSPlugin().getNameDesign()+" &e" + event.getId() + " &7is now &eenabled !");
                        else
                            getSm().sendMessage(sender, getSPlugin().getNameDesign()+" &e" + event.getId() + " &7is now &cdisabled !");
                    }

                }
                break;
            case "debug":
                if (player != null) {
                    if (DebugMode.getInstance().getPlayersInDebugMode().contains(player)) {
                        DebugMode.getInstance().getPlayersInDebugMode().remove(player);
                        getSm().sendMessage(sender,getSPlugin().getNameDesign()+" &7debug mode &cdisabled &7!");
                    } else {
                        DebugMode.getInstance().getPlayersInDebugMode().add(player);
                        getSm().sendMessage(sender,getSPlugin().getNameDesign()+" &7debug mode &aenabled &7!");
                    }
                } else {
                    if (DebugMode.getInstance().isEnabledInConsole()) {
                        DebugMode.getInstance().setEnabledInConsole(false);
                        getSm().sendMessage(sender,getSPlugin().getNameDesign()+" &7debug mode &cdisabled &7!");
                    } else {
                        DebugMode.getInstance().setEnabledInConsole(true);
                        getSm().sendMessage(sender,getSPlugin().getNameDesign()+" &7debug mode &aenabled &7!");
                    }
                }
                break;
            case "checkevents":
                OptimizedEventsHandler.getInstance().displayOptimisation();
                getSm().sendMessage(sender, getSPlugin().getNameDesign()+" &7Check events sent in your console !");
                break;
            case "default_events":
                ExecutableEventLoader.getInstance().createDefaultObjectsFile(!PlaceholderAPI.isLotOfWork(), true);
                getSPlugin().getPlugin().onReload(true);
                getSm().sendMessage(sender, getSPlugin().getNameDesign()+" &7regenerates the default items, check them in the &e/ee show &7!");
                break;

            case "create":
                if (player != null) {
                    if (args.length == 1) {
                        if (ExecutableEventLoader.getInstance().getAllObjectsLowerCases().contains(args[0])) {
                            getSm().sendMessage(sender, "&c"+getSPlugin().getNameWithBrackets()+" &cError this id already exist re-enter &6/ee create ID &7(ID is the id you want for your new event)");
                        } else {
                            if (ExecutableEventsManager.getInstance().getLoadedObjects().size() >= ExecutableEvents.plugin.getMaxSObjectsLimit() && PlaceholderAPI.isLotOfWork()) {
                                getSm().sendMessage(sender, "&c"+getSPlugin().getNameWithBrackets()+" &cREQUIRE PREMIUM: to add more than &6" + ExecutableEvents.plugin.getMaxSObjectsLimit() + " &7events you need the premium version");
                                return;
                            }

                            ExecutableEvent i = new ExecutableEvent(args[0], "plugins/ExecutableEvents/events/" + args[0] + ".yml");
                            i.save();
                            ExecutableEventsManager.getInstance().addLoadedObject(i);
                            i.openEditor(player);
                        }
                    } else {
                        getSm().sendMessage(sender, StringConverter.coloredString(getSPlugin().getNameDesign()+" &7To create a new event you need to enter &e/ee create ID &7(ID is the id you want for your new event)"));
                    }
                }
                break;
            case "show":
            case "editor":
                if (player != null) {
                    NewSObjectsManagerEditor.getInstance().startEditing(player, new ExecutableEventsEditor());
                }
                break;
            case "edit":
                if (player != null) {
                    if (args.length > 0) {
                        try {
                            Optional<ExecutableEvent> ei = ExecutableEventsManager.getInstance().getLoadedObjectWithID(args[0]);
                            if (ei.isPresent()) {
                                ei.get().openEditor(player);
                            } else {
                                getSm().sendMessage(sender, StringConverter.coloredString("&c"+getSPlugin().getNameWithBrackets()+" &cThis ExecutableEvent &6" + args[0] + " &cdoesn't exist"));
                            }
                        } catch (NullPointerException e) {
                            getSm().sendMessage(sender, StringConverter.coloredString("&c"+getSPlugin().getNameWithBrackets()+" &cError > Invalid EventID &6/ee edit {EventID}"));
                        }
                    } else
                        getSm().sendMessage(sender, StringConverter.coloredString("&c"+getSPlugin().getNameWithBrackets()+" &cError > Invalid EventID &6/ee edit {EventID}"));
                }
                break;

            case "clear":
                ClearCommand.clearCmd(ExecutableEvents.plugin, sender, args);
                break;

            case "actionbar":
                if (sender instanceof Player) {
                    if (args.length == 1) {
                        ActionbarCommands.manageCommand((Player) sender, args[0]);
                    } else
                        getSm().sendMessage(sender, StringConverter.coloredString("&c"+getSPlugin().getNameWithBrackets()+" &6/ee actionbar on &cor &6/ee actionbar off"));
                } else
                    getSm().sendMessage(sender, StringConverter.coloredString("&c"+getSPlugin().getNameWithBrackets()+" &cThis is only an In-game command !"));
                break;
            case "delete":
                if (args.length == 2) {

                    if (!args[1].equalsIgnoreCase("confirm")) {
                        getSm().sendMessage(sender, StringConverter.coloredString("&c"+getSPlugin().getNameWithBrackets()+" &cTo confirm the delete type &6/ee delete {eventID} confirm"));
                        return;
                    }
                    Optional<ExecutableEvent> eiOpt = ExecutableEventsManager.getInstance().getLoadedObjectWithID(args[0]);
                    if (eiOpt.isPresent()) {
                        ExecutableEventsManager.getInstance().deleteObject(args[0]);
                        getSm().sendMessage(sender, StringConverter.coloredString(getSPlugin().getNameDesign()+" &7Event &e" + args[0] + " &7deleted"));
                        if (player != null)
                            NewSObjectsManagerEditor.getInstance().startEditing(player, new ExecutableEventsEditor());
                    } else
                        getSm().sendMessage(sender, StringConverter.coloredString("&c"+getSPlugin().getNameWithBrackets()+" &cEvent &6" + args[0] + " &cnot found"));
                } else
                    getSm().sendMessage(sender, StringConverter.coloredString("&c"+getSPlugin().getNameWithBrackets()+" &cTo confirm the delete type &6/ee delete {eventID} confirm"));
                break;

            case "run-custom-trigger":

                List<String> trigger = new ArrayList<>();

                Map<String, String> otherArgs = new HashMap<>();
                for(int i = 0; i < args.length; i++) {
                    if(args[i].startsWith("trigger:")) {
                        // List represented by [] and ,
                        String triggerStr = args[i].replace("trigger:", "");
                        if(triggerStr.startsWith("[") && triggerStr.endsWith("]")){
                            triggerStr = triggerStr.replace("[", "");
                            triggerStr = triggerStr.replace("]", "");
                            String[] triggers = triggerStr.split(",");
                            for (String t : triggers) {
                                trigger.add(t);
                            }
                        }
                        else trigger.add(triggerStr);
                    }
                    else otherArgs.put("%arg"+otherArgs.size()+"%", args[i]);
                }
                String allArgs = "";
                for(String arg : args) {
                    allArgs += arg + " ";
                }
                // remove the last space
                if(!allArgs.isEmpty()) allArgs = allArgs.substring(0, allArgs.length()-1);
                otherArgs.put("%all_args%", allArgs);

                EventInfo eInfo = new EventInfo(null);
                eInfo.setOption(OptionGlobal.CUSTOM_TRIGGER);
                eInfo.setWhitelistActivatorsId(trigger);
                eInfo.setPlaceholders(otherArgs);

                EventsManager.getInstance().activeOption(eInfo);

                break;
            default:
                pathCommand.run(sender, command, args, typedCommand);
                break;
        }
    }

    @Override
    public List<String> getOnTabCompleteArguments(CommandSender sender, Command command, String label, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (args.length >= 2) {

            switch (args[0]) {

                case "clear":
                    if (args.length == 3) {
                        for (ClearType type : ClearType.values()) {
                            arguments.add(type.name());
                        }
                        return arguments;
                    }
                    break;
                case "edit":
                    if (args.length == 2) {
                        return ExecutableEventsManager.getInstance().getLoadedObjectsWith(args[1]);
                    }
                    break;
                case "enable":
                case "disable":
                    if (args.length == 2) {
                        arguments.addAll(ExecutableEventsManager.getInstance().getLoadedObjectsWith(args[1]));
                        for (String str : ExecutableEventLoader.getInstance().getAllFoldersName()) {
                            arguments.add("folder:" + str);
                        }
                        return arguments;
                    }
                    break;
                case "actionbar":
                    if (args.length == 2) {
                        arguments.add("on");
                        arguments.add("off");

                        return arguments;
                    }
                    break;
                case "delete":
                    if (args.length == 2) {
                        return ExecutableEventsManager.getInstance().getLoadedObjectsWith(args[1]);
                    } else if (args.length == 3) {
                        arguments.add("confirm");

                        return arguments;
                    }

                case "reload":
                    if (args.length == 2) {
                        arguments.addAll(ExecutableEventsManager.getInstance().getLoadedObjectsWith(args[1]));
                        for (String str : ExecutableEventLoader.getInstance().getAllFoldersName()) {
                            arguments.add("folder:" + str);
                        }
                        return arguments;
                    }
                    break;
                default:
                    arguments.addAll(pathCommand.getArguments(args[0], args));
                    if (!arguments.isEmpty()) return arguments;
                    break;
            }
        }
        return new ArrayList<>();
    }
}

