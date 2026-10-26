package com.nolimits.listener;

import com.nolimits.NoLimitsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

/** Chaque niveau de Protection porté ajoute des coeurs permanents. */
public final class ProtectionHeartsListener implements Listener {

    private final NoLimitsPlugin plugin;
    private final NamespacedKey modifierKey;
    private BukkitTask task;

    public ProtectionHeartsListener(NoLimitsPlugin plugin) {
        this.plugin = plugin;
        this.modifierKey = new NamespacedKey(plugin, "protection_hearts");
    }

    public void start() {
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                update(p);
            }
        }, 20L, 20L);
    }

    public void stop() {
        if (task != null) task.cancel();
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            clear(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        update(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> update(event.getPlayer()));
    }

    private void clear(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;
        attr.getModifiers().stream()
                .filter(m -> modifierKey.equals(m.getKey()))
                .toList()
                .forEach(attr::removeModifier);
    }

    private void update(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        if (!plugin.getConfig().getBoolean("protection-hearts.enabled", true)) {
            clear(player);
            return;
        }

        int levels = 0;
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (piece != null) {
                levels += piece.getEnchantmentLevel(Enchantment.PROTECTION);
            }
        }

        double perLevel = plugin.getConfig().getDouble("protection-hearts.hearts-per-level", 0.5);
        double maxBonus = plugin.getConfig().getDouble("protection-hearts.max-bonus-hearts", 30);
        double bonusHearts = Math.min(maxBonus, levels * perLevel);
        double bonusHealth = bonusHearts * 2.0;

        AttributeModifier existing = attr.getModifiers().stream()
                .filter(m -> modifierKey.equals(m.getKey()))
                .findFirst()
                .orElse(null);

        if (existing != null && existing.getAmount() == bonusHealth) return;

        clear(player);
        if (bonusHealth > 0) {
            attr.addModifier(new AttributeModifier(
                    modifierKey, bonusHealth, AttributeModifier.Operation.ADD_NUMBER));
        }
    }
}
