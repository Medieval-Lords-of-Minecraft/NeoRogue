package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
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
	private static final String ID = "windSlash";
	private int amount, damage;
	private ProjectileGroup projs = new ProjectileGroup();
	private static final ParticleContainer part = new ParticleContainer(Particle.SWEEP_ATTACK);
	
	public WindSlash(boolean isUpgraded) {
		super(ID, "Wind Slash", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 25, 10, 8));
		
		amount = isUpgraded ? 5 : 3;
		damage = isUpgraded ? 180 : 140;
		for (int i = 0; i < amount; i++) {
			projs.add(new WindSlashProjectile(i, amount / 2));
		}
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pd, in) -> {
			Sounds.attackSweep.play(p, p);
			projs.start(data);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STRING,
				"On cast, fire <yellow>" + amount + " </yellow>projectiles in a cone in front of you that deal "
						+ "<yellow>" + damage + " </yellow>" + GlossaryTag.SLASHING.tag(this) + " damage.");
	}
	
	private class WindSlashProjectile extends Projectile {
		public WindSlashProjectile(int i, int center) {
			super(0.5, properties.get(PropertyType.RANGE), 2);
			this.size(1, 1);
			int iter = i - center;
			this.rotation(iter * 25);
		}

		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			if (proj.getTick() % 3 == 0) Sounds.flap.play(p, proj.getLocation());
			part.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			damageProjectile(hit.getEntity(), proj, new DamageMeta(proj.getOwner(), damage, DamageType.SLASHING), hitBarrier);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			
		}
	}
}
