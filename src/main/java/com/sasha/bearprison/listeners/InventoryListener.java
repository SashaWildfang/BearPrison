package com.sasha.bearprison.listeners;

import com.sasha.bearprison.BearPrison;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryListener implements Listener {
    private final BearPrison plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public InventoryListener(BearPrison plugin) {
        this.plugin = plugin;
    }

    // Check when they mine a block
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // --- NEW: Direct XP Transfer ---
        int xp = event.getExpToDrop();
        if (xp > 0) {
            event.setExpToDrop(0); // Stop the physical orbs from spawning
            player.giveExp(xp);    // Send the XP straight to the player's bar
        }

        // --- EXISTING: Full Inventory Check ---
        checkInventory(player);
    }

    // Check when they try to pick an item up off the ground
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            checkInventory(player);
        }
    }

    private void checkInventory(Player player) {
        if (!plugin.getConfig().getBoolean("inventory-full.enabled", true)) return;

        // .firstEmpty() returns -1 if there are no completely empty slots left
        if (player.getInventory().firstEmpty() != -1) return;

        // Anti-Spam Cooldown Logic
        long cooldownMs = plugin.getConfig().getInt("inventory-full.cooldown", 3) * 1000L;
        long now = System.currentTimeMillis();
        long lastWarning = cooldowns.getOrDefault(player.getUniqueId(), 0L);

        if (now - lastWarning < cooldownMs) return;

        // Fetch config values and translate color codes
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("inventory-full.title", "&c&lINVENTORY FULL"));
        String subtitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("inventory-full.subtitle", "&7Use /condense or /sell!"));
        String soundName = plugin.getConfig().getString("inventory-full.sound", "block_note_block_bass");

        // Send Title (Fade In: 10 ticks, Stay: 40 ticks, Fade Out: 10 ticks)
        player.sendTitle(title, subtitle, 10, 40, 10);

        // Play Sound safely using the modern 1.21.3+ Registry API
        NamespacedKey key = NamespacedKey.minecraft(soundName.toLowerCase());
        Sound sound = Registry.SOUNDS.get(key);

        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } else {
            plugin.getLogger().warning("Invalid sound name in config: " + soundName);
        }

        // Update the player's last warning time
        cooldowns.put(player.getUniqueId(), now);
    }
}