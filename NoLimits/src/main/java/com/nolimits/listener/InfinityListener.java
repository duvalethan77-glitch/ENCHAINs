package com.nolimits.listener;

import com.nolimits.NoLimitsPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Infinity en main secondaire : pommes d'or et totems ne sont jamais consommés. */
public final class InfinityListener implements Listener {

    private final NoLimitsPlugin plugin;

    public InfinityListener(NoLimitsPlugin plugin) {
        this.plugin = plugin;
    }

    private Set<Material> allowed() {
        Set<Material> set = new HashSet<>();
        List<String> names = plugin.getConfig().getStringList("infinity-offhand.items");
        for (String n : names) {
            Material m = Material.matchMaterial(n);
            if (m != null) set.add(m);
        }
        return set;
    }

    private boolean eligible(ItemStack item) {
        if (!plugin.getConfig().getBoolean("infinity-offhand.enabled", true)) return false;
        if (item == null || item.getType() == Material.AIR) return false;
        if (!allowed().contains(item.getType())) return false;
        return item.getEnchantmentLevel(Enchantment.INFINITY) > 0;
    }

    private void restoreNextTick(Player player, ItemStack snapshot) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.getInventory().setItemInOffHand(snapshot);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) return;
        Player player = event.getPlayer();
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (!eligible(offhand)) return;

        restoreNextTick(player, offhand.clone());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        EquipmentSlot hand = event.getHand();
        if (hand != null && hand != EquipmentSlot.OFF_HAND) return;

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() != Material.TOTEM_OF_UNDYING || !eligible(offhand)) return;

        restoreNextTick(player, offhand.clone());
    }
}
