package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WindSlash extends Equipment {
	private int amount, damage;
	private ProjectileGroup projs = new ProjectileGroup();
	private static final ParticleContainer part = new ParticleContainer(Particle.SWEEP_ATTACK);
	
	public WindSlash(boolean isUpgraded) {
		super("windSlash", "Wind Slash", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 10, 10, 9));
		
		amount = isUpgraded ? 5 : 3;
		damage = isUpgraded ? 150 : 100;
		for (int i = 0; i < amount; i++) {
			projs.add(new WindSlashProjectile(i, amount / 2));
		}
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pd, in) -> {
			Util.playSound(p, Sound.ENTITY_PLAYER_ATTACK_SWEEP, false);
			projs.start(data);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, fire <yellow>" + amount + " </yellow>projectiles in a cone in front of you that deal "
						+ "<yellow>" + damage + " </yellow>" + GlossaryTag.SLASHING.tag(this) + " damage.");
	}
	
	private class WindSlashProjectile extends Projectile {
		public WindSlashProjectile(int i, int center) {
			super(0.5, properties.get(PropertyType.RANGE), 2);
			this.size(1, 1);
			int iter = i - center;
			this.rotation(iter * 45);
		}

		@Override
		public void onTick(ProjectileInstance proj) {
			if (proj.getTick() % 3 == 0) Util.playSound((Player) proj.getOwner().getEntity(), proj.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, false);
			part.spawn(proj.getLocation());
		}

		@Override
		public void onEnd(ProjectileInstance proj) {}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			damageProjectile(hit.getEntity(), proj, new DamageMeta(proj.getOwner(), damage, DamageType.SLASHING), hitBarrier);
		}
	}
}
