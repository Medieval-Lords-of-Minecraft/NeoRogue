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
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class Cauterize extends Equipment {
	private static final String ID = "cauterize";
	private int dec = 25, stacks;
	private double damage;
	private ItemStack activeIcon;
	
	public Cauterize(boolean isUpgraded) {
		super(ID, "Cauterize", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 3, 0));
			stacks = isUpgraded ? 5 : 3;
			damage = isUpgraded ? 1.8 : 1.2;
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
				ev.getMeta().addBuff(BuffType.GENERAL, new Buff(data, -dec, 0), BuffOrigin.NORMAL, true);
				FightInstance.applyStatus(ev.getTarget(), StatusType.INJURY, data, stacks, -1);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.FIRE)) return TriggerResult.keep();
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (!fd.hasStatus(StatusType.INJURY)) return TriggerResult.keep();
			int stacks = fd.getStatus(StatusType.INJURY).getStacks();
			fd.applyStatus(StatusType.INJURY, data, -stacks, -1);
			ev.getMeta().addDamageSlice(new DamageSlice(pdata, damage * stacks, DamageType.FIRE));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				"Toggleable, off by default. When active, your basic attacks are weakened by " + DescUtil.white(dec) + " in exchange for applying " +
				GlossaryTag.INJURY.tag(this, stacks, true) + ". Additionally, dealing " + GlossaryTag.FIRE.tag(this) + " damage removes all stacks of " +
				GlossaryTag.INJURY.tag(this) + " and deals " + GlossaryTag.FIRE.tag(this, damage, true) + " damage per stack removed.");
		activeIcon = item.withType(Material.TORCH);
	}
}
