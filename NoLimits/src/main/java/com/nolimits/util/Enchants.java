package com.nolimits.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Enchants {

    private Enchants() {}

    /** Trouve un enchantement par son nom ("sharpness" ou "minecraft:sharpness"). */
    public static Enchantment find(String name) {
        if (name == null) return null;
        String n = name.toLowerCase(Locale.ROOT).trim();
        NamespacedKey key = n.contains(":") ? NamespacedKey.fromString(n) : NamespacedKey.minecraft(n);
        return key == null ? null : Registry.ENCHANTMENT.get(key);
    }

    public static List<String> allNames() {
        return Registry.ENCHANTMENT.stream()
                .map(e -> e.getKey().getKey())
                .sorted()
                .toList();
    }

    /** Enchantements portés par l'objet (stockés si c'est un livre). */
    public static Map<Enchantment, Integer> read(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storage) {
            return storage.getStoredEnchants();
        }
        return item.getEnchantments();
    }

    /** Ajoute un enchantement sans aucune restriction (objet ou livre). */
    public static void applyUnsafe(ItemStack item, Enchantment enchant, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (meta instanceof EnchantmentStorageMeta storage) {
            storage.addStoredEnchant(enchant, level, true);
            item.setItemMeta(storage);
        } else {
            meta.addEnchant(enchant, level, true);
            item.setItemMeta(meta);
        }
    }

    public static int levelOn(ItemStack item, Enchantment enchant) {
        return read(item).getOrDefault(enchant, 0);
    }
}
