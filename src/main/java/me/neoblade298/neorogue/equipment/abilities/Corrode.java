package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Corrode extends Equipment implements Power {
	private static final String ID = "Corrode";
	private double bonusDamage;
	private int bonusPoison;
	
	public Corrode(boolean isUpgraded) {
		super(ID, "Corrode", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		bonusDamage = isUpgraded ? 0.6 : 0.4;
		bonusPoison = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd == null || !fd.hasStatus(StatusType.POISON)) return TriggerResult.keep();
			if (am.addCount(1) < 3) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupReforges() {
		addReforge(Mastermind.get(), Pandemic.get(), NightSurge.get(), Viper.get());

	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		Player p = data.getPlayer();
		String statusName = p.getName() + "-corrode";

		// Mark enemies on basic attack
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
			BasicAttackEvent bev = (BasicAttackEvent) in2;
			FightData fd2 = FightInstance.getFightData(bev.getTarget());
			if (fd2 == null) return TriggerResult.keep();

			// Apply 3 second mark
			Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd2, true);
			fd2.applyStatus(s, data, 1, 60, this);

			return TriggerResult.keep();
		});

		// Bonus damage when dealing poison damage to marked enemies
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
			PreDealDamageEvent dev = (PreDealDamageEvent) in2;
			if (!dev.getMeta().containsType(DamageType.POISON)) return TriggerResult.keep();

			FightData fd2 = FightInstance.getFightData(dev.getTarget());
			if (fd2 == null || !fd2.hasStatus(statusName)) return TriggerResult.keep();

			// Add bonus damage
			dev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.POISON), Buff.multiplier(data, bonusDamage, BuffStatTracker.damageBuffAlly(id, this)));

			return TriggerResult.keep();
		});

		// Increase poison application to marked enemies
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata2, in2) -> {
			PreApplyStatusEvent sev = (PreApplyStatusEvent) in2;
			if (!sev.isStatus(StatusType.POISON)) return TriggerResult.keep();

			FightData fd2 = sev.getTarget();
			if (fd2 == null || !fd2.hasStatus(statusName)) return TriggerResult.keep();

			// Add bonus poison stacks
			sev.getStacksBuffList().add(Buff.increase(data, bonusPoison, BuffStatTracker.statusBuff(id, this)));

			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.SLIME_BALL,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after basic attacking " + DescUtil.white(3) + " poisoned enemies. Your basic attacks mark enemies " + DescUtil.duration(3, false) + ". " +
				GlossaryTag.POISON.tag(this) + " damage against marked enemies deals " + 
				DescUtil.yellow((int)(bonusDamage * 100) + "%") + " increased damage. " +
				GlossaryTag.POISON.tag(this) + " applied to marked enemies is increased by " + DescUtil.yellow(bonusPoison) + ".");
	}
}
