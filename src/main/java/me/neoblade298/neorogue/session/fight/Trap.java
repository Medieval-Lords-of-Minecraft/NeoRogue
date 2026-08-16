package me.neoblade298.neorogue.session.fight;

import org.bukkit.Location;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public abstract class Trap extends Marker {
    private Trap activeDuplicate;
    public Trap(PlayerFightData owner, Location loc, int durationTicks) {
        super(owner, loc, durationTicks);
    }

    public Trap(PlayerFightData owner, Location loc, int durationTicks, Equipment sourceEquipment) {
        super(owner, loc, durationTicks, sourceEquipment);
    }

    public Trap(PlayerFightData owner, Location loc, int durationTicks, int tickPeriod, Equipment sourceEquipment) {
        super(owner, loc, durationTicks, tickPeriod, sourceEquipment);
    }

    public Trap duplicateAt(Location duplicateLocation, Equipment duplicateSource) {
        Trap original = this;
        return new Trap(owner, duplicateLocation, durationTicks, tickPeriod, duplicateSource) {
            @Override
            public void tick() {
                Location originalLocation = original.loc.clone();
                original.activeDuplicate = this;
                copyLocation(original.loc, duplicateLocation);
                try {
                    original.tick();
                } finally {
                    copyLocation(original.loc, originalLocation);
                    original.activeDuplicate = null;
                }
            }
        };
    }

    private static void copyLocation(Location target, Location source) {
        target.setWorld(source.getWorld());
        target.set(source.getX(), source.getY(), source.getZ());
        target.setYaw(source.getYaw());
        target.setPitch(source.getPitch());
    }

    public Trap getActiveDuplicate() {
        return activeDuplicate;
    }

    @Override
    public void onDeactivate() {
		  owner.runActions(owner, Trigger.DEACTIVATE_TRAP, this);
    }
    
}
