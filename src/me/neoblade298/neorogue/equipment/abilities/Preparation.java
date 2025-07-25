package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Preparation extends Equipment {
	private static final String ID = "preparation";
	private static final ParticleContainer part = new ParticleContainer(Particle.FLAME).count(25).spread(0.5, 0.5).speed(0.1).offsetY(1);
	private int damage, shields;
	
	public Preparation(boolean isUpgraded) {
		super(ID, "Preparation", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 25, 25, 0));
		damage = isUpgraded ? 100 : 70;
		shields = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		data.addTrigger(ID, bind, (pdata, in) -> {
			Sounds.equip.play(p, p);
			data.charge(100, 0);
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.success.play(p, p);
					part.play(p, p);
					data.addSimpleShield(p.getUniqueId(), shields, 200);
					inst.setCount(1);
					data.addTask(new BukkitRunnable() {
						public void run() {
							inst.setCount(2);
						}
					}.runTaskLater(NeoRogue.inst(), 200L));
				}
			}.runTaskLater(NeoRogue.inst(), 100L));
			return TriggerResult.keep();
		});
		
		inst.setAction((pdata, in) -> {
			if (inst.getCount() == 0) return TriggerResult.keep();
			if (inst.getCount() == 2) return TriggerResult.remove();
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageSlice(new DamageSlice(pdata, damage, DamageType.PIERCING, DamageStatTracker.of(ID + slot, this)));
			Sounds.anvil.play(p, p);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, " + DescUtil.charge(this, 0, 5) + " before gaining " + GlossaryTag.SHIELDS.tag(this, shields, true) + " "
				+ "and dealing an additional "
				+ GlossaryTag.PIERCING.tag(this, damage, true) + " damage on basic attacks for <white>10</white> seconds.");
	}
}
