package fr.pikili.towers.towersplugin;

import fr.pikili.towers.towersplugin.commands.*;
import fr.pikili.towers.towersplugin.events.*;

import fr.pikili.towers.towersplugin.items.ItemManager;
import fr.pikili.towers.towersplugin.map.GameMap;
import fr.pikili.towers.towersplugin.map.TowersGameMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class TowersPlugin extends JavaPlugin {

    private static TowersPlugin plugin;
    private GameMap towersMap;

    public static TowersPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("The Towers Reboot has been enabled!");
        ItemManager itemManager = new ItemManager();
        plugin=this;


        //Création des maps mini jeu
        getDataFolder().mkdirs();
        File gameMapsFolder = new File(getDataFolder(), "gamemaps");
        if(!gameMapsFolder.exists()) {
            gameMapsFolder.mkdirs();
        }

        towersMap=new TowersGameMap(gameMapsFolder,"the_towers_void_map", true);
        GameManager gameManager = new GameManager(this, towersMap);

        //Commands for the game
        getCommand("starttowers").setExecutor(new StartGame(this, gameManager));
        getCommand("stoptowers").setExecutor(new StopGame(this, gameManager));
        getCommand("giveitems").setExecutor(new GetItems());
        getCommand("jointowersblueteam").setExecutor(new JoinTowersBlueTeam(gameManager));
        getCommand("jointowersredteam").setExecutor(new JoinTowersRedTeam(gameManager));
        getCommand("warptowers").setExecutor(new WarpTowers(towersMap, gameManager));
        getCommand("leavetowers").setExecutor(new LeaveTowers(gameManager));
        getCommand("reloadtowers").setExecutor(new ReloadTowers(this,towersMap));

        //Special items
        getServer().getPluginManager().registerEvents(new RabbitFoot(this), this);
        getServer().getPluginManager().registerEvents(new Meteor(this), this);
        getServer().getPluginManager().registerEvents(new TowersMainEvents(this, gameManager),this);
        getServer().getPluginManager().registerEvents(new PlayerSwapper(this), this);
        getServer().getPluginManager().registerEvents(new ChickenNuke(this), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("The Towers Reboot has been disabled.");
    }
}
