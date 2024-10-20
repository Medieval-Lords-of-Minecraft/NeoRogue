package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Posturing extends Equipment {
	private static final String ID = "posturing";
	private int time, inc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANTMENT_TABLE).count(25).spread(1, 1).offsetY(1);
	
	public Posturing(boolean isUpgraded) {
		super(ID, "Posturing", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		time = isUpgraded ? 5 : 4;
		inc = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		act.setAction((pdata, in) -> {
			act.addCount(1);
			if (act.getCount() >= time) {
				pc.play(p, p);
				Sounds.enchant.play(p, p);
				data.addBuff(data, true, false, BuffType.GENERAL, inc);
				act.addCount(-time);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PLAYER_TICK, act);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SCAFFOLDING,
				"Passive. Every " + DescUtil.yellow(time +"s") + " spent crouched during a fight, increase your damage by " + DescUtil.yellow(inc) + ".");
	}
}
