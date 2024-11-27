package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Malice extends Equipment {
	private static final String ID = "malice";
	private int dec = 15, stacks, thres;
	private ItemStack activeIcon;
	
	public Malice(boolean isUpgraded) {
		super(ID, "Malice", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 3, 0));
			stacks = 12;
			thres = isUpgraded ? 75 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		am.setCount(1);
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			if (!inst.getBool()) {
				inst.setBool(true);
				Sounds.equip.play(p, p);
				inst.setIcon(activeIcon);
			}
			else {
				inst.setBool(false);
				inst.setIcon(item);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INJURY)) return TriggerResult.keep();
			inst.addCount(ev.getStacks());
			if (inst.getCount() >= thres) {
				inst.addCount(-thres);
				am.addCount(1);
				activeIcon.setAmount(am.getCount());
				if (inst.getBool()) inst.setIcon(activeIcon);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			if (inst.getBool()) {
				ev.getMeta().addBuff(BuffType.GENERAL, new Buff(data, -dec, 0), BuffOrigin.NORMAL, true);
				FightInstance.applyStatus(ev.getTarget(), StatusType.INJURY, data, stacks * am.getCount(), -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BONE,
				"Toggleable, off by default. When active, your basic attacks are weakened by " + DescUtil.white(dec) + " in exchange for applying " +
				GlossaryTag.INJURY.tag(this, stacks, false) + ", increased by <white>1</white> for every " + GlossaryTag.INJURY.tag(this, thres, true) + " you apply.");
		activeIcon = item.withType(Material.BONE_MEAL);
	}
}
