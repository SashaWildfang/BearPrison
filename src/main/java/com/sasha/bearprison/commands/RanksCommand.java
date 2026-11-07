package com.sasha.bearprison.commands;

import com.sasha.bearprison.BearPrison;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class RanksCommand implements CommandExecutor {
    private final BearPrison plugin;

    public RanksCommand(BearPrison plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        User user = plugin.getLuckPerms().getPlayerAdapter(Player.class).getUser(player);

        String currentRank = null;
        for (Node node : user.getNodes()) {
            if (node.getKey().startsWith("group.rank_")) {
                currentRank = node.getKey().replace("group.", "");
                break;
            }
        }

        if (currentRank == null) {
            currentRank = "rank_a";
        }

        sender.sendMessage(plugin.getMsg("ranks-header"));

        boolean showRank = false;

        for (String rank : plugin.getConfig().getConfigurationSection("ranks").getKeys(false)) {
            // Once we find the player's current rank, start showing the list
            if (rank.equals(currentRank)) {
                showRank = true;
            }

            if (showRank) {
                String nextRank = plugin.getConfig().getString("ranks." + rank + ".next");
                double cost = plugin.getConfig().getDouble("ranks." + rank + ".cost");

                // Stop if we hit the end of the track (e.g., rank_z has next: "none")
                if (nextRank == null || nextRank.equals("none")) continue;

                // Display the NEXT rank's name alongside the cost to get there
                String formattedNextRank = nextRank.replace("rank_", "").toUpperCase();

                sender.sendMessage(plugin.getMsg("ranks-format")
                        .replace("%rank%", formattedNextRank)
                        .replace("%cost%", formatSuffix(cost)));
            }
        }
        return true;
    }

    // Utility Method to format numbers to K, M, B, T
    private String formatSuffix(double value) {
        if (value == 0) return "0";
        String[] suffixes = new String[]{"", "K", "M", "B", "T", "Q"};
        int index = 0;

        while (value >= 1000 && index < suffixes.length - 1) {
            value /= 1000;
            index++;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value) + suffixes[index];
    }
}