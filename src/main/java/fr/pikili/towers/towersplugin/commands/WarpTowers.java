package fr.pikili.towers.towersplugin.commands;

import fr.pikili.towers.towersplugin.GameManager;
import fr.pikili.towers.towersplugin.map.GameMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WarpTowers implements CommandExecutor {

    private final GameMap map;
    private final GameManager manager;

    public WarpTowers(GameMap map, GameManager gameManager) {
        this.map = map;
        manager=gameManager;
    }

    /*
    * Teleports the players to the lobby deleting all his tags*/
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, 100, false, false));
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setTotalExperience(0);

            // Teleport the player
            player.teleport(new Location(Bukkit.getWorld(map.getWorld().getName()), 8.5, -51, 8.5));

            // Supprimer les effets de potions du joueur
            player.getActivePotionEffects().clear();
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            // Add the player to the team TowersTeamSelection
            player.addScoreboardTag("TowersTeamSelection");

            player.getInventory().clear();

            player.setBedSpawnLocation(new Location(Bukkit.getWorld(map.getWorld().getName()), 8.5, -51, 8.5), true);

            return true;
        }
        else{
            sender.sendMessage("Vous ne pouvez pas executer cette commande !");
            return false;
        }
    }

}
