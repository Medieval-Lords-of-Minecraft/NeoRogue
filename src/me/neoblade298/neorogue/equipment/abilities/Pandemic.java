package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
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
	private static final int radius = 6;
	private static final TargetProperties tp = TargetProperties.radius(radius, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
		.count(50).spread(2, 0.5).offsetY(1).dustOptions(new DustOptions(Color.GREEN, 1F));
	
	public Pandemic(boolean isUpgraded) {
		super(ID, "Pandemic", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(40, 20, 0, 0).add(PropertyType.AREA_OF_EFFECT, radius));
		bonusDamage = isUpgraded ? 0.9 : 0.6;
		bonusPoison = isUpgraded ? 7 : 5;
		areaPoison = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());
			String statusName = data.getPlayer().getName() + "-pandemic";

			// Mark enemies on basic attack
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in2;
				FightData fd = FightInstance.getFightData(ev.getTarget());
				if (fd == null) return TriggerResult.keep();
				
				// Apply 3 second mark to primary target
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
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
					Buff.multiplier(data, bonusDamage, BuffStatTracker.damageBuffAlly(id, this)));
				
				return TriggerResult.keep();
			});
			
			// Increase poison application to marked enemies and spread poison in area
			data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata2, in2) -> {
				PreApplyStatusEvent ev = (PreApplyStatusEvent) in2;
				if (!ev.isStatus(StatusType.POISON)) return TriggerResult.keep();
				if (ev.isSecondary()) return TriggerResult.keep();
				
				FightData fd = ev.getTarget();
				if (fd == null || !fd.hasStatus(statusName)) return TriggerResult.keep();
				
				// Add bonus poison stacks
				ev.getStacksBuffList().add(Buff.increase(data, bonusPoison, BuffStatTracker.statusBuff(id, this)));
				
				// Spread poison in area around marked target
				Player p = data.getPlayer();
				LivingEntity target = fd.getEntity();
				pc.play(p, target.getLocation());
				Sounds.water.play(p, p);
				LinkedList<LivingEntity> list = TargetHelper.getEntitiesInRadius(p, target.getLocation(), tp);
				for (LivingEntity ent : list) {
					if (ent == ev.getTarget().getEntity()) continue;
					FightData targetFd = FightInstance.getFightData(ent);
					if (targetFd == null) continue;
					targetFd.applyStatus(StatusType.POISON, data, areaPoison, -1, null, true);
				}
				
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FERMENTED_SPIDER_EYE,
				GlossaryTag.POWER.tag(this) + ". Your basic attacks mark enemies " + DescUtil.duration(3, false) + ". " +
				GlossaryTag.POISON.tag(this) + " damage against marked enemies deals " +
				DescUtil.yellow((int)(bonusDamage * 100) + "%") + " increased damage. " +
				"Applying " + GlossaryTag.POISON.tag(this) + " to marked enemies grants an additional " +
				GlossaryTag.POISON.tag(this, bonusPoison, true) + " stacks and spreads " +
				GlossaryTag.POISON.tag(this, areaPoison, true) + " to nearby enemies.");
	}
}
