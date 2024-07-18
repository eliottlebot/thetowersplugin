package fr.pikili.towers.towersplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class JoinTowersRedTeam implements CommandExecutor {

    private GameManager gameManager;

    public JoinTowersRedTeam(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        Scoreboard scoreboard = gameManager.getTowersScoreboard();
        Team redTeam = scoreboard.getTeam("TowersRedTeam");
        if (redTeam == null) {
            redTeam = scoreboard.registerNewTeam("TowersRedTeam");
            redTeam.setColor(ChatColor.BLUE);
        }

        //Si le joueur n'est pas dans l'équipe
        if(!redTeam.getEntries().contains(player.getName())) {
            redTeam.addEntry(player.getName());

            //Envoyer un message à tous les joueurs qui ont le scoreboardTag "TowersTeamSelection"
            String message = ChatColor.GOLD + "[The Towers] " + ChatColor.RED + player.getName() + ChatColor.GRAY + " a rejoint l'équipe rouge!";
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getScoreboardTags().contains("TowersTeamSelection")) {
                    onlinePlayer.sendMessage(message);
                }
            }

            // Create a DustOptions object with blue color
            DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 0, 0), 1);

            // Spawn blue dust particles at the player's location
            player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(), 50, 0.2, 1, 0.2, dustOptions);
            gameManager.tryToStartGame();
        }




        return true;
    }
}

