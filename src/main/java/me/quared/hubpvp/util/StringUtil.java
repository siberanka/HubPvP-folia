package me.quared.hubpvp.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {

	private static final Pattern HEX_PATTERN = Pattern.compile("&(#\\w{6})");

	private StringUtil() {
		throw new UnsupportedOperationException("This class is not meant to be instantiated.");
	}

	public static List<String> colorize(List<String> strings) {
		List<String> finalList = new ArrayList<>();
		for (String s : strings) finalList.add(colorize(s));
		return finalList;
	}

	public static String colorize(String message) {
		if (message == null) return "";
		Matcher matcher = HEX_PATTERN.matcher(ChatColor.translateAlternateColorCodes('&', message));
		StringBuilder buffer = new StringBuilder();

		while (matcher.find()) {
			matcher.appendReplacement(buffer, ChatColor.of(matcher.group(1)).toString());
		}

		return matcher.appendTail(buffer).toString();
	}

	public static String format(Player player, String message) {
		if (message == null) return "";
		String parsed = message;

		if (player != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			try {
				parsed = PlaceholderAPI.setPlaceholders(player, parsed);
			} catch (NoClassDefFoundError ignored) {
				// PlaceholderAPI not present at runtime.
			}
		}

		return colorize(parsed);
	}

}
