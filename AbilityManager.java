package com.killstreak.managers;

import com.killstreak.KillStreakAbilities;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {

    private final KillStreakAbilities plugin;

    /** Players currently in god mode */
    private final Map<UUID, BossBar>    godBossBars = new HashMap<>();
    private final Map<UUID, BukkitTask> godTasks    = new HashMap<>();

    public AbilityManager(KillStreakAbilities plugin) {
        this.plugin = plugin;
    }

    // ── Entry Point ──────────────────────────────────────────────────────────────

    /**
     * Called every time a player's streak increments.
     * Checks milestones and applies the matching ability.
     */
    public void checkAndApply(Player player, int streak) {
        if (isEnabled("god-mode") && streak == getKills("god-mode")) {
            applyGodMode(player);
        } else if (isEnabled("strength") && streak == getKills("strength")) {
            applyStrength(player);
        } else if (isEnabled("speed") && streak == getKills("speed")) {
            applySpeed(player);
        }
    }

    // ── Speed ────────────────────────────────────────────────────────────────────

    private void applySpeed(Player player) {
        int amp  = plugin.getConfig().getInt("streaks.speed.amplifier", 1);
        int secs = plugin.getConfig().getInt("streaks.speed.duration-seconds", 30);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, secs * 20, amp, false, true, true));

        sendTitle(player,
            "&b&lSPEED BOOST",
            "&73 Kill Streak — Speed " + toRoman(amp + 1) + " for " + secs + "s");
        playFeedback(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, Particle.CRIT, ChatColor.AQUA);
    }

    // ── Strength ─────────────────────────────────────────────────────────────────

    private void applyStrength(Player player) {
        int amp  = plugin.getConfig().getInt("streaks.strength.amplifier", 0);
        int secs = plugin.getConfig().getInt("streaks.strength.duration-seconds", 30);

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, secs * 20, amp, false, true, true));

        sendTitle(player,
            "&c&lSTRENGTH",
            "&75 Kill Streak — Strength " + toRoman(amp + 1) + " for " + secs + "s");
        playFeedback(player, Sound.ENTITY_BLAZE_SHOOT, Particle.FLAME, ChatColor.RED);
    }

    // ── God Mode ─────────────────────────────────────────────────────────────────

    private void applyGodMode(Player player) {
        int duration = plugin.getConfig().getInt("streaks.god-mode.duration-seconds", 10);
        UUID uuid = player.getUniqueId();

        // Cancel any existing god mode for this player
        removeGodMode(uuid);

        sendTitle(player,
            "&6&l⚡ GOD MODE ⚡",
            "&e10 Kill Streak — Invincible for " + duration + "s!");
        playFeedback(player, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, Particle.TOTEM_OF_UNDYING, ChatColor.GOLD);

        // BossBar countdown
        BossBar bar = null;
        if (plugin.getConfig().getBoolean("feedback.bossbar-enabled", true)) {
            bar = Bukkit.createBossBar(
                ChatColor.GOLD + "⚡ GOD MODE — " + duration + "s remaining",
                BarColor.YELLOW, BarStyle.SEGMENTED_10);
            bar.addPlayer(player);
            bar.setProgress(1.0);
        }

        final BossBar finalBar = bar;
        godBossBars.put(uuid, finalBar);

        // Countdown task (runs every second)
        final int[] remaining = {duration};
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) { removeGodMode(uuid); cancel(); return; }

                remaining[0]--;

                // Update bossbar
                if (finalBar != null) {
                    finalBar.setProgress(Math.max(0.0, (double) remaining[0] / duration));
                    finalBar.setTitle(ChatColor.GOLD + "⚡ GOD MODE — " + remaining[0] + "s remaining");
                }

                // ActionBar pulse
                if (plugin.getConfig().getBoolean("feedback.sounds-enabled", true)) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.GOLD + "⚡ " + ChatColor.YELLOW + "GOD MODE ACTIVE "
                            + ChatColor.GRAY + "— " + remaining[0] + "s"));
                }

                if (remaining[0] <= 0) {
                    removeGodMode(uuid);
                    if (p.isOnline()) {
                        String msg = colorize(plugin.getConfig().getString(
                            "messages.god-mode-end", "&cGod Mode &7has worn off!"));
                        p.sendMessage(msg);
                        p.sendTitle(
                            ChatColor.RED + "GOD MODE ENDED",
                            ChatColor.GRAY + "You are vulnerable again",
                            5, 40, 10);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        godTasks.put(uuid, task);
    }

    /** Remove god mode state for a player (called on death or timer end). */
    public void removeGodMode(UUID uuid) {
        BukkitTask task = godTasks.remove(uuid);
        if (task != null && !task.isCancelled()) task.cancel();

        BossBar bar = godBossBars.remove(uuid);
        if (bar != null) bar.removeAll();
    }

    /** Returns true if the player is currently in god mode. */
    public boolean isInGodMode(UUID uuid) {
        return godTasks.containsKey(uuid);
    }

    /** Cancel everything on plugin disable. */
    public void cleanup() {
        godTasks.values().forEach(t -> { if (!t.isCancelled()) t.cancel(); });
        godBossBars.values().forEach(BossBar::removeAll);
        godTasks.clear();
        godBossBars.clear();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private void sendTitle(Player player, String title, String subtitle) {
        if (!plugin.getConfig().getBoolean("feedback.titles-enabled", true)) return;
        player.sendTitle(colorize(title), colorize(subtitle), 5, 50, 10);
    }

    private void playFeedback(Player player, Sound sound, Particle particle, ChatColor color) {
        if (plugin.getConfig().getBoolean("feedback.sounds-enabled", true)) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
        if (plugin.getConfig().getBoolean("feedback.particles-enabled", true)) {
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), 60, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private boolean isEnabled(String key) {
        return plugin.getConfig().getBoolean("streaks." + key + ".enabled", true);
    }

    private int getKills(String key) {
        return plugin.getConfig().getInt("streaks." + key + ".kills-required", 999);
    }

    private String toRoman(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III";
            case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(n);
        };
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
