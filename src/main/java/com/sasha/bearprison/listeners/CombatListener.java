package com.sasha.bearprison.listeners;

import com.sasha.bearprison.BearPrison;
import com.sasha.bearprison.tasks.NametagTask;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {

    private final BearPrison plugin;
    private final NametagTask nametagTask;

    public CombatListener(BearPrison plugin, NametagTask nametagTask) {
        this.plugin = plugin;
        this.nametagTask = nametagTask;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player victim = null;
        Player attacker = null;

        // 1. Identify Victim
        if (event.getEntity() instanceof Player) {
            victim = (Player) event.getEntity();
        }

        // 2. Identify Attacker (Handles both melee and bows/projectiles!)
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile proj) {
            // If an arrow hit the victim, check if a player shot it!
            if (proj.getShooter() instanceof Player shooter) {
                attacker = shooter;
            }
        }

        // 3. If both involved are players (and they didn't shoot themselves)
        if (victim != null && attacker != null && !victim.equals(attacker)) {

            // Tag BOTH players in the combat manager!
            // This now automatically triggers the glow effect in NametagTask!
            plugin.getCombatManager().tag(victim, attacker);
            plugin.getCombatManager().tag(attacker, victim);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // If they log out while the combat tag is active...
        if (plugin.getCombatManager().isInCombat(player)) {
            // Instantly kill them so they drop their inventory
            player.setHealth(0.0);
            plugin.getCombatManager().removeTag(player);

            // Broadcast their cowardice to the whole server!
            String broadcast = plugin.getMsg("combat-logged").replace("%player%", player.getName());
            plugin.getServer().broadcastMessage(broadcast);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // If they die legitimately in combat, remove the tag so they don't spawn with it
        plugin.getCombatManager().removeTag(event.getEntity());
    }
}