package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class CopperFunnel extends Equipment {
	private static final String ID = "CopperFunnel";
	private int reps, reduc = 25;
	
	public CopperFunnel(boolean isUpgraded) {
		super(ID, "Copper Funnel", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(isUpgraded ? 10 : 15, 0, 0, 0));
				reps = isUpgraded ? 3 : 2;
				properties.addUpgrades(PropertyType.MANA_COST);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		icon.setAmount(reps);
		ActionMeta am = new ActionMeta();
		am.setCount(reps);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.MAGICAL)) return TriggerResult.keep();
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, reduc, StatTracker.defenseBuffAlly(am.getId(), this)));
			am.addCount(-1);
			if (am.getCount() <= 0) {
				Sounds.breaks.play(p, p);
				return TriggerResult.remove();
			}
			else {
				icon.setAmount(am.getCount());
				inst.setIcon(icon);
				return TriggerResult.keep();
			}
		});
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.HOPPER,
				"Passive. The first " + DescUtil.yellow(reps) + " times you receive " + GlossaryTag.MAGICAL.tag(this) + " damage, " +
				"reduce it by " + DescUtil.white(reduc) + ".");
	}
}
