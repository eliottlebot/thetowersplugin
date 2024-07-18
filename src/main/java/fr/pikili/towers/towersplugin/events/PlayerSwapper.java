package fr.pikili.towers.towersplugin.events;

import fr.pikili.towers.towersplugin.items.ItemManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
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

import java.util.Objects;

public class PlayerSwapper implements Listener {

    private final JavaPlugin plugin;

    public PlayerSwapper(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerSwap(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                event.getItem() != null &&
                Objects.equals(event.getItem().getItemMeta(), ItemManager.playerSwapper.getItemMeta())) {

            Player player = event.getPlayer();

            if (!player.getScoreboardTags().contains("Teleporting")) {
                Player nearestPlayer = null;
                double nearestDistance = Double.MAX_VALUE;

                for (Entity entity : player.getNearbyEntities(30, 30, 30)) {
                    if (entity instanceof Player) {
                        Player nearbyPlayer = (Player) entity;
                        double distance = player.getLocation().distance(nearbyPlayer.getLocation());

                        // Vérifier que le joueur ne swap pas avec un joueur de la même équipe
                        if (!isSameTeam(player, nearbyPlayer) && distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestPlayer = nearbyPlayer;
                        }
                    }
                }

                if (nearestPlayer != null) {
                    player.sendMessage(ChatColor.DARK_PURPLE + "Echange de position avec " + nearestPlayer.getName() + ChatColor.DARK_PURPLE + "...");
                    Player finalNearestPlayer = nearestPlayer;
                    teleportStarts(player, finalNearestPlayer);
                    new BukkitRunnable() {
                        int count = 0;

                        @Override
                        public void run() {
                            count++;
                            if (count >= 40) { // Arrêter après 2 secondes (2 * 20 ticks)
                                cancel();
                                swapLocations(player, finalNearestPlayer);
                            }
                        }
                    }.runTaskTimer(plugin, 0, 1); // Exécute toutes les 1 tick (20 fois par seconde)

                    //Retirer un item de la stack
                    reduceItemStack(event.getItem(), player);

                } else {
                    player.sendMessage(ChatColor.DARK_PURPLE + "Aucun joueur à proximité pour échanger.");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.5F);
                }
            } else {
                player.sendMessage(ChatColor.DARK_PURPLE + "Vous êtes déjà en téléportation !");
            }
        }
    }

    private boolean isSameTeam(Player player1, Player player2) {
        if (player1.getScoreboard().getEntryTeam(player1.getName()) != null &&
                player1.getScoreboard().getEntryTeam(player2.getName()) != null) {
            return Objects.equals(player1.getScoreboard().getEntryTeam(player1.getName()), player1.getScoreboard().getEntryTeam(player2.getName()));
        }
        return false;
    }


    private void teleportStarts(Player player, Player nearestPlayer) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 40, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 1));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 0.75F);

        nearestPlayer.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 40, 1));
        nearestPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
        nearestPlayer.playSound(nearestPlayer.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 0.5F);

        player.addScoreboardTag("Teleporting");

        nearestPlayer.addScoreboardTag("Teleporting");
    }

    private void teleportEnds(Player player, Player nearestPlayer) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.25F);
        nearestPlayer.playSound(nearestPlayer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.25F);
        player.removeScoreboardTag("Teleporting");
        nearestPlayer.removeScoreboardTag("Teleporting");
    }

    private void swapLocations(Player player, Player nearestPlayer) {
        Location location = player.getLocation();
        Location swapLocation = nearestPlayer.getLocation();
        player.teleport(swapLocation);
        nearestPlayer.teleport(location);
        teleportEnds(player, nearestPlayer);
    }

    private void reduceItemStack(ItemStack item, Player player) {
        int amount = item.getAmount();
        if (amount > 1) {
            item.setAmount(amount - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
    }
}
