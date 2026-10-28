package com.ssomar.executableevents.editor;

import com.ssomar.score.utils.backward_compatibility.SnapshotUtils;
import com.ssomar.executableevents.executableevents.ExecutableEventEditor;
import com.ssomar.executableevents.executableevents.ExecutableEventEditorManager;
import com.ssomar.executableevents.executableevents.activators.ActivatorEEFeatureEditor;
import com.ssomar.executableevents.executableevents.activators.ActivatorEEFeatureEditorManager;
import com.ssomar.score.menu.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class EditorInteractionsListener implements Listener {

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();
        try {
            if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        } catch (NullPointerException error) {
            return;
        }

        int slot = e.getSlot();
        ItemStack itemS = e.getClickedInventory().getItem(slot);
        //boolean notNullItem = itemS!=null;

        try {
            if (SnapshotUtils.getHolderNoSnapshot(e.getInventory()) instanceof GUI)
                this.manage(player, itemS, "", e);
        } catch (NullPointerException error) {
            error.printStackTrace();
        }
    }

    public void manage(Player player, ItemStack itemS, String guiType, InventoryClickEvent e) {

        e.setCancelled(true);

        if (itemS == null) return;

        if (!itemS.hasItemMeta()) return;

        if (!GUI.hasIdentifier(itemS)) return;

        //String itemName = itemS.getItemMeta().getDisplayName();

        boolean isShiftLeft = e.getClick().equals(ClickType.SHIFT_LEFT);

        InventoryHolder holder = SnapshotUtils.getHolderNoSnapshot(e.getInventory());

        if (holder instanceof ExecutableEventEditor) {
            ExecutableEventEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof ActivatorEEFeatureEditor) {
            ActivatorEEFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatEvent2(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().contains("/score interact ")) {
            String message = e.getMessage().replace("/score interact ", "");
            receiveMessage(e.getPlayer(), message, e);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatEvent(AsyncPlayerChatEvent e) {
        receiveMessage(e.getPlayer(), e.getMessage(), e);
    }

    public void receiveMessage(Player player, String message, Cancellable e) {
        Player p = player;
        if (ExecutableEventEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            ExecutableEventEditorManager.getInstance().receiveMessage(p, message);
        } else if (ActivatorEEFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            ActivatorEEFeatureEditorManager.getInstance().receiveMessage(p, message);
        }
    }
}
