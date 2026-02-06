package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.core.PvPState;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ItemSlotChangeListener implements Listener {

	private static final long JOIN_SLOT_SYNC_GRACE_MS = 2500L;
	private final Map<UUID, Long> joinGraceUntil = new ConcurrentHashMap<>();

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		joinGraceUntil.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + JOIN_SLOT_SYNC_GRACE_MS);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		joinGraceUntil.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onSlotChange(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		ItemStack held = p.getInventory().getItem(e.getNewSlot());
		HubPvP instance = HubPvP.instance();
		PvPManager pvpManager = instance.pvpManager();

		if (!p.hasPermission("hubpvp.use")) return;

		if (isWeapon(held, pvpManager)) {
			if (isInJoinGrace(p) && !pvpManager.isInPvP(p)) return;

			if (pvpManager.getPlayerState(p) == PvPState.DISABLING) pvpManager.setPlayerState(p, PvPState.ON);
			if (pvpManager.getPlayerState(p) == PvPState.ENABLING) return;

			if (HubPvP.instance().getConfig().getStringList("disabled-worlds").contains(p.getWorld().getName())) {
				p.sendMessage(StringUtil.format(p, instance.getConfig().getString("lang.disabled-in-world")));
				return;
			}

			if (!pvpManager.isInPvP(p)) {
				pvpManager.setPlayerState(p, PvPState.ENABLING);
				final int[] time = {instance.getConfig().getInt("enable-cooldown") + 1};
				pvpManager.putTimer(p, instance.taskScheduler().runPlayerTimer(p, () -> {
					if (!p.isOnline()) {
						pvpManager.removeTimer(p);
						return;
					}

					time[0]--;
					ItemStack currentHeld = p.getInventory().getItem(p.getInventory().getHeldItemSlot());
					if (pvpManager.getPlayerState(p) != PvPState.ENABLING || !isWeapon(currentHeld, pvpManager)) {
						pvpManager.removeTimer(p);
					} else if (time[0] == 0) {
						pvpManager.enablePvP(p);
						pvpManager.removeTimer(p);
					} else {
						p.sendMessage(StringUtil.format(p, instance.getConfig().getString("lang.pvp-enabling").replaceAll("%time%", Integer.toString(time[0]))));
					}
				}, 0L, 20L));
			}
		} else if (pvpManager.isInPvP(p)) {
			if (pvpManager.getPlayerState(p) == PvPState.ENABLING) pvpManager.setPlayerState(p, PvPState.OFF);
			if (pvpManager.getPlayerState(p) == PvPState.DISABLING) return;

			pvpManager.setPlayerState(p, PvPState.DISABLING);
			final int[] time = {instance.getConfig().getInt("disable-cooldown") + 1};
			pvpManager.putTimer(p, instance.taskScheduler().runPlayerTimer(p, () -> {
				if (!p.isOnline()) {
					pvpManager.removeTimer(p);
					return;
				}

				time[0]--;
				ItemStack currentHeld = p.getInventory().getItem(p.getInventory().getHeldItemSlot());
				if (pvpManager.getPlayerState(p) != PvPState.DISABLING || isWeapon(currentHeld, pvpManager)) {
					pvpManager.removeTimer(p);
				} else if (time[0] == 0) {
					pvpManager.disablePvP(p);
					pvpManager.removeTimer(p);
				} else {
					p.sendMessage(StringUtil.format(p, instance.getConfig().getString("lang.pvp-disabling").replaceAll("%time%", Integer.toString(time[0]))));
				}
			}, 0L, 20L));
		} else {
			pvpManager.setPlayerState(p, PvPState.OFF);
			pvpManager.removeTimer(p);
		}
	}

	private boolean isInJoinGrace(Player player) {
		Long expires = joinGraceUntil.get(player.getUniqueId());
		return expires != null && System.currentTimeMillis() <= expires;
	}

	private boolean isWeapon(ItemStack stack, PvPManager manager) {
		return stack != null && stack.isSimilar(manager.getWeapon());
	}

}
