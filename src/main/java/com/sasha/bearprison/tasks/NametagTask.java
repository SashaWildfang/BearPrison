package com.sasha.bearprison.tasks;

import com.sasha.bearprison.BearPrison;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.Map;
import java.util.SortedMap;

public class NametagTask extends BukkitRunnable {
    private final BearPrison plugin;
    private final Scoreboard scoreboard;

    public NametagTask(BearPrison plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective oldObj = scoreboard.getObjective("health_below");
        if (oldObj != null) oldObj.unregister();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateVisuals(player);
        }
    }

    private void updateVisuals(Player player) {
        double health = player.getHealth();
        int healthInt = (int) Math.ceil(health);

        // SYNCED WITH COMBAT MANAGER: Will glow for the entire combat tag duration!
        boolean inCombat = plugin.getCombatManager().isInCombat(player);
        boolean shouldGlow = (inCombat || health < 15);

        // Determine the color for the suffix AND the glow
        String healthColorString;
        ChatColor glowColor;

        if (health >= 15) {
            healthColorString = "§a";
            glowColor = ChatColor.GREEN;
        } else if (health >= 10) {
            healthColorString = "§e";
            glowColor = ChatColor.YELLOW;
        } else if (health >= 5) {
            healthColorString = "§c";
            glowColor = ChatColor.RED;
        } else {
            healthColorString = "§4";
            glowColor = ChatColor.DARK_RED;
        }

        String healthSuffix = " " + healthColorString + healthInt + " §4❤";

        // LuckPerms & Prefix Logic
        User user = plugin.getLuckPerms().getPlayerAdapter(Player.class).getUser(player);

        int combinedWeight = 0;
        for (Node node : user.getNodes()) {
            if (node instanceof InheritanceNode inheritanceNode) {
                Group group = plugin.getLuckPerms().getGroupManager().getGroup(inheritanceNode.getGroupName());
                if (group != null && group.getWeight().isPresent()) {
                    combinedWeight += group.getWeight().getAsInt();
                }
            }
        }

        String fullPrefix = user.getCachedData().getMetaData().getPrefix();
        if (fullPrefix == null) fullPrefix = "&8[&7Member&8] ";

        SortedMap<Integer, String> allPrefixes = user.getCachedData().getMetaData().getPrefixes();
        String staffPrefix = "";
        String donorPrefix = "";
        for (Map.Entry<Integer, String> entry : allPrefixes.entrySet()) {
            int weight = entry.getKey();
            String pfx = entry.getValue();
            if (weight >= 1000 && staffPrefix.isEmpty()) staffPrefix = pfx;
            else if (weight >= 100 && weight < 1000 && donorPrefix.isEmpty()) donorPrefix = pfx;
        }
        String nameplatePrefix = ChatColor.translateAlternateColorCodes('&', staffPrefix + donorPrefix);

        // Formatting
        String primaryGroup = user.getPrimaryGroup();
        String formatPath = "tab-formats." + (plugin.getConfig().contains("tab-formats." + primaryGroup) ? primaryGroup : "default");
        String rawTabFormat = plugin.getConfig().getString(formatPath, "%luckperms_prefix%&7%player%");
        String formattedTabName = ChatColor.translateAlternateColorCodes('&', rawTabFormat.replace("%luckperms_prefix%", fullPrefix).replace("%player%", player.getName()));
        player.setPlayerListName(" " + formattedTabName + healthSuffix);

        int sortScore = Math.max(0, 99999 - combinedWeight);
        String teamName = String.format("%05d", sortScore) + player.getName();
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);

        // --- GLOW AND CLEANUP LOGIC ---
        if (shouldGlow) {
            // Player is in danger/combat, turn it on
            if (!player.isGlowing()) player.setGlowing(true);
            if (team.getColor() != glowColor) team.setColor(glowColor);
        } else {
            // Player is safe, force it off
            if (player.isGlowing()) player.setGlowing(false);
            // Reset team color to WHITE so the Green/Yellow/Red glow vanishes
            if (team.getColor() != ChatColor.WHITE) team.setColor(ChatColor.WHITE);
        }

        if (!team.getPrefix().equals(nameplatePrefix)) team.setPrefix(nameplatePrefix);
        if (team.getSuffix() != null && !team.getSuffix().equals("")) team.setSuffix("");
        if (!team.hasEntry(player.getName())) {
            for (Team t : scoreboard.getTeams()) {
                if (t.hasEntry(player.getName()) && !t.getName().equals(teamName)) t.removeEntry(player.getName());
            }
            team.addEntry(player.getName());
        }

        if (player.getScoreboard() != scoreboard) player.setScoreboard(scoreboard);
    }
}