package fr.pikili.towers.towersplugin.events;

import fr.pikili.towers.towersplugin.items.ItemManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class RabbitFoot implements Listener {
    private JavaPlugin plugin;

    public RabbitFoot(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (event.getItem() != null) {
                if(event.getItem().getItemMeta().equals(ItemManager.jumpBoots.getItemMeta())) {
                    // Obtenir la direction du joueur
                    Player player = event.getPlayer();
                    Vector direction = player.getLocation().getDirection();

                    // Ajuster la direction pour propulser le joueur en l'air et en avant
                    direction.setY(0.5);  // Ajuster cette valeur pour changer le boost vertical
                    direction.multiply(2); // Ajuster cette valeur pour changer le boost horizontal

                    // Appliquer la nouvelle vélocité
                    player.setVelocity(direction);

                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.1, 0, 0.1, 0.1);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 0.5F);

                    // Retirer un item de la stack
                    int amount = event.getItem().getAmount();
                    if (amount > 1) {
                        event.getItem().setAmount(amount - 1);
                    } else {
                        player.getInventory().setItemInHand(new ItemStack(Material.AIR));
                    }
                }

            }
        }
    }

}
