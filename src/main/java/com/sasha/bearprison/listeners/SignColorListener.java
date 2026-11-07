package com.sasha.bearprison.listeners;

import com.sasha.bearprison.BearPrison;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignColorListener implements Listener {

    private final BearPrison plugin;

    public SignColorListener(BearPrison plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        // Check if the player is an Operator or has the specific permission node
        if (player.isOp() || player.hasPermission("bn.bearprison.coloredsigns")) {

            // Loop through all 4 lines of the sign
            for (int i = 0; i < 4; i++) {
                String line = event.getLine(i);
                if (line != null && !line.isEmpty()) {
                    // Translate the '&' color codes into Minecraft's internal '§' formatting
                    event.setLine(i, ChatColor.translateAlternateColorCodes('&', line));
                }
            }
        }
    }
}