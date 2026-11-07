package com.sasha.bearprison;

import com.sasha.beareconomy.BearEconomy;
import com.sasha.bearprison.managers.CombatManager;
import com.sasha.bearprison.tasks.NametagTask;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class BearPrison extends JavaPlugin {

    private static BearPrison instance;
    private LuckPerms luckPerms;
    private BearEconomy economy;
    private CombatManager combatManager;

    // Cooldown variables
    private org.bukkit.configuration.file.FileConfiguration cooldownsConfig;
    private java.io.File cooldownsFile;
    private NametagTask nametagTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize the cooldowns.yml file
        setupCooldowns();

        // Hook LuckPerms
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) luckPerms = provider.getProvider();

        // Hook BearEconomy
        if (getServer().getPluginManager().getPlugin("BearEconomy") != null) {
            economy = (BearEconomy) getServer().getPluginManager().getPlugin("BearEconomy");
        }

        this.combatManager = new CombatManager(this);
        getServer().getPluginManager().registerEvents(new com.sasha.bearprison.listeners.CombatListener(this, nametagTask), this);
        getCommand("combat-timer").setExecutor(new com.sasha.bearprison.commands.CombatTimerCommand(this));

        // Register Events
        getServer().getPluginManager().registerEvents(new com.sasha.bearprison.listeners.PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new com.sasha.bearprison.listeners.InventoryListener(this), this); // Add this line!
        getServer().getPluginManager().registerEvents(new com.sasha.bearprison.listeners.SignColorListener(this), this);
        // Register Commands
        getCommand("rankup").setExecutor(new com.sasha.bearprison.commands.RankupCommand(this));
        getCommand("ranks").setExecutor(new com.sasha.bearprison.commands.RanksCommand(this));
        getCommand("cleardrops").setExecutor(new com.sasha.bearprison.commands.ClearDropsCommand(this));
        getCommand("condense").setExecutor(new com.sasha.bearprison.commands.CondenseCommand(this));
        getCommand("fix").setExecutor(new com.sasha.bearprison.commands.FixCommand(this));
        getCommand("rankupmax").setExecutor(new com.sasha.bearprison.commands.RankupMaxCommand(this));

        // Start the ClearLagg Task
        if (getConfig().getBoolean("clearlag.enabled", true)) {
            new com.sasha.bearprison.tasks.ClearLaggTask(this).runTaskTimer(this, 20L, 20L); // Runs every 1 second (20 ticks)
        }

        new com.sasha.bearprison.tasks.RankupReminderTask(this).runTaskTimer(this, 100L, 100L);
        new com.sasha.bearprison.tasks.TablistTask(this).runTaskTimer(this, 10L, 10L);

        nametagTask = new com.sasha.bearprison.tasks.NametagTask(this);
        nametagTask.runTaskTimer(this, 10L, 10L);

        getServer().getPluginManager().registerEvents(new com.sasha.bearprison.listeners.CombatListener(this, nametagTask), this);

        getLogger().info("BearPrison enabled! Economy, LuckPerms, and Cooldowns hooked.");
    }

    public LuckPerms getLuckPerms() { return luckPerms; }
    public BearEconomy getEconomy() { return economy; }

    public String getMsg(String path) {
        String prefix = getConfig().getString("messages.prefix", "&8[&eBearPrison&8] ");
        String msg = getConfig().getString("messages." + path, "&cMissing: " + path);
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    // Utility method to clear all dropped items globally
    public int clearGroundItems() {
        int count = 0;
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntitiesByClasses(org.bukkit.entity.Item.class)) {
                entity.remove();
                count++;
            }
        }
        return count;
    }

    // --- Cooldown File Management ---

    public void setupCooldowns() {
        cooldownsFile = new java.io.File(getDataFolder(), "cooldowns.yml");
        if (!cooldownsFile.exists()) {
            cooldownsFile.getParentFile().mkdirs();
            try {
                cooldownsFile.createNewFile(); // Creates a blank file programmatically
            } catch (java.io.IOException e) {
                getLogger().severe("Could not create cooldowns.yml!");
            }
        }
        cooldownsConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(cooldownsFile);
    }

    public org.bukkit.configuration.file.FileConfiguration getCooldownsConfig() {
        return cooldownsConfig;
    }

    public void saveCooldownsConfig() {
        try {
            cooldownsConfig.save(cooldownsFile);
        } catch (java.io.IOException e) {
            getLogger().severe("Could not save cooldowns.yml!");
        }
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public String getRawMsg(String path) {
        String msg = getConfig().getString("messages." + path, "&cMissing: " + path);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}