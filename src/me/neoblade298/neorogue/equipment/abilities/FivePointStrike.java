package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class FivePointStrike extends Equipment {
	private static final String ID = "fivePointStrike";
	private int damage;
	private ProjectileGroup projs = new ProjectileGroup();
	private static final ParticleContainer part = new ParticleContainer(Particle.CRIT);
	
	public FivePointStrike(boolean isUpgraded) {
		super(ID, "Five Point Strike", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 25, 10, 4));
		
		damage = isUpgraded ? 60 : 40;
		for (int i = 0; i < 5; i++) {
			projs.add(new FivePointStrikeProjectile(i));
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
		item = createItem(Material.POINTED_DRIPSTONE,
				"On cast, fire <white>5</white> needles in a cone in front of you that deal "
						+ GlossaryTag.PIERCING.tag(this, damage, true) + " damage.");
	}
	
	private class FivePointStrikeProjectile extends Projectile {
		public FivePointStrikeProjectile(int i) {
			super(1, properties.get(PropertyType.RANGE), 1);
			int iter = i - 2;
			this.rotation(iter * 15);
			this.size(1, 1);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			damageProjectile(hit.getEntity(), proj, hitBarrier);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Player p = (Player) proj.getOwner().getEntity();
			Sounds.attackSweep.play(p, p);
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.PIERCING));
		}

		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			part.play(p, proj.getLocation());
		}
	}
}
