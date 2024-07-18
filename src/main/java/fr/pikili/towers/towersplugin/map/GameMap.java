package fr.pikili.towers.towersplugin.map;

import org.bukkit.World;

public interface GameMap {

    boolean load();
    void unload();
    boolean restoreFromSource();
    boolean isLoaded();
    World getWorld();


}
