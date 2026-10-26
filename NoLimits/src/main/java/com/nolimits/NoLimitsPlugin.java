package com.nolimits;

import com.nolimits.command.NoLimitsCommand;
import com.nolimits.listener.AnvilListener;
import com.nolimits.listener.EnchantTableListener;
import com.nolimits.listener.InfinityListener;
import com.nolimits.listener.ProtectionHeartsListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class NoLimitsPlugin extends JavaPlugin {

    private ProtectionHeartsListener heartsListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantTableListener(this), this);
        getServer().getPluginManager().registerEvents(new InfinityListener(this), this);

        heartsListener = new ProtectionHeartsListener(this);
        getServer().getPluginManager().registerEvents(heartsListener, this);
        heartsListener.start();

        PluginCommand cmd = getCommand("nolimits");
        if (cmd != null) {
            NoLimitsCommand handler = new NoLimitsCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
    }

    @Override
    public void onDisable() {
        if (heartsListener != null) {
            heartsListener.stop();
        }
    }

    /** Niveau max configuré, borné à [1, 255]. */
    public int maxLevel() {
        return Math.max(1, Math.min(255, getConfig().getInt("max-level", 255)));
    }
}
