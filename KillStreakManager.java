package com.killstreak.managers;

import com.killstreak.KillStreakAbilities;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillStreakManager {

    private final KillStreakAbilities plugin;

    /** In-memory streak storage: UUID → current streak */
    private final Map<UUID, Integer> streaks = new HashMap<>();

    private File dataFile;
    private FileConfiguration data;

    public KillStreakManager(KillStreakAbilities plugin) {
        this.plugin = plugin;
        loadStreaks();
    }

    // ── Public API ───────────────────────────────────────────────────────────────

    /** Increment streak by 1 and return the new value. */
    public int increment(Player player) {
        int newStreak = streaks.merge(player.getUniqueId(), 1, Integer::sum);
        announceStreak(player, newStreak);
        return newStreak;
    }

    /** Reset streak to 0 (called on death). */
    public void reset(Player player) {
        streaks.put(player.getUniqueId(), 0);
    }

    /** Get current streak for a player (0 if none). */
    public int getStreak(Player player) {
        return streaks.getOrDefault(player.getUniqueId(), 0);
    }

    // ── Announce ─────────────────────────────────────────────────────────────────

    private void announceStreak(Player player, int streak) {
        // Only announce at notable milestones
        if (streak < 3 || streak % 5 != 0 && streak != 3) return;

        String raw = plugin.getConfig().getString("messages.streak-announce",
                "&e%player% &7is on a &c%streak% kill streak&7!");
        String msg = colorize(raw
                .replace("%player%", player.getName())
                .replace("%streak%", String.valueOf(streak)));

        plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(msg));
    }

    // ── Persistence ──────────────────────────────────────────────────────────────

    public void loadStreaks() {
        dataFile = new File(plugin.getDataFolder(), "streaks.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Cannot create streaks.yml");
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        if (data.isConfigurationSection("streaks")) {
            for (String key : data.getConfigurationSection("streaks").getKeys(false)) {
                streaks.put(UUID.fromString(key), data.getInt("streaks." + key));
            }
        }
    }

    public void saveStreaks() {
        streaks.forEach((uuid, streak) -> data.set("streaks." + uuid, streak));
        try { data.save(dataFile); } catch (IOException e) {
            plugin.getLogger().severe("Cannot save streaks.yml");
        }
    }

    // ── Util ─────────────────────────────────────────────────────────────────────

    private String colorize(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}
