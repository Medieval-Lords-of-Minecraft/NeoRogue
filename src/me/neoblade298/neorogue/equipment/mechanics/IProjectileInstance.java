package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.Location;

public abstract class IProjectileInstance {
    private Location origin;
    public IProjectileInstance(Location origin) {
        this.origin = origin;
    }
    public Location getOrigin() {
        return origin;
    }
    // Specific use cases, like Stone Arrow + Arrow Rain
    public void setOrigin(Location origin) {
        this.origin = origin;
    }
}
