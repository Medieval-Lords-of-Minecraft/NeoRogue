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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Lethality extends Equipment {
	private static final String ID = "Lethality";
	private int thres, inc;
	
	public Lethality(boolean isUpgraded) {
		super(ID, "Lethality", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		thres = isUpgraded ? 30 : 40;
		inc = isUpgraded ? 40 : 25;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.PIERCING)) return TriggerResult.keep();
			if (data.getStamina() < data.getMaxStamina() * 0.5) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 3) return TriggerResult.keep();
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			String buffId = UUID.randomUUID().toString();
			data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
				if (data.getStamina() < thres) return TriggerResult.keep();
				PreDealDamageEvent ev2 = (PreDealDamageEvent) in2;
				ev2.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.PIERCING), new Buff(data, inc, 0, StatTracker.damageBuffAlly(buffId, this)));
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_CRYSTALS,
				GlossaryTag.POWER.tag(this) + ". Activates after dealing " + GlossaryTag.PIERCING.tag(this) + " damage " + DescUtil.white(3) + " times while above " + DescUtil.white("50%") + " stamina. Increase " + GlossaryTag.PIERCING.tag(this) + " damage by " + DescUtil.yellow(inc)
				+ " while above " + DescUtil.yellow(thres) + " stamina.");
	}
}
