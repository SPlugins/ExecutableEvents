package com.ssomar.executableevents.events.player;

import com.ssomar.executableevents.ExecutableEvents;
import com.ssomar.executableevents.configs.GeneralConfig;
import com.ssomar.score.SCore;
import com.ssomar.score.utils.CheckVersionSpigot;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEvt implements Listener {


    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {

        Player p = e.getPlayer();

        if (p.isOp() && GeneralConfig.getInstance().getBooleanSetting(GeneralConfig.Setting.checkVersionMsg.name())) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    try {
                        String cVer = CheckVersionSpigot.getVersionOf("107622");
                        if (cVer == null) return;
                        String aVer = ExecutableEvents.plugin.getPlugin().getDescription().getVersion();

                        if (isNewerVersion(cVer, aVer)) {
                            p.sendMessage(StringConverter.coloredString("&8&l&oExecutableEvents &8(&cOnly for op&8)  &7Your version of ExecutableEvents is out of date. &8(&7Current: &6" + aVer + "&8) &8(&7Latest: &a" + cVer + "&8) &7Update your plugin for the latest features / bug fixes / better performance !"));
                        }
                    } catch (Exception err) {
                        err.printStackTrace();
                    }

                }
            };
            SCore.schedulerHook.runAsyncTask(runnable, 0L);
        }
    }

    /**
     * True only if latest is strictly newer than current, compared number by number (7.26.10.4 > 7.26.9.22).
     * Anything else than dot-separated numbers gives false: no message rather than a wrong one.
     */
    public static boolean isNewerVersion(String latest, String current) {
        if (latest == null || current == null) return false;
        String format = "\\d{1,9}(\\.\\d{1,9})*";
        latest = latest.trim();
        current = current.trim();
        // dev build, never out of date (42.42.42.x builds were once published on Spigot)
        if (current.equals("42.42.42")) return false;
        if (!latest.matches(format) || !current.matches(format)) return false;
        String[] l = latest.split("\\.");
        String[] c = current.split("\\.");
        for (int i = 0; i < Math.max(l.length, c.length); i++) {
            int a = i < l.length ? Integer.parseInt(l[i]) : 0;
            int b = i < c.length ? Integer.parseInt(c[i]) : 0;
            if (a != b) return a > b;
        }
        return false;
    }

}
