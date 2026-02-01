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
	public void setupReforges() {
		addReforge(Obfuscation.get(), Paranoia.get());
		addReforge(Mastermind.get(), Analyze2.get(), BalefulStrike.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta stacks = new ActionMeta();
		ItemStack icon = item.clone();
		ItemStack activeIcon = icon.withType(Material.PAPER);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// Every second (20 ticks), gain a stack if not at max
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (stacks.getCount() < MAX_STACKS) {
				stacks.addCount(1);
				// Use active icon when we have stacks
				ItemStack currentIcon = activeIcon.clone();
				currentIcon.setAmount(stacks.getCount());
				inst.setIcon(currentIcon);
			}
			return TriggerResult.keep();
		});
		
		// On basic attack, consume all stacks for damage and shields
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			Player p = data.getPlayer();
			if (stacks.getCount() <= 0) return TriggerResult.keep();
			
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			int currentStacks = stacks.getCount();
			
			// Deal bonus damage
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
					new Buff(data, damagePerStack * currentStacks, 0, StatTracker.damageBuffAlly(id + slot, this)));
			
			// Grant shields
			int shieldAmount = SHIELDS_PER_STACK * currentStacks;
			data.addSimpleShield(p.getUniqueId(), shieldAmount, 100); // 5 seconds = 100 ticks
			
			// Reset stacks and icon to base
			stacks.setCount(0);
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
