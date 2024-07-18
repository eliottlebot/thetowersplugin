package fr.pikili.towers.towersplugin.commands;

import fr.pikili.towers.towersplugin.map.GameMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class ReloadTowers implements CommandExecutor {

    private GameMap map;
    private JavaPlugin plugin;

    public ReloadTowers(JavaPlugin plugin, GameMap map) {
        this.map = map;
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!(sender.isOp())) {
            sender.sendMessage("Il n'y a que les opérateurs qui peuvent utiliser la commande !");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN+"Reset de la map ...");

        Location lobby = new Location(Bukkit.getWorld("world"), 244.5, 52., 1348.5, 90, 0);
        for(Player p : Bukkit.getOnlinePlayers()){
            p.teleport(lobby);
            p.setBedSpawnLocation(lobby);
        }

        if(map.restoreFromSource()){
            sender.sendMessage(ChatColor.GREEN+"Reset de la map effectué !");
        }
        else{
            sender.sendMessage(ChatColor.RED+"Failed to reset map ...");
        }

        Objects.requireNonNull(Bukkit.getServer().getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers")))).setTime(0L);
        Objects.requireNonNull(Bukkit.getServer().getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers")))).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        Objects.requireNonNull(Bukkit.getServer().getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers")))).setClearWeatherDuration(36000);
        Objects.requireNonNull(Bukkit.getServer().getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers")))).setGameRule(GameRule.NATURAL_REGENERATION, false);
        Objects.requireNonNull(Bukkit.getServer().getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers")))).setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        return true;
    }

}
