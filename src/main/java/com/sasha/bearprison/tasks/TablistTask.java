package com.sasha.bearprison.tasks;

import com.sasha.bearprison.BearPrison;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class TablistTask extends BukkitRunnable {
    private final BearPrison plugin;
    private int tickCounter = 0;
    private int messageIndex = 0;
    private int barIndex = 0;

    public TablistTask(BearPrison plugin) {
        this.plugin = plugin;
    }

    private String getPingBars(int ping) {
        if (ping < 50) return "§aıll";
        if (ping < 150) return "§eıl§8l";
        return "§cı§8ll";
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("tablist.enabled", true)) return;

        tickCounter += 10;

        List<String> messages = plugin.getConfig().getStringList("tablist.rotating-messages.messages");
        int messageInterval = plugin.getConfig().getInt("tablist.rotating-messages.interval", 100);

        // 1. Rotate the Middle Message
        if (!messages.isEmpty() && tickCounter % messageInterval == 0) {
            messageIndex++;
            if (messageIndex >= messages.size()) {
                messageIndex = 0;
            }
        }

        // 2. Rotate the Loading Bar Animation
        barIndex++;
        if (barIndex >= 10) {
            barIndex = 0;
        }

        String currentMessage = messages.isEmpty() ? "" : messages.get(messageIndex);

        // 3. Build the Dynamic Top Tags
        String[] tags = {"INFO", "DISCORD", "STORE", "VOTE"};
        StringBuilder tagsBar = new StringBuilder();
        for (int i = 0; i < tags.length; i++) {
            // Check if this tag matches the current message index
            if (i == (messageIndex % tags.length)) {
                tagsBar.append("&9[").append(tags[i]).append("]"); // Active (Blue)
            } else {
                tagsBar.append("&7[").append(tags[i]).append("]"); // Inactive (Gray)
            }
            if (i < tags.length - 1) tagsBar.append(" ");
        }

        // 4. Build the Loading Bar: Fills up with Blue (■) over Gray
        StringBuilder loadingBar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i <= barIndex) {
                loadingBar.append("&9■");
            } else {
                loadingBar.append("&8■");
            }
        }

        List<String> headerList = plugin.getConfig().getStringList("tablist.header");
        List<String> footerList = plugin.getConfig().getStringList("tablist.footer");

        String rawHeader = String.join("\n", headerList);
        String rawFooter = String.join("\n", footerList);
        int onlineCount = Bukkit.getOnlinePlayers().size();

        // 5. Distribute to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            String header = rawHeader;
            String footer = rawFooter;

            String pingBars = getPingBars(player.getPing());

            // Replace Custom Variables (Added %dynamic_tags%)
            header = header.replace("%player%", player.getName())
                    .replace("%ping%", String.valueOf(player.getPing()))
                    .replace("%ping_bars%", pingBars)
                    .replace("%online%", String.valueOf(onlineCount))
                    .replace("%dynamic_tags%", tagsBar.toString());

            footer = footer.replace("%player%", player.getName())
                    .replace("%ping%", String.valueOf(player.getPing()))
                    .replace("%online%", String.valueOf(onlineCount))
                    .replace("%rotating_message%", currentMessage)
                    .replace("%loading_bar%", loadingBar.toString());

            // Apply PlaceholderAPI and Colors
            header = PlaceholderAPI.setPlaceholders(player, header);
            header = ChatColor.translateAlternateColorCodes('&', header);

            footer = PlaceholderAPI.setPlaceholders(player, footer);
            footer = ChatColor.translateAlternateColorCodes('&', footer);

            player.setPlayerListHeaderFooter(header, footer);
        }
    }
}