package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Posturing extends Equipment {
	private static final String ID = "Posturing";
	private int time, inc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(1, 1).offsetY(1);
	
	public Posturing(boolean isUpgraded) {
		super(ID, "Posturing", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		time = isUpgraded ? 4 : 5;
		inc = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	public void setupReforges() {
		addReforge(KeenSenses.get(), Posturing2.get(), Setup.get());
		addReforge(AgilityTraining.get(), FlashDraw.get());
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		String buffId = UUID.randomUUID().toString();
		StandardPriorityAction act = new StandardPriorityAction(id);
		act.setAction((pdata, in) -> {
		if (!p.isSneaking()) return TriggerResult.keep();
			act.addCount(1);
			if (act.getCount() >= time) {
				pc.play(p, p);
				Sounds.enchant.play(p, p);
				data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, inc, 0, StatTracker.damageBuffAlly(buffId, this)));
				act.addCount(-time);
				if (act.getBool()) {
					icon.setAmount(icon.getAmount() + 1);
					p.getInventory().setItem(EquipSlot.convertSlot(es, slot), icon);
				}
				else {
					act.setBool(true);
				}
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
