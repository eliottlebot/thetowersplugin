package fr.pikili.towers.towersplugin.items;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    public static ItemStack meteor;
    public static ItemStack jumpBoots;
    public static ItemStack playerSwapper;
    public static ItemStack chickenNuke;

    public ItemManager() {
        init();
    }

    private void init() {
        createMeteor();
        createJumpBoots();
        createPlayerSwapper();
        createChickenNuke();
    }

    private static void createMeteor(){
        ItemStack item = new ItemStack(Material.SNOWBALL,1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bBoule de neige explosive");
        item.setItemMeta(meta);
        meteor = item;
    }

    private static void createJumpBoots(){
        ItemStack item = new ItemStack(Material.RABBIT_FOOT,5);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Jump Boots");
        item.setItemMeta(meta);
        jumpBoots=item;
    }

    private static void createPlayerSwapper(){
        ItemStack item = new ItemStack(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§5Swapper");

        List<String> lore = new ArrayList<>();
        lore.add("§7Echange de place avec le");
        lore.add("§7joueur le plus proche");
        meta.setLore(lore);

        // Masquer les informations de l'item de base
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        item.setItemMeta(meta);
        playerSwapper = item;
    }

    private static void createChickenNuke(){
        ItemStack item = new ItemStack(Material.FIREWORK_STAR,1);
        FireworkEffectMeta meta = (FireworkEffectMeta)item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW+"Chicken Nuke");

        List<String> lore = new ArrayList<>();
        lore.add("§7Fais apparaitre une bombe");
        lore.add("§7de poulets à l'endroit");
        lore.add("§7pointé");
        meta.setLore(lore);

        // Ajouter l'effet de feu d'artifice avec la couleur jaune
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(Color.YELLOW)
                .build();
        meta.setEffect(effect);

        // Masquer les informations de l'item de base
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        item.setItemMeta(meta);
        chickenNuke = item;
    }
}
