package com.sasha.bearprison.commands;

import com.sasha.bearprison.BearPrison;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CondenseCommand implements CommandExecutor {
    private final BearPrison plugin;
    private final Map<Material, Material> recipes = new HashMap<>();

    public CondenseCommand(BearPrison plugin) {
        this.plugin = plugin;

        // Register Condensable Materials (Raw -> Block)
        recipes.put(Material.COAL, Material.COAL_BLOCK);
        recipes.put(Material.IRON_INGOT, Material.IRON_BLOCK);
        recipes.put(Material.GOLD_INGOT, Material.GOLD_BLOCK);
        recipes.put(Material.DIAMOND, Material.DIAMOND_BLOCK);
        recipes.put(Material.EMERALD, Material.EMERALD_BLOCK);
        recipes.put(Material.REDSTONE, Material.REDSTONE_BLOCK);
        recipes.put(Material.LAPIS_LAZULI, Material.LAPIS_BLOCK);
        recipes.put(Material.NETHERITE_INGOT, Material.NETHERITE_BLOCK);
        recipes.put(Material.RAW_IRON, Material.RAW_IRON_BLOCK);
        recipes.put(Material.RAW_GOLD, Material.RAW_GOLD_BLOCK);
        recipes.put(Material.RAW_COPPER, Material.RAW_COPPER_BLOCK);
        recipes.put(Material.COPPER_INGOT, Material.COPPER_BLOCK);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("bn.bearprison.condense")) {
            player.sendMessage(plugin.getMsg("no-permission"));
            return true;
        }

        int blocksCrafted = 0;

        for (Map.Entry<Material, Material> entry : recipes.entrySet()) {
            Material raw = entry.getKey();
            Material block = entry.getValue();

            int totalRaw = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == raw) {
                    totalRaw += item.getAmount();
                }
            }

            if (totalRaw >= 9) {
                int toBlock = totalRaw / 9;
                int remainder = totalRaw % 9;

                // Strip all of the raw items from inventory
                player.getInventory().remove(raw);

                // Add the formatted blocks and the leftover raw materials
                player.getInventory().addItem(new ItemStack(block, toBlock));
                if (remainder > 0) {
                    player.getInventory().addItem(new ItemStack(raw, remainder));
                }

                blocksCrafted += toBlock;
            }
        }

        if (blocksCrafted > 0) {
            player.sendMessage(plugin.getMsg("condense-success").replace("%count%", String.valueOf(blocksCrafted)));
        } else {
            player.sendMessage(plugin.getMsg("condense-none"));
        }

        return true;
    }
}