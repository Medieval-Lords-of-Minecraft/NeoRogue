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
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class BalefulStrike extends Equipment {
	private static final String ID = "BalefulStrike";
	private static final int CHARGE_TIME = 3; // 3 seconds
	private double damageMultiplier;
	private double applyMultiplier;
	
	public BalefulStrike(boolean isUpgraded) {
		super(ID, "Baleful Strike", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damageMultiplier = isUpgraded ? 3 : 2;
		applyMultiplier = isUpgraded ? 1.5 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta chargeTicks = new ActionMeta();
		ItemStack icon = item.clone();
		ItemStack activeIcon = icon.withType(Material.PAPER);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// Every second (20 ticks), increment charge if not at max
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (chargeTicks.getCount() < CHARGE_TIME) {
				chargeTicks.addCount(1);
				// Use active icon when charged
				if (chargeTicks.getCount() >= CHARGE_TIME) {
					inst.setIcon(activeIcon);
				}
			}
			return TriggerResult.keep();
		});
		
		// On basic attack, if charged, deal bonus damage based on poison stacks and apply more poison
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (chargeTicks.getCount() < CHARGE_TIME) return TriggerResult.keep();
			
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			LivingEntity target = ev.getTarget();
			FightData targetData = FightInstance.getFightData(target);
			
			// Get current poison stacks on target
			int poisonStacks = targetData.getStatus(StatusType.POISON).getStacks();
			
			if (poisonStacks > 0) {
				// Deal bonus damage based on poison stacks
				int bonusDamage = (int) (poisonStacks * damageMultiplier);
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
						new Buff(data, bonusDamage, 0, StatTracker.damageBuffAlly(id + slot, this)));
				
				// Apply poison stacks after the attack
				int stacksToApply = (int) (poisonStacks * applyMultiplier);
				FightInstance.applyStatus(target, StatusType.POISON, data, stacksToApply, 100); // 5 seconds = 100 ticks
			}
			
			// Reset charge and icon
			chargeTicks.setCount(0);
			inst.setIcon(icon);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				"Passive. If you don't basic attack for <white>" + CHARGE_TIME + "s</white>, your next basic attack " +
				"deals <yellow>" + damageMultiplier + "x</yellow> the current " + GlossaryTag.POISON.tag(this) + " stacks on the enemy " +
				"as " + GlossaryTag.PIERCING.tag(this) + " damage and then applies <yellow>" + applyMultiplier + "x</yellow> " +
				"the current " + GlossaryTag.POISON.tag(this) + " stacks [<white>5s</white>] to the target.");
	}
}
