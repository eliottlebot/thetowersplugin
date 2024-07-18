package fr.pikili.towers.towersplugin.commands;

import fr.pikili.towers.towersplugin.GameManager;
import fr.pikili.towers.towersplugin.Generators;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.Objects;

public class StopGame implements CommandExecutor {

    private GameManager gameManager;

    public StopGame(JavaPlugin plugin, GameManager gameManager) {
        this.gameManager = gameManager;
    }

    //This command is optionnal, it's just for tests
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("stoptowers")) {
            // Verify that the command comes from a player who is not an operator and reject the command
            if (sender instanceof Player && !sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "Cette commande ne peut être exécutée que par un opérateur ou la console !");
                return false;
            }
            gameManager.stopGame();
        }
        return true;
    }

}
