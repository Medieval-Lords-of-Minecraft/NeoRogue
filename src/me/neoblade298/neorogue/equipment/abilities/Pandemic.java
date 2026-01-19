package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
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

public class Pandemic extends Equipment {
	private static final String ID = "Pandemic";
	private double bonusDamage;
	private int bonusPoison, areaPoison;
	private static final int radius = 5;
	private static final TargetProperties tp = TargetProperties.radius(radius, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST_COLOR_TRANSITION)
		.count(50).spread(2, 0.5).offsetY(1);
	
	public Pandemic(boolean isUpgraded) {
		super(ID, "Pandemic", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		bonusDamage = isUpgraded ? 0.7 : 0.6;
		bonusPoison = isUpgraded ? 5 : 4;
		areaPoison = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String statusName = p.getName() + "-pandemic";
		
		// Mark enemies and spread poison in area on basic attack
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd == null) return TriggerResult.keep();
			
			// Apply 3 second mark to primary target
			Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
			fd.applyStatus(s, data, 1, 60);
			
			// Spread poison in area around target
			pc.play(p, ev.getTarget().getLocation());
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, ev.getTarget().getLocation(), tp)) {
				FightInstance.applyStatus(ent, StatusType.POISON, data, areaPoison, -1);
			}
			
			return TriggerResult.keep();
		});
		
		// Bonus damage when dealing poison damage to marked enemies
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.POISON)) return TriggerResult.keep();
			
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd == null || !fd.hasStatus(statusName)) return TriggerResult.keep();
			
			// Add bonus damage
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
				Buff.multiplier(data, bonusDamage, BuffStatTracker.damageBuffAlly(id, this)));
			
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
	public void setupItem() {
		item = createItem(Material.FERMENTED_SPIDER_EYE,
				"Passive. Your basic attacks mark enemies for <white>3s</white> and spread " +
				GlossaryTag.POISON.tag(this, areaPoison, true) + " in a <white>" + radius + "</white> block radius. " +
				GlossaryTag.POISON.tag(this) + " damage against marked enemies deals <yellow>" + 
				(int)(bonusDamage * 100) + "%</yellow> increased damage. " +
				"Applying " + GlossaryTag.POISON.tag(this) + " to marked enemies grants " +
				GlossaryTag.POISON.tag(this, bonusPoison, true) + " additional stacks.");
	}
}
