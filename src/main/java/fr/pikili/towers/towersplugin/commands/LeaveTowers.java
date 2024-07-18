package fr.pikili.towers.towersplugin.commands;

import fr.pikili.towers.towersplugin.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class LeaveTowers implements CommandExecutor {

    private GameManager gameManager;

    public LeaveTowers(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            player.sendMessage(ChatColor.LIGHT_PURPLE + "Vous avez quitté la partie !");

            // Supprimer le joueur de toutes les équipes
            Scoreboard scoreboard = gameManager.getTowersScoreboard();
            Set<Team> teamsToRemove = new HashSet<>();
            for (Team team : scoreboard.getTeams()) {
                if (team.hasEntry(player.getName())) {
                    teamsToRemove.add(team);
                }
            }
            for (Team team : teamsToRemove) {
                team.removeEntry(player.getName());
            }

            // Tp le joueur aux coordonnées 244.5, 52., 1348.5, 90, 0
            Location lobby = new Location(Bukkit.getWorld("world"), 244.5, 52., 1348.5, 90, 0);
            player.setBedSpawnLocation(lobby, true);
            player.teleport(lobby);
            player.getInventory().clear();

            // Supprimer tous les tags du joueur
            Set<String> tagsToRemove = new HashSet<>(player.getScoreboardTags());
            for (String tag : tagsToRemove) {
                player.removeScoreboardTag(tag);
            }

            return true;
        }
        return false;
    }
}
