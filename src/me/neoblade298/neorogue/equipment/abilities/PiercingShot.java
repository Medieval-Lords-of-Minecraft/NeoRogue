package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class PiercingShot extends Equipment {
	private static final String ID = "PiercingShot";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST).spread(0.2, 0.2).count(3);
	
	public PiercingShot(boolean isUpgraded) {
		super(ID, "Piercing Shot", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 15, 8, 0));
		damage = isUpgraded ? 55 : 35;
	}

	@Override
	public void setupReforges() {
		addReforge(FocusedShot.get(), SunderingShot.get());
		addReforge(BasicElementMastery.get(), ShatteringShot.get());
		addReforge(KeenSenses.get(), PiercingShot2.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.equip.play(p, p);
			data.addTrigger(ID, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata2, in) -> {
				LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
				if (!ev.isBasicAttack()) return TriggerResult.keep();
				Sounds.fire.play(p, p);
				ProjectileInstance inst = (ProjectileInstance) ev.getInstances().getFirst();
				inst.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(ID + slot, this)));
				BowProjectile settings = (BowProjectile) inst.getParent();
				settings.pierce(1);
				settings.addProjectileTickAction((p2, proj, interpolation) -> {
					pc.play(p2, proj.getLocation());
				});
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_ROD,
				"On cast, your next basic attack deals an additional " + GlossaryTag.PIERCING.tag(this, damage, true) + " and pierces " +
				DescUtil.white(1) + " enemy.");
	}
}
