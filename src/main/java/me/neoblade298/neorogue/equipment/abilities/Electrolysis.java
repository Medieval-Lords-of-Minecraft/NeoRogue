package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
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
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Electrolysis extends Equipment {
	private static final String ID = "Electrolysis";
	private double mult;
	private int mana, multStr;
	
	public Electrolysis(boolean isUpgraded) {
		super(ID, "Electrolysis", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		mult = isUpgraded ? 0.5 : 0.3;
		mana = isUpgraded ? 10 : 5;
		multStr = (int) (mult * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 5;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.ELECTRIFIED)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msgRaw(p, Component.text("").append(hoverable).append(Component.text(" was activated", NamedTextColor.GRAY)));

			String buffId = UUID.randomUUID().toString();
			data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
				PreDealDamageEvent ev2 = (PreDealDamageEvent) in2;
				FightData fd = FightInstance.getFightData(ev2.getTarget());
				if (!ev2.getMeta().containsType(DamageType.LIGHTNING) || !fd.hasStatus(StatusType.ELECTRIFIED)) return TriggerResult.keep();
				ev2.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.LIGHTNING), Buff.multiplier(data, mult, BuffStatTracker.damageBuffAlly(buffId, this)));
				data.addMana(mana);
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ORANGE_DYE,
				GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.ELECTRIFIED.tag(this) + " " + DescUtil.white(5) + " times. Dealing " + GlossaryTag.LIGHTNING.tag(this) + " damage to enemies with " + GlossaryTag.ELECTRIFIED.tag(this) + " increases the damage by "
				+ DescUtil.yellow(multStr + "%") + " and grants " + DescUtil.yellow(mana) + " mana.");
	}
}
