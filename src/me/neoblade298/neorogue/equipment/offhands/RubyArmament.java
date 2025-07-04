package me.neoblade298.neorogue.equipment.offhands;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class RubyArmament extends Equipment {
	private static final String ID = "rubyArmament";
	private int stamina, stamCost, damage, damageDec;
	private static final ParticleContainer patience = new ParticleContainer(Particle.END_ROD).count(25).speed(0.01),
			power = new ParticleContainer(Particle.SOUL_FIRE_FLAME).count(25).speed(0.01);
	
	public RubyArmament(boolean isUpgraded) {
		super(ID, "Ruby Armament", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		stamina = isUpgraded ? 3 : 2;
		stamCost = 4;
		damage = isUpgraded ? 20 : 15;
		damageDec = 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		StandardPriorityAction act = new StandardPriorityAction(ID);
		data.addTrigger(ID, Trigger.RIGHT_CLICK, (pdata, in) -> {
			if (act.getCount() == 0) {
				act.setCount(1);
				Util.msg(p, "Entered stance <white>Power");
				Sounds.fire.play(p, p);
				power.play(p, p);
			}
			else {
				act.setCount(0);
				Util.msg(p, "Entered stance <white>Patience");
				Sounds.enchant.play(p, p);
				patience.play(p, p);
			}
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			if (act.getCount() == 0) {
				data.addStamina(stamina);
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(pdata, -damageDec, 0, StatTracker.damageBuffAlly(buffId, this)));
			}
			else {
				if (data.getStamina() < stamCost) return TriggerResult.keep();
				data.addStamina(-stamCost);
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(pdata, damage, 0, StatTracker.damageBuffAlly(buffId, this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_BLOCK, "Right clicking switches stances between <white>Power</white> and <white>Patience</white>."
				+ " While in <white>Patience</white>, your basic attack damage is decreased by <white>" + damageDec + "</white> and grant <yellow>" + stamina + "</yellow> stamina."
				+ " While in <white>Power</white>, your basic attack damage is increased by <yellow>" + damage + "</yellow> and cost <white>" + stamCost + "</white>"
				+ " stamina.");
	}
}
