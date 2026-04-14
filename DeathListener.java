package com.killstreak.listeners;

import com.killstreak.KillStreakAbilities;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final KillStreakAbilities plugin;

    public DeathListener(KillStreakAbilities plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // ── Victim: reset streak ─────────────────────────────────────────────────
        int lostStreak = plugin.getKillStreakManager().getStreak(victim);
        plugin.getKillStreakManager().reset(victim);
        plugin.getAbilityManager().removeGodMode(victim.getUniqueId());

        if (lostStreak >= 3) {
            String msg = colorize(plugin.getConfig().getString("messages.streak-reset",
                "&7Your kill streak has been &creset&7."));
            // Notify victim after respawn (schedule 1 tick later)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline()) {
                    victim.sendMessage(msg + ChatColor.GRAY + " (Lost " + lostStreak + " streak)");
                }
            }, 1L);
        }

        // ── Killer: increment streak ─────────────────────────────────────────────
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;
        if (killer.equals(victim)) return; // No self-kill credit

        int newStreak = plugin.getKillStreakManager().increment(killer);
        plugin.getAbilityManager().checkAndApply(killer, newStreak);

        // Notify killer of their current streak
        killer.sendMessage(colorize("&c&l" + newStreak + " Kill Streak! &7Keep it up!"));
        killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
