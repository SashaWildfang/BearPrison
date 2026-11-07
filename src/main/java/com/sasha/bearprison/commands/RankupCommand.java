package com.sasha.bearprison.commands;

import com.sasha.bearprison.BearPrison;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class RankupCommand implements CommandExecutor {
    private final BearPrison plugin;

    public RankupCommand(BearPrison plugin) { this.plugin = plugin; }

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

        String nextRank = plugin.getConfig().getString("ranks." + currentRank + ".next");
        double cost = plugin.getConfig().getDouble("ranks." + currentRank + ".cost", 0.0);

        if (nextRank == null || nextRank.equals("none")) {
            player.sendMessage(plugin.getMsg("rankup-max"));
            return true;
        }

        // Format the raw config string (e.g., "rank_b") into just "B"
        String formattedNextRank = nextRank.replace("rank_", "").toUpperCase();

        if (!plugin.getEconomy().getEconomyManager().hasBalance(player.getUniqueId(), cost)) {
            player.sendMessage(plugin.getMsg("rankup-insufficient-funds")
                    .replace("%price%", formatSuffix(cost))
                    .replace("%next_rank%", formattedNextRank));
            return true;
        }

        plugin.getEconomy().getEconomyManager().removeBalance(player.getUniqueId(), cost);

        final String finalCurrentRank = currentRank;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            user.data().remove(InheritanceNode.builder(finalCurrentRank).build());
            user.data().add(InheritanceNode.builder(nextRank).build());
            plugin.getLuckPerms().getUserManager().saveUser(user);

            Bukkit.getScheduler().runTask(plugin, () -> {
                // Send personal message
                player.sendMessage(plugin.getMsg("rankup-success").replace("%next_rank%", formattedNextRank));

                // Send global broadcast
                String broadcastMsg = plugin.getMsg("rankup-broadcast")
                        .replace("%player%", player.getName())
                        .replace("%next_rank%", formattedNextRank);
                Bukkit.broadcastMessage(broadcastMsg);
            });
        });

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