package com.nolimits.listener;

import com.nolimits.NoLimitsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public final class EnchantTableListener implements Listener {

    private final NoLimitsPlugin plugin;

    public EnchantTableListener(NoLimitsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() != InventoryType.ENCHANTING) return;
        if (!plugin.getConfig().getBoolean("enchanting-table.disabled", true)) return;

        event.setCancelled(true);
        if (event.getPlayer() instanceof Player player) {
            String msg = plugin.getConfig().getString("enchanting-table.message", "");
            if (msg != null && !msg.isEmpty()) {
                player.sendMessage(msg.replace('&', '§'));
            }
        }
    }
}
