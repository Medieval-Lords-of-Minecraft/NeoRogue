package me.neoblade298.neorogue.session.fight.modifier;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.MobModifier;
import net.kyori.adventure.text.Component;

// Explodes when the mob dies, dealing fire damage to nearby players.
public class Martyr extends MobModifier {
	private static final double RADIUS = 4.0;
	private static final double DAMAGE = 2;
	private static final long WINDUP = 10L;
	private static final ParticleContainer explosion = new ParticleContainer(Particle.EXPLOSION).count(1);
	private static final SoundContainer sound = new SoundContainer(Sound.ENTITY_GENERIC_EXPLODE);
	private static final SoundContainer windup = new SoundContainer(Sound.ENTITY_CREEPER_PRIMED);

	public Martyr() {
		super("Martyr", Component.text("Martyr"),
				Component.text("Explodes on death, dealing damage to nearby players."), false);
	}

	@Override
	public void initialize(FightData mob) {
		mob.addDeathAction(fd -> {
			LivingEntity ent = fd.getEntity();
			if (ent == null) return;
			Location loc = ent.getLocation();
			// Windup: hiss now, then explode after a short delay
			for (Player p : fd.getInstance().getSession().getOnlinePlayers()) {
				windup.play(p, loc);
			}
			new BukkitRunnable() {
				@Override
				public void run() {
					double radiusSq = RADIUS * RADIUS;
					for (Player p : fd.getInstance().getSession().getOnlinePlayers()) {
						explosion.play(p, loc);
						sound.play(p, loc);
						if (p.getWorld().equals(loc.getWorld()) && p.getLocation().distanceSquared(loc) <= radiusSq) {
							FightInstance.dealDamage(fd, DamageType.FIRE, DAMAGE, p, DamageStatTracker.ignored(id));
						}
					}
				}
			}.runTaskLater(NeoRogue.inst(), WINDUP);
		});
	}
}
