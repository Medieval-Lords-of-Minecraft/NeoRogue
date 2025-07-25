package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Maim extends Equipment {
	private static final String ID = "maim";
	private int inc, damage;
	private static final ParticleContainer part = new ParticleContainer(Particle.CRIT).count(50).spread(0.5, 0.5);
	private static final SoundContainer hit = new SoundContainer(Sound.BLOCK_BAMBOO_WOOD_TRAPDOOR_CLOSE);
	
	public Maim(boolean isUpgraded) {
		super(ID, "Maim", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 20, 15, 0));
		
		inc = isUpgraded ? 30 : 20;
		damage = isUpgraded ? 90 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			Sounds.equip.play(p, p);
			inst.addCount(1);
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, bind, inst);
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (inst.getCount() > 0) {
				inst.addCount(-1);
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				part.play(p, ev.getTarget());
				hit.play(p, ev.getTarget());
				ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING,
					DamageStatTracker.of(ID + slot, this)));
				FightInstance.getFightData(ev.getTarget()).addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), new Buff(data, -inc, 0, StatTracker.damageBuffAlly(buffId, this)), 200);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_ROD,
				"On cast, your next basic attack deals an additional " + GlossaryTag.BLUNT.tag(this, damage, true) + " damage"
						+ " and increases " + GlossaryTag.PHYSICAL.tag(this) + " damage dealt to the enemy hit by <yellow>" + inc + "</yellow>"
						+ " for <white>10</white> seconds.");
	}
}
