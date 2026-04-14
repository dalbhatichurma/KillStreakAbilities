package com.killstreak.commands;

import com.killstreak.KillStreakAbilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KillStreakCommand implements CommandExecutor, TabCompleter {

    private final KillStreakAbilities plugin;
    private static final String PREFIX = ChatColor.RED + "" + ChatColor.BOLD + "KS " + ChatColor.DARK_GRAY + "» " + ChatColor.RESET;

    public KillStreakCommand(KillStreakAbilities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("killstreak.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {

            // /ks check <player>
            case "check" -> {
                if (args.length < 2) { sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /ks check <player>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(PREFIX + ChatColor.RED + "Player not found: " + args[1]); return true; }

                int streak = plugin.getKillStreakManager().getStreak(target);
                boolean godMode = plugin.getAbilityManager().isInGodMode(target.getUniqueId());

                sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                sender.sendMessage(ChatColor.YELLOW + "  Kill Streak: " + ChatColor.WHITE + target.getName());
                sender.sendMessage(ChatColor.GRAY  + "  Streak:    " + ChatColor.RED + streak + " kills");
                sender.sendMessage(ChatColor.GRAY  + "  God Mode:  " + (godMode ? ChatColor.GREEN + "ACTIVE" : ChatColor.RED + "inactive"));
                sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

            // /ks reset <player>
            case "reset" -> {
                if (args.length < 2) { sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /ks reset <player>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(PREFIX + ChatColor.RED + "Player not found: " + args[1]); return true; }

                plugin.getKillStreakManager().reset(target);
                plugin.getAbilityManager().removeGodMode(target.getUniqueId());

                sender.sendMessage(PREFIX + ChatColor.GREEN + "Reset streak for " + ChatColor.WHITE + target.getName());
                target.sendMessage(PREFIX + ChatColor.RED + "Your kill streak was reset by an admin.");
            }

            // /ks reload
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Config reloaded!");
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.YELLOW + "  KillStreakAbilities Commands");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.AQUA + "  /ks check <player>"  + ChatColor.GRAY + " — View player streak");
        sender.sendMessage(ChatColor.AQUA + "  /ks reset <player>"  + ChatColor.GRAY + " — Reset player streak");
        sender.sendMessage(ChatColor.AQUA + "  /ks reload"          + ChatColor.GRAY + " — Reload config");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("check", "reset", "reload");
        if (args.length == 2 && (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("reset"))) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
