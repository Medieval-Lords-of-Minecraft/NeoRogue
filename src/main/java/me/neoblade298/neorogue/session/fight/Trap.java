package me.neoblade298.neorogue.session.fight;

import org.bukkit.Location;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public abstract class Trap extends Marker {
    public Trap(PlayerFightData owner, Location loc, int durationTicks) {
        super(owner, loc, durationTicks);
    }

    public Trap(PlayerFightData owner, Location loc, int durationTicks, Equipment sourceEquipment) {
        super(owner, loc, durationTicks, sourceEquipment);
    }

    public Trap(PlayerFightData owner, Location loc, int durationTicks, int tickPeriod, Equipment sourceEquipment) {
        super(owner, loc, durationTicks, tickPeriod, sourceEquipment);
    }

    @Override
    public void onDeactivate() {
		  owner.runActions(owner, Trigger.DEACTIVATE_TRAP, this);
    }
    
}
