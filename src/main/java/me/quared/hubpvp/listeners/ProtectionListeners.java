package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class ProtectionListeners implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		PvPManager pvPManager = HubPvP.instance().pvpManager();

		if (isInventoryLockEnabled() && isPlayerInventoryMove(e)) {
			e.setCancelled(true);
			return;
		}

		if (item == null) return;

		if (pvPManager.isInPvP(p)) {
			if (item.isSimilar(pvPManager.getWeapon())) {
				e.setCancelled(true);
			} else if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onDrag(InventoryDragEvent e) {
		if (!isInventoryLockEnabled()) return;

		int topSize = e.getView().getTopInventory().getSize();
		for (int rawSlot : e.getRawSlots()) {
			if (rawSlot >= topSize) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void onSwapHand(PlayerSwapHandItemsEvent e) {
		if (isInventoryLockEnabled()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		ItemStack item = e.getItemDrop().getItemStack();
		PvPManager pvPManager = HubPvP.instance().pvpManager();

		if (pvPManager.isInPvP(p)) {
			if (item.isSimilar(pvPManager.getWeapon())) {
				e.setCancelled(true);
			} else if (item.getType().toString().toLowerCase().contains("armor")) { // very bad way of doing this, feel free to make a new branch to update
				e.setCancelled(true);
			}
		}
	}

	private boolean isInventoryLockEnabled() {
		return HubPvP.instance().getConfig().getBoolean("inventory.lock-item-slots", true);
	}

	private boolean isPlayerInventoryMove(InventoryClickEvent e) {
		if (e.getClick() == ClickType.NUMBER_KEY) return true;
		if (e.getAction() == InventoryAction.HOTBAR_SWAP || e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) return true;
		if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) return true;
		return e.getClickedInventory() != null && e.getClickedInventory().getType() == InventoryType.PLAYER;
	}

}
