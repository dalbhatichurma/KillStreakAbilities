package com.killstreak.listeners;

import com.killstreak.KillStreakAbilities;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    private final KillStreakAbilities plugin;

    public DamageListener(KillStreakAbilities plugin) {
        this.plugin = plugin;
    }

    /**
     * Cancel ALL incoming damage while a player is in god mode.
     * Uses HIGH priority so it runs after most other plugins but
     * before MONITOR listeners.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!plugin.getAbilityManager().isInGodMode(victim.getUniqueId())) return;

        // Cancel the damage
        event.setCancelled(true);

        // Visual feedback — show shield-like particles
        if (plugin.getConfig().getBoolean("feedback.particles-enabled", true)) {
            victim.getWorld().spawnParticle(
                Particle.TOTEM_OF_UNDYING,
                victim.getLocation().add(0, 1, 0),
                15, 0.4, 0.4, 0.4, 0.05);
        }

        // Notify attacker if it was a player
        if (event instanceof EntityDamageByEntityEvent dmgEvent
                && dmgEvent.getDamager() instanceof Player attacker) {
            attacker.sendMessage(ChatColor.GOLD + "⚡ " + ChatColor.YELLOW
                + victim.getName() + ChatColor.GRAY + " is in God Mode!");
        }
    }
}
