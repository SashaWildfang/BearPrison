package com.sasha.bearprison.listeners;

import com.sasha.bearprison.BearPrison;
import net.luckperms.api.model.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    private final BearPrison plugin;

    public PlayerListener(BearPrison plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        User user = plugin.getLuckPerms().getPlayerAdapter(org.bukkit.entity.Player.class).getUser(event.getPlayer());

        // Check if user has ANY group from the 'prison' track
        boolean hasPrisonRank = user.getNodes().stream()
                .anyMatch(node -> node.getKey().startsWith("group.rank_"));

        if (!hasPrisonRank) {
            // Perform the heavy LuckPerms data operation asynchronously
            org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // Add Rank A
                user.data().add(net.luckperms.api.node.types.InheritanceNode.builder("rank_a").build());
                plugin.getLuckPerms().getUserManager().saveUser(user);

                // Send the join message back on the main thread (thread-safe)
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    if (event.getPlayer().isOnline()) {
                        event.getPlayer().sendMessage(plugin.getMsg("join-rank-set"));
                    }
                });
            });
        }
    }
}