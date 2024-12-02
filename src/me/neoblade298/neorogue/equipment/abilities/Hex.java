package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Hex extends Equipment {
	private static final String ID = "hex";
	private int dec = 20, stacks;
	private ItemStack activeIcon;
	
	public Hex(boolean isUpgraded) {
		super(ID, "Hex", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 3, 0));
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			if (inst.getCount() == 0) {
				inst.setCount(1);
				Sounds.equip.play(p, p);
				inst.setIcon(activeIcon);
			}
			else {
				inst.setCount(0);
				inst.setIcon(item);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			if (inst.getCount() == 1) {
				ev.getMeta().addBuff(DamageBuffType.GENERAL, new Buff(data, -dec, 0), BuffOrigin.NORMAL, true);
				FightInstance.applyStatus(ev.getTarget(), StatusType.INJURY, data, stacks, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BONE,
				"On cast, your next. When active, your basic attacks are weakened by " + DescUtil.white(dec) + " in exchange for applying " +
				GlossaryTag.INJURY.tag(this, stacks, true) + ".");
		activeIcon = item.withType(Material.BONE_MEAL);
	}
}
