package fr.pikili.towers.towersplugin.events;

import fr.pikili.towers.towersplugin.items.ItemManager;
import fr.pikili.towers.towersplugin.items.NukeChicken;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;


public class Meteor implements Listener {

    private JavaPlugin plugin;

    public Meteor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile instanceof Snowball) {
            if (((Snowball) projectile).getItem().getItemMeta().equals(ItemManager.meteor.getItemMeta())){
                projectile.setMetadata("Meteor", new FixedMetadataValue(plugin, true));
            }
        }
    }

    @EventHandler
    public void onMeteorHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if(projectile instanceof Snowball){
            if (projectile.hasMetadata("Meteor")) {
                Location explosion = null;

                // Déterminer le lieu de l'impact
                if (event.getHitBlock() != null) {
                    explosion = event.getHitBlock().getLocation();
                    ArrayList<Entity> launchable = new ArrayList<Entity>(Objects.requireNonNull(explosion.getWorld()).getNearbyEntities(explosion, 5, 5, 5));
                    for (Entity entity : launchable) {
                        if(!(entity instanceof Item) && !(entity instanceof Chicken)) {
                            // Calculer la direction opposée
                            Vector direction = entity.getLocation().toVector().subtract(explosion.toVector()).normalize();

                            // Ajuster la direction pour propulser l'entité en l'air et en arrière
                            direction.multiply(2); // Inverser et augmenter la force de propulsion
                            direction.setY(0.4); // Ajuster cette valeur pour changer le boost vertical

                            // Appliquer la nouvelle vélocité
                            entity.setVelocity(direction);

                            // Particules de l'explosion
                            entity.getWorld().spawnParticle(Particle.CLOUD, explosion, 200, 0.5, 0.5, 0.5, 0.1);
                            entity.getWorld().spawnParticle(Particle.FLAME, explosion, 10, 1, 1, 1, 0.1);
                            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 0.75F);
                        }
                    }
                } else if (event.getHitEntity() != null) {
                    explosion = event.getHitEntity().getLocation();
                    Entity entity = event.getHitEntity();
                    Vector vector = entity.getVelocity();
                    vector.multiply(1.5);
                    vector.setY(1);
                    entity.setVelocity(vector);
                    entity.getWorld().spawnParticle(Particle.CLOUD, explosion, 200, 1, 1, 1, 0.1);
                    entity.getWorld().spawnParticle(Particle.FLAME, explosion, 10, 1, 1, 1, 0.1);
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.75F, 0.5F);
                }
            }
        }

    }
}
