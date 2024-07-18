package fr.pikili.towers.towersplugin.items;

import fr.pikili.towers.towersplugin.events.ChickenNuke;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Nuke {
    private final Location blockLocation;
    private Location explosionLocation;
    private final JavaPlugin plugin;
    private final ArrayList<NukeChicken> chickens = new ArrayList<>();

    public Nuke(Location blockLocation, JavaPlugin plugin) {
        this.blockLocation = blockLocation;
        this.plugin = plugin;
        launchNuke();
    }

    private void launchNuke() {
        Location nukeSpawnLoc = blockLocation.clone().add(0, 10, 0); // Clone to avoid modifying blockLocation
        for (int i = 0; i < 20; i++) {
            NukeChicken chicken = new NukeChicken(nukeSpawnLoc);
            chickens.add(chicken);
        }

        for(Entity entity : Objects.requireNonNull(blockLocation.getWorld()).getNearbyEntities(nukeSpawnLoc, 9, 8, 9)){
            if(entity instanceof Player){
                Player player = (Player) entity;
                player.sendMessage(ChatColor.DARK_RED+"Une nuke à été lancée près de votre position !");
            }
        }


        new BukkitRunnable() {
            public void run() {
                if (chickens.isEmpty()) {
                    cancel(); // Stop the task if there are no more chickens
                    ArrayList<Entity> launchable = new ArrayList<>(Objects.requireNonNull(explosionLocation.getWorld()).getNearbyEntities(explosionLocation, 9, 8, 9));
                    for (Entity entity : launchable) {
                        if(!(entity instanceof Chicken)) {
                            // Calculer la direction opposée
                            Vector direction = entity.getLocation().toVector().subtract(explosionLocation.toVector()).normalize();

                            // Ajuster la direction pour propulser l'entité en l'air et en arrière
                            direction.multiply(8); // Inverser et augmenter la force de propulsion
                            direction.setY(1); // Ajuster cette valeur pour changer le boost vertical

                            // Appliquer la nouvelle vélocité
                            entity.setVelocity(direction);

                            //Faire des dégats
                            if (entity instanceof LivingEntity) {
                                LivingEntity livingEntity = (LivingEntity) entity;
                                double damageAmount = 10.0; // Ajuster la valeur des dégâts selon vos besoins
                                livingEntity.damage(damageAmount);
                            }
                        }
                    }
                    // Particules de l'explosion
                    explosionLocation.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, explosionLocation, 4, 3, 1, 3, 0.1);
                    explosionLocation.getWorld().spawnParticle(Particle.FLAME, explosionLocation, 50, 1, 1, 1, 0.1);
                    explosionLocation.getWorld().playSound(explosionLocation, Sound.ENTITY_GENERIC_EXPLODE, 1F, 0.5F);
                    return;
                }

                List<NukeChicken> chickensToRemove = new ArrayList<>();
                for (NukeChicken chicken : chickens) {
                    if (chicken.isOnGround()) {
                        chicken.getChicken().remove(); // Remove the chicken entity
                        chickensToRemove.add(chicken); // Add to the list to remove from ArrayList
                        explosionLocation=chicken.getChicken().getLocation();
                    }
                }

                Objects.requireNonNull(blockLocation.getWorld()).spawnParticle(Particle.REDSTONE, blockLocation, 100, 0, 40, 0, new Particle.DustOptions(Color.YELLOW, 1F));
                blockLocation.getWorld().playSound(blockLocation, Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F , 2F);
                chickens.removeAll(chickensToRemove); // Remove all grounded chickens from the list
            }
        }.runTaskTimer(plugin, 0, 2); // Schedule task to run every tick (20 times per second)
    }
}
