package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;

import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileTickAction;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class BowProjectile extends Projectile {
	public static ParticleContainer tick = new ParticleContainer(Particle.CRIT);
	
	private PlayerFightData data;
	private Player p;
	private Bow bow;
	private EquipmentProperties props;
	private AmmunitionInstance ammo;
	private double initialVelocity, damageBonus;
	private boolean isBasicAttack = true;
	private ArrayList<ProjectileTickAction> tickActions = new ArrayList<ProjectileTickAction>();

	public BowProjectile(PlayerFightData data, Vector v, Bow bow, boolean isBasicAttack) {
		super(bow.getProperties().get(PropertyType.RANGE), 1);
		this.isBasicAttack = isBasicAttack;
	}

	// Vector is non-normalized velocity of the vanilla projectile being fired
	public BowProjectile(PlayerFightData data, Vector v, Bow bow) {
		super(bow.getProperties().get(PropertyType.RANGE), 1);
		setBowDefaults();
		this.data = data;
		this.p = data.getPlayer();
		this.bow = bow;
		this.ammo = data.getAmmoInstance();
		this.props = bow.getProperties();
		initialVelocity = v.length();

		blocksPerTick(initialVelocity);
	}

	public void addProjectileTickAction(ProjectileTickAction action) {
		tickActions.add(action);
	}

	public double getInitialVelocity() {
		return initialVelocity;
	}

	public boolean isBasicAttack() {
		return isBasicAttack;
	}

	@Override
	public void onTick(ProjectileInstance proj, int interpolation) {
		bow.onTick(p, proj, interpolation);
		ammo.onTick(p, proj, interpolation);
		for (ProjectileTickAction act : tickActions) {
			act.onTick(p, proj, interpolation);
		}
	}

	// Sometimes useful for modifying bow damage, like with Composite Bow
	public BowProjectile setDamageBonus(double damageBonus) {
		this.damageBonus = damageBonus;
		return this;
	}

	public double getDamageBonus() {
		return this.damageBonus;
	}

	@Override
	public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
		bow.bowProjectileOnHit(hit.getEntity(), proj, hitBarrier, meta, ammo, isBasicAttack);
		ammo.onHit(proj, meta, hit.getEntity());
	}

	@Override
	public void onStart(ProjectileInstance proj) {
		Sounds.shoot.play(p, p);
		DamageMeta dm = proj.getMeta();
		EquipmentProperties ammoProps = ammo.getProperties();
		double dmg = ammoProps.get(PropertyType.DAMAGE) + props.get(PropertyType.DAMAGE) + damageBonus;
		dm.addDamageSlice(new DamageSlice(data, dmg, ammoProps.getType()));
		ammo.onStart(proj);
	}

	@Override
	public void onHitBlock(ProjectileInstance proj, Block b) {
		ammo.onHitBlock(proj, b);
	}
}
