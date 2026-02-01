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
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Fireball extends Equipment {
	private static final String ID = "Fireball";
	private static final ParticleContainer tick = new ParticleContainer(Particle.FLAME).count(5).spread(0.3, 0.3);

	private int damage, burn;

	public Fireball(boolean isUpgraded) {
		super(ID, "Fireball", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 5, 12, 10));
		damage = isUpgraded ? 240 : 160;
		burn = 30;
	}

	@Override
	public void setupReforges() {
		addReforge(Manabending.get(), Fireball2.get(), Torch.get(), Fireblast.get());
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new FireballProjectile(data, slot, this));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			data.channel(20).then(new Runnable() {
				public void run() {
					data.applyStatus(StatusType.BURN, data, burn, -1);
					proj.start(data);
				}
			});
			return TriggerResult.keep();
		}));
	}

	private class FireballProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private int slot;
		private Equipment eq;

		public FireballProjectile(PlayerFightData data, int slot, Equipment eq) {
			super(1, properties.get(PropertyType.RANGE), 1);
			this.size(0.5, 0.5);
			this.data = data;
			this.slot = slot;
			this.p = data.getPlayer();
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {

		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
			proj.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE, DamageStatTracker.of(ID + slot, eq)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before launching a fireball that deals "
						+ GlossaryTag.FIRE.tag(this, damage, true) + " damage but apply "
						+ GlossaryTag.BURN.tag(this, burn, false) + " to yourself.");
	}
}
