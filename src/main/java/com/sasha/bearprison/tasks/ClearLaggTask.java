package com.sasha.bearprison.tasks;

import com.sasha.bearprison.BearPrison;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ClearLaggTask extends BukkitRunnable {
    private final BearPrison plugin;
    private int timeLeft;
    private final int interval;
    private final int threshold;
    private final List<Integer> warnings;

    public ClearLaggTask(BearPrison plugin) {
        this.plugin = plugin;
        this.interval = plugin.getConfig().getInt("clearlag.interval", 300);
        this.threshold = plugin.getConfig().getInt("clearlag.threshold", 500);
        this.warnings = plugin.getConfig().getIntegerList("clearlag.warnings");
        this.timeLeft = interval;
    }

    @Override
    public void run() {
        // 1. Threshold Detection (If too many items pile up, force a clear instantly)
        int currentItems = countGroundItems();
        if (currentItems >= threshold) {
            int cleared = plugin.clearGroundItems();
            Bukkit.broadcastMessage(plugin.getMsg("cleardrops-threshold").replace("%count%", String.valueOf(cleared)));
            timeLeft = interval; // Reset the timer
            return;
        }

        // 2. Warning Broadcasts
        if (warnings.contains(timeLeft)) {
            Bukkit.broadcastMessage(plugin.getMsg("cleardrops-warning").replace("%time%", String.valueOf(timeLeft)));
        }

        // 3. Scheduled Clear Execution
        if (timeLeft <= 0) {
            int cleared = plugin.clearGroundItems();
            Bukkit.broadcastMessage(plugin.getMsg("cleardrops-cleared").replace("%count%", String.valueOf(cleared)));
            timeLeft = interval; // Loop back
            return;
        }

        timeLeft--;
    }

    // Helper method to just count the items without removing them
    private int countGroundItems() {
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntitiesByClasses(Item.class)) {
                count++;
            }
        }
        return count;
    }
}