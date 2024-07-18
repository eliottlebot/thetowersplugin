package fr.pikili.towers.towersplugin;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class Generators {

    private static int delayToSpawnIron = 400;
    private static int delayToSpawnEmerald = 500;

    private static final Map<Location, BukkitTask> particleTasks = new HashMap<>();
    private int level = 1;

    public void startGenerator(World world, double x, double y, double z, Material material, Particle particle, Color color) {
        Location location = new Location(world, x, y + 0.1, z);
        BukkitTask task = new BukkitRunnable() {
            private static final double ANGLE_INCREMENT = Math.PI / 16;
            private double angle = 0;
            private int counter = 0;

            @Override
            public void run() {
                counter++;
                angle += ANGLE_INCREMENT;

                for (int i = 0; i < level; i++) {
                    double radius = 0.75 + (i * 0.5);
                    double X = radius * Math.cos(angle);
                    double Z = radius * Math.sin(angle);
                    Location particleLocation = location.clone().add(X, 0, Z);

                    if (particle == Particle.REDSTONE) {
                        world.spawnParticle(Particle.REDSTONE, particleLocation, 1, new Particle.DustOptions(color, 1.5F));
                    } else {
                        world.spawnParticle(particle, particleLocation, 1);
                    }
                }

                if (material == Material.IRON_INGOT && counter % delayToSpawnIron == 0) {
                    spawnItem(world, location, material);
                } else if (material == Material.EMERALD && counter % delayToSpawnEmerald == 0) {
                    spawnItem(world, location, material);
                }
            }
        }.runTaskTimer(TowersPlugin.getPlugin(), 0, 1);

        particleTasks.put(location, task);
    }

    private void spawnItem(World world, Location location, Material material) {
        world.dropItem(location, new ItemStack(material));
        world.playSound(location, Sound.ENTITY_ITEM_PICKUP, 1, 1);
    }

    public void stopGenerators(World world, double x, double y, double z) {
        Location location = new Location(world, x, y, z);
        BukkitTask task = particleTasks.remove(location);
        if (task != null) {
            task.cancel();
        }
    }

    public void setDelayToSpawnIron(int delay) {
        if (delay > 0) {
            delayToSpawnIron = delay;
        }
    }

    public void setDelayToSpawnEmerald(int delay) {
        if (delay > 0) {
            delayToSpawnEmerald = delay;
        }
    }

    public void setGeneratorToLevelOne() {
        this.level = 1;
        setDelayToSpawnIron(400);
        setDelayToSpawnEmerald(600);
    }

    public void setGeneratorToLevelTwo() {
        this.level = 2;
        setDelayToSpawnIron(200);
        setDelayToSpawnEmerald(300);
    }

    public void setGeneratorToLevelThree() {
        this.level = 3;
        setDelayToSpawnIron(100);
        setDelayToSpawnEmerald(150);
    }

    public void setGeneratorToLevelFour() {
        this.level = 4;
        setDelayToSpawnIron(50);
        setDelayToSpawnEmerald(75);
    }
}
