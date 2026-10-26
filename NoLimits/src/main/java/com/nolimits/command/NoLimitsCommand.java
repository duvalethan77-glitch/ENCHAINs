package com.nolimits.command;

import com.nolimits.NoLimitsPlugin;
import com.nolimits.util.Enchants;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NoLimitsCommand implements CommandExecutor, TabCompleter {

    private final NoLimitsPlugin plugin;

    public NoLimitsCommand(NoLimitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage("§aConfig rechargée.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande doit être utilisée en jeu.");
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            player.sendMessage("§cTiens un objet en main.");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            Enchantment e = Enchants.find(args[1]);
            if (e == null) {
                player.sendMessage("§cEnchantement inconnu : " + args[1]);
                return true;
            }
            ItemMeta meta = hand.getItemMeta();
            if (meta != null) {
                meta.removeEnchant(e);
                hand.setItemMeta(meta);
            }
            player.sendMessage("§aEnchantement retiré : " + e.getKey().getKey());
            return true;
        }

        if (args.length != 2) {
            player.sendMessage("§eUsage : /" + label + " <enchant> <niveau> | remove <enchant> | reload");
            return true;
        }

        Enchantment enchant = Enchants.find(args[0]);
        if (enchant == null) {
            player.sendMessage("§cEnchantement inconnu : " + args[0]);
            return true;
        }

        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            player.sendMessage("§cNiveau invalide : " + args[1]);
            return true;
        }
        level = Math.max(1, Math.min(plugin.maxLevel(), level));

        Enchants.applyUnsafe(hand, enchant, level);
        player.sendMessage("§a" + enchant.getKey().getKey() + " " + level + " appliqué.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String p = args[0].toLowerCase(Locale.ROOT);
            for (String s : List.of("remove", "reload")) {
                if (s.startsWith(p)) out.add(s);
            }
            for (String s : Enchants.allNames()) {
                if (s.startsWith(p)) out.add(s);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            String p = args[1].toLowerCase(Locale.ROOT);
            for (String s : Enchants.allNames()) {
                if (s.startsWith(p)) out.add(s);
            }
        }
        return out;
    }
}
