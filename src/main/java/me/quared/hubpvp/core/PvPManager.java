package me.quared.hubpvp.core;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.scheduler.CancellableTask;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PvPManager {

	private final Map<Player, PvPState> playerPvpStates;
	private final Map<Player, CancellableTask> currentTimers;
	private final List<OldPlayerData> oldPlayerDataList;

	private ItemStack weapon, helmet, chestplate, leggings, boots;

	public PvPManager() {
		playerPvpStates = new HashMap<>();
		currentTimers = new HashMap<>();
		oldPlayerDataList = new ArrayList<>();

		loadItems();
	}

	public void loadItems() {
		// Weapon
		weapon = getItemFromConfig("weapon");

		// Armor
		helmet = getItemFromConfig("helmet");
		chestplate = getItemFromConfig("chestplate");
		leggings = getItemFromConfig("leggings");
		boots = getItemFromConfig("boots");
	}

	public ItemStack getItemFromConfig(String name) {
		HubPvP instance = HubPvP.instance();
		String material = instance.getConfig().getString("items." + name + ".material");
		if (material == null) {
			instance.getLogger().warning("Material for item " + name + " is null!");
			return new ItemStack(Material.AIR);
		}
		Material resolvedMaterial;
		try {
			resolvedMaterial = Material.valueOf(material.toUpperCase());
		} catch (IllegalArgumentException ex) {
			instance.getLogger().warning("Invalid material for item " + name + ": " + material);
			return new ItemStack(Material.AIR);
		}
		ItemStack item = new ItemStack(resolvedMaterial);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		String itemName = instance.getConfig().getString("items." + name + ".name");
		if (itemName != null && !itemName.isEmpty()) meta.setDisplayName(StringUtil.colorize(itemName));

		if ("weapon".equalsIgnoreCase(name)) {
			int customModelData = instance.getConfig().getInt("items.weapon.custom-model-data", -1);
			if (customModelData >= 0) {
				meta.setCustomModelData(customModelData);
			}
		}

		List<String> lore = instance.getConfig().getStringList("items." + name + ".lore");
		if (!lore.isEmpty() && !(lore.size() == 1 && lore.get(0).isEmpty())) meta.setLore(StringUtil.colorize(lore));

		List<String> enchants = instance.getConfig().getStringList("items." + name + ".enchantments");
		if (enchants != null && !enchants.isEmpty()) {
			for (String enchant : enchants) {
				String[] split = enchant.split(":");
				if (split.length != 2) {
					instance.getLogger().warning("Invalid enchant format for " + name + ": " + enchant);
					continue;
				}
				Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(split[0].toLowerCase()));
				if (enchantment == null) {
					instance.getLogger().warning("Could not find enchantment " + split[0]);
					continue;
				}
				try {
					item.addUnsafeEnchantment(enchantment, Integer.parseInt(split[1]));
				} catch (NumberFormatException ex) {
					instance.getLogger().warning("Invalid enchant level for " + name + ": " + enchant);
				}
			}
		}

		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		meta.setUnbreakable(true);
		item.setItemMeta(meta);

		return item;
	}

	public void enablePvP(Player player) {
		setPlayerState(player, PvPState.ON);

		if (getOldData(player) != null) getOldPlayerDataList().remove(getOldData(player));
		getOldPlayerDataList().add(new OldPlayerData(player, player.getInventory().getArmorContents(), player.getAllowFlight()));

		player.setAllowFlight(false);
		player.getInventory().setHelmet(getHelmet().clone());
		player.getInventory().setChestplate(getChestplate().clone());
		player.getInventory().setLeggings(getLeggings().clone());
		player.getInventory().setBoots(getBoots().clone());

		sendPvpStatus(player);
		player.sendMessage(StringUtil.format(player, HubPvP.instance().getConfig().getString("lang.pvp-enabled")));
	}

	public void setPlayerState(Player p, PvPState state) {
		playerPvpStates.put(p, state);
	}

	public @Nullable OldPlayerData getOldData(Player p) {
		return oldPlayerDataList.stream().filter(data -> data.player().equals(p)).findFirst().orElse(null);
	}

	public void removePlayer(Player p) {
		disablePvP(p);
		playerPvpStates.remove(p);
	}

	public void disablePvP(Player player) {
		setPlayerState(player, PvPState.OFF);

		OldPlayerData oldPlayerData = getOldData(player);
		if (oldPlayerData != null) {
			player.getInventory().setHelmet(oldPlayerData.armor()[3] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[3]);
			player.getInventory().setChestplate(oldPlayerData.armor()[2] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[2]);
			player.getInventory().setLeggings(oldPlayerData.armor()[1] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[1]);
			player.getInventory().setBoots(oldPlayerData.armor()[0] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[0]);
			player.setAllowFlight(oldPlayerData.canFly());
		}

		sendPvpStatus(player);
		player.sendMessage(StringUtil.format(player, HubPvP.instance().getConfig().getString("lang.pvp-disabled")));
	}

	public void disable() {
		for (Player p : playerPvpStates.keySet()) {
			if (isInPvP(p)) disablePvP(p);
		}
		playerPvpStates.clear();
	}

	public boolean isInPvP(Player player) {
		return getPlayerState(player) == PvPState.ON || getPlayerState(player) == PvPState.DISABLING;
	}

	public PvPState getPlayerState(Player p) {
		return playerPvpStates.get(p);
	}

	public void giveWeapon(Player p) {
		p.getInventory().setItem(HubPvP.instance().getConfig().getInt("items.weapon.slot") - 1, getWeapon().clone());
	}

	public void putTimer(Player p, CancellableTask timerTask) {
		if (getCurrentTimers().containsKey(p)) {
			getCurrentTimers().get(p).cancel();
		}
		getCurrentTimers().put(p, timerTask);
	}

	public void removeTimer(Player p) {
		if (getCurrentTimers().containsKey(p)) {
			getCurrentTimers().get(p).cancel();
		}
		getCurrentTimers().remove(p);
	}

	public Map<Player, PvPState> getPlayerPvpStates() {
		return playerPvpStates;
	}

	public Map<Player, CancellableTask> getCurrentTimers() {
		return currentTimers;
	}

	public List<OldPlayerData> getOldPlayerDataList() {
		return oldPlayerDataList;
	}

	public ItemStack getWeapon() {
		return weapon;
	}

	public ItemStack getHelmet() {
		return helmet;
	}

	public ItemStack getChestplate() {
		return chestplate;
	}

	public ItemStack getLeggings() {
		return leggings;
	}

	public ItemStack getBoots() {
		return boots;
	}

	public void sendPvpStatus(Player player) {
		String status = HubPvP.instance().getConfig().getString("lang.pvp-status");
		if (status == null || status.isBlank()) return;
		player.sendMessage(StringUtil.format(player, status));
	}

}
