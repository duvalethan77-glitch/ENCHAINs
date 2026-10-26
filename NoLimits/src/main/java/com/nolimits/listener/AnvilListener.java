package com.nolimits.listener;

import com.nolimits.NoLimitsPlugin;
import com.nolimits.util.Enchants;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public final class AnvilListener implements Listener {

    private final NoLimitsPlugin plugin;

    public AnvilListener(NoLimitsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepare(PrepareAnvilEvent event) {
        if (!plugin.getConfig().getBoolean("anvil.unrestricted", true)) return;

        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getFirstItem();
        ItemStack second = inv.getSecondItem();
        if (first == null || second == null) return;
        if (first.getType() == Material.AIR || second.getType() == Material.AIR) return;

        Map<Enchantment, Integer> toAdd = Enchants.read(second);
        if (toAdd.isEmpty()) return;

        ItemStack result = first.clone();
        boolean upgrade = plugin.getConfig().getBoolean("anvil.merge-same-level-upgrade", true);
        int max = plugin.maxLevel();

        for (Map.Entry<Enchantment, Integer> entry : toAdd.entrySet()) {
            Enchantment e = entry.getKey();
            int incoming = entry.getValue();
            int current = Enchants.levelOn(result, e);

            int level;
            if (current == incoming && upgrade) {
                level = current + 1;
            } else {
                level = Math.max(current, incoming);
            }
            level = Math.max(1, Math.min(max, level));
            Enchants.applyUnsafe(result, e, level);
        }

        // Renommage éventuel
        String rename = event.getView().getRenameText();
        if (rename != null && !rename.isBlank()) {
            ItemMeta meta = result.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(rename);
                result.setItemMeta(meta);
            }
        }

        event.setResult(result);
        event.getView().setMaximumRepairCost(1000);
        event.getView().setRepairCost(Math.max(1, plugin.getConfig().getInt("anvil.cost", 5)));
    }
}
