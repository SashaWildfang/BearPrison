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

public class RankupMaxCommand implements CommandExecutor {
    private final BearPrison plugin;

    public RankupMaxCommand(BearPrison plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        User user = plugin.getLuckPerms().getPlayerAdapter(Player.class).getUser(player);

        // Find current rank
        String initialRank = null;
        for (Node node : user.getNodes()) {
            if (node.getKey().startsWith("group.rank_")) {
                initialRank = node.getKey().replace("group.", "");
                break;
            }
        }

        if (initialRank == null) {
            initialRank = "rank_a";
        }

        String currentIteratorRank = initialRank;
        double totalCost = 0.0;
        int ranksGained = 0;

        // Loop through ranks until they run out of money or hit max
        while (true) {
            String nextRank = plugin.getConfig().getString("ranks." + currentIteratorRank + ".next");

            // Break loop if they hit Rank Z (or whatever your max is)
            if (nextRank == null || nextRank.equals("none")) {
                break;
            }

            double cost = plugin.getConfig().getDouble("ranks." + currentIteratorRank + ".cost", 0.0);

            // Break loop if they can't afford this specific jump
            if (!plugin.getEconomy().getEconomyManager().hasBalance(player.getUniqueId(), cost)) {
                break;
            }

            // Deduct the money safely for this step
            plugin.getEconomy().getEconomyManager().removeBalance(player.getUniqueId(), cost);

            totalCost += cost;
            currentIteratorRank = nextRank;
            ranksGained++;
        }

        // If the loop finished and they gained 0 ranks, tell them why
        if (ranksGained == 0) {
            String nextRank = plugin.getConfig().getString("ranks." + initialRank + ".next");
            if (nextRank == null || nextRank.equals("none")) {
                player.sendMessage(plugin.getMsg("rankup-max"));
            } else {
                double cost = plugin.getConfig().getDouble("ranks." + initialRank + ".cost", 0.0);
                String formattedNextRank = nextRank.replace("rank_", "").toUpperCase();
                player.sendMessage(plugin.getMsg("rankup-insufficient-funds")
                        .replace("%price%", formatSuffix(cost))
                        .replace("%next_rank%", formattedNextRank));
            }
            return true;
        }

        // If we got here, they ranked up at least once!
        final String finalOriginalRank = initialRank;
        final String finalNewRank = currentIteratorRank;
        final String formattedNextRank = finalNewRank.replace("rank_", "").toUpperCase();
        final double finalTotalCost = totalCost;

        // Perform the heavy LuckPerms update asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            user.data().remove(InheritanceNode.builder(finalOriginalRank).build());
            user.data().add(InheritanceNode.builder(finalNewRank).build());
            plugin.getLuckPerms().getUserManager().saveUser(user);

            // Send messages back on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Personal summary message
                player.sendMessage(plugin.getMsg("rankupmax-success")
                        .replace("%next_rank%", formattedNextRank)
                        .replace("%total_cost%", formatSuffix(finalTotalCost)));

                // Global broadcast
                String broadcastMsg = plugin.getMsg("rankupmax-broadcast")
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