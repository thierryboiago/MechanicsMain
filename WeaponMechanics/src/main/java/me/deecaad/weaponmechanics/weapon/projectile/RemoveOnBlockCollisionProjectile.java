package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.RayTraceResult;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Really lightweight remove on block collision projectile.
 * This considers all blocks as 1x1x1 and is removed on any
 * collision including passable blocks.
 *
 * This doesn't check entity collisions.
 */
public class RemoveOnBlockCollisionProjectile extends AProjectile {

    private static final RayTrace rayTrace = new RayTrace().disableEntityChecks();

    public RemoveOnBlockCollisionProjectile(Location location, Vector motion) {
        this(location, motion, null);
    }

    public RemoveOnBlockCollisionProjectile(Location location, Vector motion, FakeEntity disguise) {
        super(location, motion);
        if (disguise != null) spawnDisguise(disguise);
    }

    @Override
    public boolean handleCollisions() {
        Vector possibleNextLocation = getLocation().add(getMotion());
        List<RayTraceResult> hits = rayTrace.cast(getWorld(), getLocation(), possibleNextLocation, getNormalizedMotion());
        if (hits != null) {
            RayTraceResult firstHit = hits.get(0);
            setRawLocation(firstHit.getHitLocation());
            onCollide(firstHit.getBlock());
            return true;
        }
        setRawLocation(possibleNextLocation);
        return false;
    }
}
