package fr.pikili.towers.towersplugin.events;

import fr.pikili.towers.towersplugin.items.Nuke;
import fr.pikili.towers.towersplugin.items.ItemManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChickenNuke implements Listener {

    private final List<Nuke> nukes = new ArrayList<>();
    private final JavaPlugin plugin;

    public ChickenNuke(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onNukeLaunch(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                event.getItem() != null &&
                Objects.equals(event.getItem().getItemMeta(), ItemManager.chickenNuke.getItemMeta())) {

            Player player = event.getPlayer();

            Location blockLocation = Objects.requireNonNull(player.getTargetBlockExact(40)).getLocation();
            blockLocation.add(0.5, 0, 0.5);

            // Créer et lancer une nouvelle nuke
            Nuke nuke = new Nuke(blockLocation, plugin);
            nukes.add(nuke);

            // Retirer un item de la stack
            int amount = event.getItem().getAmount();
            if (amount > 1) {
                event.getItem().setAmount(amount - 1);
            } else {
                player.getInventory().setItemInHand(new ItemStack(Material.AIR));
            }

            player.sendMessage(ChatColor.YELLOW + "Nuke lancée !!");
        }
    }
}
