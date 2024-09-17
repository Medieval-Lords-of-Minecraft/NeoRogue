package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileTickAction;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class BowProjectile extends Projectile {
	public static ParticleContainer bowTick = new ParticleContainer(Particle.CRIT).count(10).speed(0.01).spread(0.1, 0.1);
	
	private PlayerFightData data;
	private Player p;
	private ArrayList<ProjectileTickAction> tickActions = new ArrayList<ProjectileTickAction>();

	// Vector is non-normalized velocity of the vanilla projectile being fired
	public BowProjectile(PlayerFightData data, Vector v, Bow bow) {
		super(1, bow.getProperties().get(PropertyType.RANGE), 1);
		this.gravity(0.2);
		this.size(1, 0.1);
		this.data = data;
		this.p = data.getPlayer();
	}

	public void addProjectileTickAction(ProjectileTickAction action) {
		tickActions.add(action);
	}

	@Override
	public void onTick(ProjectileInstance proj, boolean interpolation) {
		BowProjectileInstance bproj = (BowProjectileInstance) proj;
		bow.onTick(p, proj, interpolation);
		ammo.onTick(p, proj, interpolation);
		for (ProjectileTickAction action : tickActions) {
			action.onTick(p, proj, interpolation);
		}
	}

	@Override
	public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
		bow.bowDamageProjectile(hit.getEntity(), proj, hitBarrier, ammo, initialVelocity, true);
	}

	@Override
	protected IProjectileInstance create(FightData owner, Location source, Vector direction) {
		PlayerFightData data = (PlayerFightData) owner;
		BowProjectileInstance proj = new BowProjectileInstance(data, direction, this, bow, ammo, initialVelocity);
		onStart(proj);
		return proj;
	}

	@Override
	public void onStart(ProjectileInstance proj) {
		Sounds.shoot.play(p, p);
		DamageMeta dm = proj.getMeta();
		EquipmentProperties ammoProps = ammo.getProperties();
		double dmg = ammoProps.get(PropertyType.DAMAGE) + props.get(PropertyType.DAMAGE);
		dm.addDamageSlice(new DamageSlice(data, dmg, ammoProps.getType()));
		ammo.onStart(proj);
	}
}
