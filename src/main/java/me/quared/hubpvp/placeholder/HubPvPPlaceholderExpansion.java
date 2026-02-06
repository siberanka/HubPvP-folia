package me.quared.hubpvp.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPState;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HubPvPPlaceholderExpansion extends PlaceholderExpansion {

	private final HubPvP plugin;

	public HubPvPPlaceholderExpansion(HubPvP plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull String getIdentifier() {
		return "hubpvp";
	}

	@Override
	public @NotNull String getAuthor() {
		return String.join(", ", plugin.getDescription().getAuthors());
	}

	@Override
	public @NotNull String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onPlaceholderRequest(Player player, @NotNull String params) {
		if (player == null) return "";

		if (params.equalsIgnoreCase("status")) {
			PvPState state = plugin.pvpManager().getPlayerState(player);
			boolean enabled = state == PvPState.ON || state == PvPState.DISABLING;
			String key = enabled ? "lang.status-on" : "lang.status-off";
			return StringUtil.colorize(plugin.getConfig().getString(key, enabled ? "&aON" : "&cOFF"));
		}

		return null;
	}
}
