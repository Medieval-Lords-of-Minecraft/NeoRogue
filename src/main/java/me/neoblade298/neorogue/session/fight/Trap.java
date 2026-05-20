package me.neoblade298.neorogue.session.fight;

import org.bukkit.Location;

import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public abstract class Trap extends Marker {
    public Trap(PlayerFightData owner, Location loc, int durationTicks) {
        super(owner, loc, durationTicks);
    }

    @Override
    public void onDeactivate() {
		  owner.runActions(owner, Trigger.DEACTIVATE_TRAP, this);
    }
    
}
