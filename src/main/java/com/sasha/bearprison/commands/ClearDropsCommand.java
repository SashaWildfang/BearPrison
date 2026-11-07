package com.sasha.bearprison.commands;

import com.sasha.bearprison.BearPrison;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ClearDropsCommand implements CommandExecutor {
    private final BearPrison plugin;

    public ClearDropsCommand(BearPrison plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bn.bearprison.cleardrops")) {
            sender.sendMessage(plugin.getMsg("no-permission"));
            return true;
        }

        int cleared = plugin.clearGroundItems();
        sender.sendMessage(plugin.getMsg("cleardrops-cleared").replace("%count%", String.valueOf(cleared)));
        return true;
    }
}