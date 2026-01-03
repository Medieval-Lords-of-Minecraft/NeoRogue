package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Paranoia extends Equipment {
	private static final String ID = "Paranoia";
	private static final int MAX_STACKS = 5;
	private static final int SHIELDS_PER_STACK = 8;
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private int damagePerStack, insanity;
	
	public Paranoia(boolean isUpgraded) {
		super(ID, "Paranoia", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damagePerStack = isUpgraded ? 150 : 100;
		insanity = isUpgraded ? 120 : 80;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta stacks = new ActionMeta();
		ActionMeta timeSinceAttack = new ActionMeta();  // Tracks seconds since last basic attack
		ItemStack icon = item.clone();
		ItemStack activeIcon = icon.withType(Material.PAPER);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// Every second (20 ticks), gain a stack if not at max, and apply insanity if no attack in 2s
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			// Gain stacks
			if (stacks.getCount() < MAX_STACKS) {
				stacks.addCount(1);
				// Use active icon when we have stacks
				ItemStack currentIcon = activeIcon.clone();
				currentIcon.setAmount(stacks.getCount());
				inst.setIcon(currentIcon);
			}
			
			// Track time since last attack and apply insanity
			timeSinceAttack.addCount(1);
			if (timeSinceAttack.getCount() >= 2) {
				// Haven't attacked in 2+ seconds, apply insanity
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
					FightInstance.applyStatus(ent, StatusType.INSANITY, data, insanity, -1);
				}
			}
			
			return TriggerResult.keep();
		});
		
		// On basic attack, consume all stacks for damage and shields, and reset timer
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			int currentStacks = stacks.getCount();
			
			// Reset attack timer
			timeSinceAttack.setCount(0);
			
			// Only grant bonuses if we have stacks
			if (currentStacks > 0) {
				// Deal bonus damage
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
						new Buff(data, damagePerStack * currentStacks, 0, StatTracker.damageBuffAlly(id + slot, this)));
				
				// Grant shields
				int shieldAmount = SHIELDS_PER_STACK * currentStacks;
				data.addSimpleShield(p.getUniqueId(), shieldAmount, 100); // 5 seconds = 100 ticks
				
				// Reset stacks and icon to base
				stacks.setCount(0);
				inst.setIcon(icon);
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				"Passive. For every second you don't basic attack, gain a stack (up to <white>" + MAX_STACKS + "</white>). " +
				"The next time you basic attack, deal " + GlossaryTag.PIERCING.tag(this, damagePerStack, true) + " damage " +
				"and gain " + GlossaryTag.SHIELDS.tag(this, SHIELDS_PER_STACK, true) + " for <white>5s</white> per stack. " +
				"If you haven't basic attacked in the past <white>2s</white>, apply " + 
				GlossaryTag.INSANITY.tag(this, insanity, true) + " to enemies around you every second.");
	}
}
