package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Analyze extends Equipment {
	private static final String ID = "Analyze";
	private static final int MAX_STACKS = 5;
	private static final int SHIELDS_PER_STACK = 4;
	private int damagePerStack;
	
	public Analyze(boolean isUpgraded) {
		super(ID, "Analyze", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damagePerStack = isUpgraded ? 75 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta(); // Tracks seconds since last basic attack
		ActionMeta stacks = new ActionMeta(); // Tracks analyze stacks
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// Every second (20 ticks), gain a stack if not at max
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			am.addCount(1);
			// PLAYER_TICK triggers every tick, so 20 ticks = 1 second
			if (am.getCount() >= 20) {
				am.setCount(0);
				if (stacks.getCount() < MAX_STACKS) {
					stacks.addCount(1);
					icon.setAmount(stacks.getCount());
					inst.setIcon(icon);
				}
			}
			return TriggerResult.keep();
		});
		
		// On basic attack, consume all stacks for damage and shields
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (stacks.getCount() <= 0) return TriggerResult.keep();
			
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			int currentStacks = stacks.getCount();
			
			// Deal bonus damage
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
					new Buff(data, damagePerStack * currentStacks, 0, StatTracker.damageBuffAlly(id + slot, this)));
			
			// Grant shields
			int shieldAmount = SHIELDS_PER_STACK * currentStacks;
			data.addSimpleShield(p.getUniqueId(), shieldAmount, 100); // 5 seconds = 100 ticks
			
			// Reset stacks and icon
			stacks.setCount(0);
			am.setCount(0);
			icon.setAmount(1); // Reset to 1 (default item amount)
			inst.setIcon(icon);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				"Passive. For every second you don't basic attack, gain a stack (up to <white>" + MAX_STACKS + "</white>). " +
				"The next time you basic attack, deal " + GlossaryTag.PIERCING.tag(this, damagePerStack, true) + " damage " +
				"and gain " + GlossaryTag.SHIELDS.tag(this, SHIELDS_PER_STACK, false) + " for <white>5s</white> per stack.");
	}
}
