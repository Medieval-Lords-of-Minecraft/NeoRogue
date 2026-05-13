package me.neoblade298.neorogue.equipment.abilities;

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
				EquipmentProperties.ofUsable(30, 15, 0, 0));
		bonusDamage = isUpgraded ? 0.6 : 0.4;
		bonusPoison = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());
			String statusName = data.getPlayer().getName() + "-corrode";

			// Mark enemies on basic attack
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in2;
				FightData fd = FightInstance.getFightData(ev.getTarget());
				if (fd == null) return TriggerResult.keep();
				
				// Apply 3 second mark
				Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
				fd.applyStatus(s, data, 1, 60);
				
				return TriggerResult.keep();
			});
			
			// Bonus damage when dealing poison damage to marked enemies
			data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
				PreDealDamageEvent ev = (PreDealDamageEvent) in2;
				if (!ev.getMeta().containsType(DamageType.POISON)) return TriggerResult.keep();
				
				FightData fd = FightInstance.getFightData(ev.getTarget());
				if (fd == null || !fd.hasStatus(statusName)) return TriggerResult.keep();
				
				// Add bonus damage
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.POISON), Buff.multiplier(data, bonusDamage, BuffStatTracker.damageBuffAlly(id, this)));
				
				return TriggerResult.keep();
			});
			
			// Increase poison application to marked enemies
			data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata2, in2) -> {
				PreApplyStatusEvent ev = (PreApplyStatusEvent) in2;
				if (!ev.isStatus(StatusType.POISON)) return TriggerResult.keep();
				
				FightData fd = ev.getTarget();
				if (fd == null || !fd.hasStatus(statusName)) return TriggerResult.keep();
				
				// Add bonus poison stacks
				ev.getStacksBuffList().add(Buff.increase(data, bonusPoison, BuffStatTracker.statusBuff(id, this)));
				
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupReforges() {
		addReforge(Mastermind.get(), Pandemic.get(), NightSurge.get(), Viper.get());

	}

	@Override
	public void setupItem() {
		item = createItem(Material.SLIME_BALL,
				GlossaryTag.POWER.tag(this) + ". Your basic attacks mark enemies " + DescUtil.duration(3, false) + ". " +
				GlossaryTag.POISON.tag(this) + " damage against marked enemies deals " + 
				DescUtil.yellow((int)(bonusDamage * 100) + "%") + " increased damage. " +
				GlossaryTag.POISON.tag(this) + " applied to marked enemies is increased by " + DescUtil.yellow(bonusPoison) + ".");
	}
}
