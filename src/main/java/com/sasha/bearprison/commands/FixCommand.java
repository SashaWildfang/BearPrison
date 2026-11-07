package com.sasha.bearprison.commands;

import com.sasha.bearprison.BearPrison;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class FixCommand implements CommandExecutor {
    private final BearPrison plugin;
    private final long COOLDOWN_MS = 86400000L; // 24 hours in milliseconds

    public FixCommand(BearPrison plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("bn.bearprison.fix")) {
            player.sendMessage(plugin.getMsg("no-permission"));
            return true;
        }

        // 1. Cooldown Check
        if (!player.hasPermission("bn.bearprison.fix.bypass")) {
            long lastFix = plugin.getCooldownsConfig().getLong("fix." + player.getUniqueId().toString(), 0);
            long now = System.currentTimeMillis();

            if (now - lastFix < COOLDOWN_MS) {
                long timeLeft = COOLDOWN_MS - (now - lastFix);
                String formattedTime = formatTime(timeLeft);
                player.sendMessage(plugin.getMsg("fix-cooldown").replace("%time%", formattedTime));
                return true;
            }
        }

        // Determine if player wants to fix all or just hand
        boolean fixAll = args.length > 0 && args[0].equalsIgnoreCase("all");

        if (fixAll) {
            // Fix All Logic
            if (!player.hasPermission("bn.bearprison.fix.all")) {
                player.sendMessage(plugin.getMsg("no-permission"));
                return true;
            }

            boolean fixedAny = false;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || item.getType().isAir()) continue;

                ItemMeta meta = item.getItemMeta();
                if (meta instanceof Damageable damageable && damageable.getDamage() > 0) {
                    damageable.setDamage(0);
                    item.setItemMeta(meta);
                    fixedAny = true;
                }
            }

            if (!fixedAny) {
                player.sendMessage(plugin.getMsg("fix-invalid-all"));
                return true;
            }

            player.sendMessage(plugin.getMsg("fix-all-success"));

        } else {
            // Fix Hand Logic (triggers on /fix or /fix hand)
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                player.sendMessage(plugin.getMsg("fix-empty"));
                return true;
            }

            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof Damageable damageable) || damageable.getDamage() == 0) {
                player.sendMessage(plugin.getMsg("fix-invalid"));
                return true;
            }

            damageable.setDamage(0);
            item.setItemMeta(meta);
            player.sendMessage(plugin.getMsg("fix-success"));
        }

        // 4. Save Cooldown
        plugin.getCooldownsConfig().set("fix." + player.getUniqueId().toString(), System.currentTimeMillis());
        plugin.saveCooldownsConfig();

        return true;
    }

    // Utility to format milliseconds into "14h 22m 10s"
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;

        StringBuilder time = new StringBuilder();
        if (h > 0) time.append(h).append("h ");
        if (m > 0) time.append(m).append("m ");
        time.append(s).append("s");
        return time.toString().trim();
    }
}