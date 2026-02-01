package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Corrode extends Equipment {
	private static final String ID = "Corrode";
	private double bonusDamage;
	private int bonusPoison;
	
	public Corrode(boolean isUpgraded) {
		super(ID, "Corrode", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		bonusDamage = isUpgraded ? 0.6 : 0.4;
		bonusPoison = isUpgraded ? 120 : 80;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Player p = data.getPlayer();
		String statusName = p.getName() + "-corrode";
		
		// Mark enemies on basic attack
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd == null) return TriggerResult.keep();
			
			// Apply 3 second mark
			Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
			fd.applyStatus(s, data, 1, 60);
			
			return TriggerResult.keep();
		});
		
		// Bonus damage when dealing poison damage to marked enemies
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.POISON)) return TriggerResult.keep();
			
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd == null || !fd.hasStatus(statusName)) return TriggerResult.keep();
			
			// Add bonus damage
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.POISON), Buff.multiplier(data, bonusDamage, BuffStatTracker.damageBuffAlly(id, this)));
			
			return TriggerResult.keep();
		});
		
		// Increase poison application to marked enemies
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.POISON)) return TriggerResult.keep();
			
			FightData fd = ev.getTarget();
			if (fd == null || !fd.hasStatus(statusName)) return TriggerResult.keep();
			
			// Add bonus poison stacks
			ev.getStacksBuffList().add(Buff.increase(data, bonusPoison, BuffStatTracker.statusBuff(id, this)));
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupReforges() {
		addReforge(Mastermind.get(), Pandemic.get(), NightSurge.get(), Viper.get());

	}

	@Override
	public void setupItem() {
		item = createItem(Material.SLIME_BALL,
				"Passive. Your basic attacks mark enemies for <white>3s</white>. " +
				GlossaryTag.POISON.tag(this) + " damage against marked enemies deals <yellow>" + 
				(int)(bonusDamage * 100) + "%</yellow> increased damage. " +
				GlossaryTag.POISON.tag(this) + " applied to marked enemies is increased by " + DescUtil.yellow(bonusPoison) + ".");
	}
}
