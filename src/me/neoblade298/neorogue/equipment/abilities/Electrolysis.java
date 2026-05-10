package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Electrolysis extends Equipment {
	private static final String ID = "Electrolysis";
	private double mult;
	private int mana, multStr;
	
	public Electrolysis(boolean isUpgraded) {
		super(ID, "Electrolysis", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 10, 0, 0));
		mult = isUpgraded ? 0.5 : 0.3;
		mana = isUpgraded ? 10 : 5;
		multStr = (int) (mult * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			String buffId = UUID.randomUUID().toString();
			data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
				PreDealDamageEvent ev = (PreDealDamageEvent) in2;
				FightData fd = FightInstance.getFightData(ev.getTarget());
				if (!ev.getMeta().containsType(DamageType.LIGHTNING) || !fd.hasStatus(StatusType.ELECTRIFIED)) return TriggerResult.keep();
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.LIGHTNING), Buff.multiplier(data, mult, BuffStatTracker.damageBuffAlly(buffId, this)));
				data.addMana(mana);
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ORANGE_DYE,
				GlossaryTag.POWER.tag(this) + ". Dealing " + GlossaryTag.LIGHTNING.tag(this) + " damage to enemies with " + GlossaryTag.ELECTRIFIED.tag(this) + " increases the damage by "
				+ DescUtil.yellow(multStr + "%") + " and grants " + DescUtil.yellow(mana) + " mana.");
	}
}
