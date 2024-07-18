package fr.pikili.towers.towersplugin.events;

import fr.pikili.towers.towersplugin.GameManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class TowersMainEvents implements Listener {

    private final JavaPlugin plugin;
    private final GameManager manager;

    public TowersMainEvents(JavaPlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.manager = gameManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(event.getPlayer().getScoreboardTags().contains("TowersTeamSelection")) {
            event.getPlayer().getScoreboardTags().remove("TowersTeamSelection");
            // Faire quitter l'équipe du joueur s'il en a une
            Team team = event.getPlayer().getScoreboard().getEntryTeam(event.getPlayer().getName());
            if (team != null) {
                team.removeEntry(event.getPlayer().getName());
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(event.getPlayer().getWorld().getName().equals(plugin.getConfig().getString("map_towers"))){
            if(event.getPlayer().getScoreboardTags().contains("TowersGame")){
                // Attendre 1 tick avant de téléporter et appliquer l'effet de potion
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        respawn(event.getPlayer(), 100, false);
                        event.getPlayer().setScoreboard(manager.getTowersScoreboard());
                    }
                }.runTaskLater(plugin, 1);
            }
            else{
                event.getPlayer().teleport(Objects.requireNonNull(event.getPlayer().getBedSpawnLocation()));
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.getScoreboardTags().contains("TowersGame")) {
            // Attendre 1 tick avant de téléporter et appliquer l'effet de potion
            new BukkitRunnable() {
                @Override
                public void run() {
                    respawn(player, 20, false);
                }
            }.runTaskLater(plugin, 1);

            // Utilisez setDeathMessage uniquement ici
            event.setDeathMessage(setTowersDeathMessage(event));
        }
    }

    public String setTowersDeathMessage(PlayerDeathEvent event) {

        Player deceased = event.getEntity();
        String deathMessage;

        if (deceased.getLastDamageCause() != null) {
            EntityDamageEvent.DamageCause cause = deceased.getLastDamageCause().getCause();

            switch (cause) {
                case VOID:
                    deathMessage = deceased.getName() + " est tombé dans le vide";
                    break;
                case ENTITY_ATTACK:
                case ENTITY_SWEEP_ATTACK:
                    if (event.getEntity().getKiller() != null) {
                        Player killer = event.getEntity().getKiller();
                        deathMessage = killer.getName() + " a hagar " + deceased.getName();
                    } else {
                        deathMessage = deceased.getName() + " est mort";
                    }
                    break;
                case PROJECTILE:
                    if (event.getEntity().getKiller() != null) {
                        Player killer = event.getEntity().getKiller();
                        deathMessage = killer.getName() + " a shooté " + deceased.getName();
                    } else {
                        deathMessage = deceased.getName() + " est mort";
                    }
                    break;
                default:
                    deathMessage = deceased.getName() + " est mort.";
                    break;
            }
        } else {
            deathMessage = deceased.getName() + " est mort.";
        }

        return deathMessage;
    }

    public void respawn(Player player, Integer time, Boolean hasJoined) {
        // Faire respawn le joueur dans son spawn
        if (player.getScoreboardTags().contains("TowersBlueTeam")) {
            teleportAndEquipPlayer(player, plugin.getConfig().getDouble("blue_team_spawn_x"),
                    plugin.getConfig().getDouble("blue_team_spawn_y"),
                    plugin.getConfig().getDouble("blue_team_spawn_z"), 90, Color.BLUE, time, hasJoined);
        } else if (player.getScoreboardTags().contains("TowersRedTeam")) {
            teleportAndEquipPlayer(player, plugin.getConfig().getDouble("red_team_spawn_x"),
                    plugin.getConfig().getDouble("red_team_spawn_y"),
                    plugin.getConfig().getDouble("red_team_spawn_z"), -90, Color.RED, time, hasJoined);
        }
    }

    private void teleportAndEquipPlayer(Player player, double x, double y, double z, float yaw, Color color, Integer time, Boolean hasJoined) {
        // Téléporter le joueur
        Location location = new Location(Objects.requireNonNull(Bukkit.getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers")))), x, y, z, yaw, 0);
        player.teleport(location);

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                // Répéter la téléportation pour s'assurer que le joueur reste sur place
                player.teleport(location);
                if (count == 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, time, 1));
                }
                count++;
                if (count >= time) { // Arrêter après le temps défini
                    // Arrêter la téléportation en boucle
                    cancel();

                    // Envoyer un message au joueur
                    if (!hasJoined) {
                        equipPlayer(player, color);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Exécute toutes les 1 tick (20 fois par seconde)
    }

    private void equipPlayer(Player player, Color color) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setColor(color);
        helmet.setItemMeta(meta);
        chestplate.setItemMeta(meta);
        leggings.setItemMeta(meta);
        boots.setItemMeta(meta);

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }
}
