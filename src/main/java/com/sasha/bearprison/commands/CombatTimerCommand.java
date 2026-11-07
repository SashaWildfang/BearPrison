package com.sasha.bearprison.commands;

import com.sasha.bearprison.BearPrison;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CombatTimerCommand implements CommandExecutor {

    private final BearPrison plugin;

    public CombatTimerCommand(BearPrison plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getRawMsg("not-player"));
                return true;
            }

            int timeLeft = plugin.getCombatManager().getRemainingSeconds(player);
            if (timeLeft > 0) {
                player.sendMessage(plugin.getMsg("combat-timer-self").replace("%time%", String.valueOf(timeLeft)));
            } else {
                player.sendMessage(plugin.getMsg("combat-not-in-combat"));
            }
        } else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getMsg("player-not-found").replace("%player%", args[0]));
                return true;
            }

            int timeLeft = plugin.getCombatManager().getRemainingSeconds(target);
            if (timeLeft > 0) {
                sender.sendMessage(plugin.getMsg("combat-timer-other")
                        .replace("%player%", target.getName())
                        .replace("%time%", String.valueOf(timeLeft)));
            } else {
                sender.sendMessage(plugin.getMsg("combat-other-not-in-combat").replace("%player%", target.getName()));
            }
        }
        return true;
    }
}