package com.killstreak;

import com.killstreak.commands.KillStreakCommand;
import com.killstreak.listeners.DeathListener;
import com.killstreak.listeners.DamageListener;
import com.killstreak.managers.AbilityManager;
import com.killstreak.managers.KillStreakManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KillStreakAbilities extends JavaPlugin {

    private static KillStreakAbilities instance;
    private KillStreakManager killStreakManager;
    private AbilityManager abilityManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Init managers
        killStreakManager = new KillStreakManager(this);
        abilityManager   = new AbilityManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);

        // Register command + tab completer
        KillStreakCommand cmd = new KillStreakCommand(this);
        getCommand("killstreak").setExecutor(cmd);
        getCommand("killstreak").setTabCompleter(cmd);

        getLogger().info("KillStreakAbilities enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel all active god mode tasks / bossbars
        abilityManager.cleanup();

        // Persist streaks if enabled
        if (getConfig().getBoolean("save-streaks", true)) {
            killStreakManager.saveStreaks();
        }

        getLogger().info("KillStreakAbilities disabled.");
    }

    // ── Accessors ────────────────────────────────────────────────────────────────
    public static KillStreakAbilities getInstance() { return instance; }
    public KillStreakManager getKillStreakManager()  { return killStreakManager; }
    public AbilityManager    getAbilityManager()     { return abilityManager; }
}
