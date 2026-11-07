package com.sasha.bearprison.managers;

import com.sasha.bearprison.BearPrison;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {
    private final BearPrison plugin;
    // Uses ConcurrentHashMap to safely remove entries while checking them
    private final Map<UUID, Long> combatTags = new ConcurrentHashMap<>();

    public CombatManager(BearPrison plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    private void startCleanupTask() {
        // Checks every 1 second (20 ticks) if anyone's combat tag has expired
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, Long>> it = combatTags.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<UUID, Long> entry = it.next();
                if (now > entry.getValue()) {
                    it.remove(); // Tag expired!
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        player.sendMessage(plugin.getMsg("combat-safe"));
                    }
                }
            }
        }, 20L, 20L);
    }

    public void tag(Player victim, Player attacker) {
        // Check for the bypass permission!
        if (victim.hasPermission("bn.bearprison.combatlog.bypass")) return;

        int duration = plugin.getConfig().getInt("settings.combat-tag-duration", 15);
        long expiry = System.currentTimeMillis() + (duration * 1000L);

        boolean wasInCombat = isInCombat(victim);
        combatTags.put(victim.getUniqueId(), expiry);

        // Only send the "You are in combat" message if they weren't already tagged
        if (!wasInCombat) {
            if (attacker != null && !attacker.equals(victim)) {
                victim.sendMessage(plugin.getMsg("combat-tagged-by").replace("%player%", attacker.getName()));
            } else {
                victim.sendMessage(plugin.getMsg("combat-tagged"));
            }
        }
    }

    public boolean isInCombat(Player player) {
        return combatTags.containsKey(player.getUniqueId()) && combatTags.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public int getRemainingSeconds(Player player) {
        if (!isInCombat(player)) return 0;
        return (int) Math.ceil((combatTags.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000.0);
    }

    public void removeTag(Player player) {
        combatTags.remove(player.getUniqueId());
    }
}