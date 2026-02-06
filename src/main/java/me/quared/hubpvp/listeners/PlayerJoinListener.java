package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.OldPlayerData;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.core.PvPState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PvPManager pvPManager = HubPvP.instance().pvpManager();

        // Clear armor for Floodgate/Geyser players to avoid stale HubPvP armor on join.
        if (isGeyserPlayer(p)) {
            p.getInventory().setArmorContents(new ItemStack[4]);
        }

        if (p.hasPermission("hubpvp.use") &&
                !HubPvP.instance().getConfig().getStringList("disabled-worlds").contains(p.getWorld().getName())) {
            pvPManager.giveWeapon(p);
        }

        pvPManager.getOldPlayerDataList().add(new OldPlayerData(p, p.getInventory().getArmorContents(), p.getAllowFlight()));
        pvPManager.setPlayerState(p, PvPState.OFF);
        pvPManager.sendPvpStatus(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        PvPManager pvPManager = HubPvP.instance().pvpManager();

        pvPManager.removePlayer(p);
    }

    private boolean isGeyserPlayer(Player player) {
        try {
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Method getInstance = floodgateApiClass.getMethod("getInstance");
            Object floodgateApi = getInstance.invoke(null);
            if (floodgateApi == null) return false;

            Method isFloodgatePlayer = floodgateApiClass.getMethod("isFloodgatePlayer", UUID.class);
            Object result = isFloodgatePlayer.invoke(floodgateApi, player.getUniqueId());
            return result instanceof Boolean && (Boolean) result;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

}
