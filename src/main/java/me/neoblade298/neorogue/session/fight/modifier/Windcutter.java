package me.neoblade298.neorogue.session.fight.modifier;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.MobModifier;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;

// Every few times the mob deals damage, it releases a slashing wave that damages nearby players.
public class Windcutter extends MobModifier {
	private static final int ATTACKS_REQUIRED = 3;
	private static final double DAMAGE = 2;
	private static final double RADIUS = 5.0;
	private static final ParticleContainer part = new ParticleContainer(Particle.SWEEP_ATTACK).count(3);
	private static final SoundContainer sound = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP);

	public Windcutter() {
		super("Windcutter", Component.text("Windcutter"),
				Component.text("Every " + ATTACKS_REQUIRED + " times it deals damage, releases a slashing wave."), false);
	}

	@Override
	public void initialize(FightData mob) {
		int[] count = { 0 };
		boolean[] active = { false }; // Prevents the wave's own damage from counting toward itself
		mob.addMobTrigger(Trigger.DEAL_DAMAGE, (fd, in) -> {
			if (active[0]) return TriggerResult.keep();
			if (++count[0] < ATTACKS_REQUIRED) return TriggerResult.keep();
			count[0] = 0;
			LivingEntity ent = fd.getEntity();
			if (ent == null || !ent.isValid()) return TriggerResult.keep();
			Location loc = ent.getLocation().add(0, 1, 0);
			double radiusSq = RADIUS * RADIUS;
			double damage = DAMAGE * fd.getInstance().getSession().getMobDamageMultiplier();
			active[0] = true;
			try {
				for (Player p : fd.getInstance().getSession().getOnlinePlayers()) {
					part.play(p, loc);
					sound.play(p, loc);
					if (p.getWorld().equals(loc.getWorld()) && p.getLocation().distanceSquared(loc) <= radiusSq) {
						FightInstance.dealDamage(fd, DamageType.SLASHING, damage, p, DamageStatTracker.ignored(id));
					}
				}
			} finally {
				active[0] = false;
			}
			return TriggerResult.keep();
		});
	}
}
