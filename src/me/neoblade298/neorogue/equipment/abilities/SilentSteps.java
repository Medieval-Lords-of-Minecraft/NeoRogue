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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class SilentSteps extends Equipment {
	private static final String ID = "SilentSteps";
	private int duration, damage;
	
	public SilentSteps(boolean isUpgraded) {
		super(ID, "Silent Steps", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 5, 0, 0));
		duration = isUpgraded ? 3 : 2;
		damage = isUpgraded ? 25 : 15;
	}

	@Override
	public void setupReforges() {
		addReforge(BasicManaManipulation.get(), SilentSteps2.get(), Fade.get(), Vanish.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			String buffId = UUID.randomUUID().toString();
			data.addTrigger(id, Trigger.PRE_RECEIVE_STATUS, (pdata2, in2) -> {
				PreApplyStatusEvent ev = (PreApplyStatusEvent) in2;
				if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
				ev.getDurationBuffList().add(new Buff(data, 20, 0, BuffStatTracker.ignored(this)));
				return TriggerResult.keep();
			});

			data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata3, in3) -> {
				if (!pdata3.hasStatus(StatusType.STEALTH)) return TriggerResult.keep();
				PreDealDamageEvent ev = (PreDealDamageEvent) in3;
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
						new Buff(pdata3, damage, 0, StatTracker.damageBuffAlly(buffId, this)));
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				GlossaryTag.POWER.tag(this) + ". Whenever you receive " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by " + DescUtil.yellow(duration) + "." +
				" Damage dealt is increased by " + DescUtil.yellow(damage) + " if you have " + GlossaryTag.STEALTH.tag(this) +".");
	}
}
