package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;

import org.bukkit.Particle;
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

public class BowProjectileInstance extends ProjectileInstance {
	public static ParticleContainer bowTick = new ParticleContainer(Particle.CRIT).count(10).speed(0.01).spread(0.1, 0.1);
	
	private PlayerFightData data;
	private Player p;
	private Bow bow;
	private EquipmentProperties props;
	private Ammunition ammo;
	private double initialVelocity;
	private ArrayList<ProjectileTickAction> tickActions = new ArrayList<ProjectileTickAction>();

	// Vector is non-normalized velocity of the vanilla projectile being fired
	public BowProjectileInstance(PlayerFightData data, Vector v, BowProjectile settings, Bow bow, Ammunition ammo, double initialVelocity) {
		super(settings, data);
		this.data = data;
		this.p = data.getPlayer();
		this.bow = bow;
		this.props = bow.getProperties();
		this.ammo = data.getAmmunition();
		this.initialVelocity = initialVelocity;
	}

	public void addProjectileTickAction(ProjectileTickAction action) {
		tickActions.add(action);
	}
}
