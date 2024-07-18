package fr.pikili.towers.towersplugin.map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;

public class TowersGameMap implements GameMap {

    private final File sourceWorldFolder;
    private File activeWorldFolder;
    private World bukkitWorld;

    public TowersGameMap(File worldFolder, String worldName, boolean loadOnInit) {
        this.sourceWorldFolder = new File(worldFolder, worldName);

        if(loadOnInit) {
            load();
        }
    }

    @Override
    public boolean load() {
        if(isLoaded()) return true;

        this.activeWorldFolder = new File(
                Bukkit.getWorldContainer(),
                sourceWorldFolder.getName() + "_temp"
        );

        try {
            FileUtil.copy(sourceWorldFolder, activeWorldFolder);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to load game map");
            e.printStackTrace();
            return false;
        }

        this.bukkitWorld = Bukkit.createWorld(new WorldCreator(activeWorldFolder.getName()));

        if(bukkitWorld != null) {
            this.bukkitWorld.setAutoSave(false);
        }
        return isLoaded();
    }

    @Override
    public void unload() {
        if (bukkitWorld != null) {
            Bukkit.unloadWorld(bukkitWorld, false);
        }
        if (activeWorldFolder != null) {
            FileUtil.delete(activeWorldFolder);
        }

        bukkitWorld = null;
        activeWorldFolder = null;
    }

    @Override
    public boolean restoreFromSource() {
        unload();
        return load();
    }

    @Override
    public boolean isLoaded() {
        return bukkitWorld != null && Bukkit.getWorld(bukkitWorld.getUID()) != null;
    }

    @Override
    public World getWorld() {
        return bukkitWorld;
    }
}
