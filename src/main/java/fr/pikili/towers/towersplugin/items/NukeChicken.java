package fr.pikili.towers.towersplugin.items;

import org.bukkit.Location;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;

import java.util.Objects;

public class NukeChicken {

    private final Chicken chicken;

    public NukeChicken(Location location) {
        chicken = (Chicken) Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.CHICKEN);
        chicken.setSilent(true);
        chicken.setInvulnerable(true);
        chicken.setCollidable(false);
        chicken.setCustomName("NukeChicken");
    }

    public boolean isOnGround() {
        return chicken.isOnGround();
    }

    public Chicken getChicken() {
        return chicken;
    }
}
