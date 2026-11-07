package com.sasha.bearprison.tasks;

import com.sasha.bearprison.BearPrison;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankupReminderTask extends BukkitRunnable {
    private final BearPrison plugin;
    private final Map<UUID, Long> lastReminded = new HashMap<>();

    public RankupReminderTask(BearPrison plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("rankup-reminder.enabled", true)) return;

        long cooldownMs = plugin.getConfig().getInt("rankup-reminder.interval", 300) * 1000L;
        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            // 1. Check if the player is still on cooldown from their last reminder
            if (now - lastReminded.getOrDefault(player.getUniqueId(), 0L) < cooldownMs) {
                continue;
            }

            // 2. Safely fetch their current prison rank using LuckPerms
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

            // 3. Find their next rank and the cost
            String nextRank = plugin.getConfig().getString("ranks." + currentRank + ".next");
            if (nextRank == null || nextRank.equals("none")) {
                continue; // They are at max rank, do nothing
            }

            double cost = plugin.getConfig().getDouble("ranks." + currentRank + ".cost", 0.0);

            // 4. Hook into BearEconomy to see if they can afford it
            if (plugin.getEconomy().getEconomyManager().hasBalance(player.getUniqueId(), cost)) {
                String formattedNextRank = nextRank.replace("rank_", "").toUpperCase();

                player.sendMessage(plugin.getMsg("rankup-reminder")
                        .replace("%next_rank%", formattedNextRank)
                        .replace("%cost%", formatSuffix(cost)));

                // 5. Update their cooldown so they aren't spammed
                lastReminded.put(player.getUniqueId(), now);
            }
        }
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