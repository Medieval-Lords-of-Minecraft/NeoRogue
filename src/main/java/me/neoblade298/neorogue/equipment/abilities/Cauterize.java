package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Cauterize extends Equipment {
	private static final String ID = "Cauterize";
	private int dec = 15, stacks, hitReq = 3;
	private double damage;
	private ItemStack activeIcon;

	public Cauterize(boolean isUpgraded) {
		super(ID, "Cauterize", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 3, 0));
		stacks = 4;
		damage = isUpgraded ? 25 : 15;
		properties.setCastType(CastType.TOGGLE);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String buffId = UUID.randomUUID().toString();
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, sessionEq, slot, es);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			if (inst.getCount() == 0) {
				inst.setCount(1);
				Sounds.equip.play(p, p);
				inst.setIcon(activeIcon);
			} else {
				inst.setCount(0);
				inst.setIcon(item);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);

		int[] hits = {0};
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			if (inst.getCount() == 1) {
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
						new Buff(data, -dec, 0, StatTracker.damageBuffAlly(buffId, this)));
				if (++hits[0] >= hitReq) {
					hits[0] = 0;
					FightInstance.applyStatus(ev.getTarget(), StatusType.INJURY, data, stacks, -1, this);
				}
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (inst.getCount() == 1)
				return TriggerResult.keep();
			if (!ev.getMeta().containsType(DamageType.FIRE))
				return TriggerResult.keep();
			if (ev.getMeta().isSecondary())
				return TriggerResult.keep();
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (!fd.hasStatus(StatusType.INJURY))
				return TriggerResult.keep();
			int stacks = fd.getStatus(StatusType.INJURY).getStacks();
			ev.getMeta().addDamageSlice(new DamageSlice(pdata, damage * stacks, DamageType.FIRE, DamageStatTracker.of(id + slot, this)));
			fd.applyStatus(StatusType.INJURY, data, -stacks, -1, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				"Toggleable, off by default. When active, your basic attacks are weakened by " + DescUtil.val(dec)
						+ " in exchange for applying " + GlossaryTag.INJURY.tag(this, stacks)
						+ " every " + DescUtil.val(hitReq) + " hits. When inactive, dealing " + GlossaryTag.FIRE.tag(this) + " damage removes all stacks of "
						+ GlossaryTag.INJURY.tag(this) + " and deals " + GlossaryTag.FIRE.tag(this, damage)
						+ " damage per stack removed.");
		activeIcon = item.withType(Material.TORCH);
	}
}
