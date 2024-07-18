package fr.pikili.towers.towersplugin.commands;

import fr.pikili.towers.towersplugin.items.ItemManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class GetItems implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player) || !(sender.isOp())) {
            sender.sendMessage("Il n'y a que les opérateurs qui peuvent utiliser la commande !");
            return true;
        }
        Player player = (Player) sender;
        if(cmd.getName().equalsIgnoreCase("giveitems")) {
            player.getInventory().addItem(ItemManager.meteor);
            player.getInventory().addItem(ItemManager.jumpBoots);
            player.getInventory().addItem(ItemManager.playerSwapper);
            player.getInventory().addItem(ItemManager.chickenNuke);
        }
        return false;
    }
}
