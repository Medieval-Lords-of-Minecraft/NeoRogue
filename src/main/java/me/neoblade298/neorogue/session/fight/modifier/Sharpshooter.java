package me.neoblade298.neorogue.session.fight.modifier;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.MobModifier;
import me.neoblade298.neorogue.session.fight.TickAction;
import net.kyori.adventure.text.Component;

// Fires an arrow at a random player every few seconds, dealing piercing damage.
public class Sharpshooter extends MobModifier {
	private static final int INTERVAL = 5; // seconds
	private static final double DAMAGE = 15;
	private static final SoundContainer shoot = new SoundContainer(Sound.ENTITY_ARROW_SHOOT);
	private static final SoundContainer impact = new SoundContainer(Sound.ENTITY_ARROW_HIT_PLAYER);
	private static final ParticleContainer trail = new ParticleContainer(Particle.CRIT).count(2);

	public Sharpshooter() {
		super("Sharpshooter", Component.text("Sharpshooter"),
				Component.text("Fires an arrow at a random enemy every " + INTERVAL + "s."), false);
	}

	@Override
	public void initialize(FightData mob) {
		mob.addTickAction(new TickAction() {
			private int counter = 0;

			@Override
			public TickResult run() {
				LivingEntity ent = mob.getEntity();
				if (ent == null || !ent.isValid()) return TickResult.REMOVE;
				if (++counter < INTERVAL) return TickResult.KEEP;
				counter = 0;

				ArrayList<Player> players = new ArrayList<Player>();
				for (Player p : mob.getInstance().getSession().getOnlinePlayers()) {
					if (p.getWorld().equals(ent.getWorld())) players.add(p);
				}
				if (players.isEmpty()) return TickResult.KEEP;

				Player target = players.get(NeoRogue.gen.nextInt(players.size()));
				Location from = ent.getLocation().add(0, 1.2, 0);
				Location to = target.getLocation().add(0, 1, 0);
				Vector dir = to.clone().subtract(from).toVector();
				double dist = dir.length();
				if (dist > 0) dir.normalize();

				for (Player p : players) {
					shoot.play(p, from);
				}
				for (double d = 0; d < dist; d += 0.5) {
					Location point = from.clone().add(dir.clone().multiply(d));
					for (Player p : players) {
						trail.play(p, point);
					}
				}
				for (Player p : players) {
					impact.play(p, to);
				}
				double damage = DAMAGE * mob.getInstance().getSession().getMobDamageMultiplier();
				FightInstance.dealDamage(mob, DamageType.PIERCING, damage, target, DamageStatTracker.ignored(id));
				return TickResult.KEEP;
			}
		});
	}
}
