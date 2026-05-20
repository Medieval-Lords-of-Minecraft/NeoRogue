package me.neoblade298.neorogue.session.fight;

import org.bukkit.Location;
import org.bukkit.Particle;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class Rift extends Marker {
    private static final ParticleContainer pc = new ParticleContainer(Particle.END_ROD).spread(0.5, 0.5).count(25).offsetY(0.5);
    public Rift(PlayerFightData owner, Location loc, int durationTicks) {
        super(owner, loc, durationTicks);
    }

    @Override
    public void tick() {
      pc.play(owner.getPlayer(), loc);
    }

    @Override
    public void onDeactivate() {
		  owner.runActions(owner, Trigger.REMOVE_RIFT, this);
    }
    
}
