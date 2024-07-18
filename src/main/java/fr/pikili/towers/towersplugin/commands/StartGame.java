package fr.pikili.towers.towersplugin.commands;

import fr.pikili.towers.towersplugin.GameManager;
import fr.pikili.towers.towersplugin.Generators;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.Objects;

public class StartGame implements CommandExecutor {
    private final GameManager gameManager;

    public StartGame(JavaPlugin plugin, GameManager gameManager) {
        this.gameManager = gameManager;
    }

    //This command is optionnal, it's just for tests
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || sender.isOp()) {
            gameManager.startGame();
            return true;
        }
        return false;
    }

}
