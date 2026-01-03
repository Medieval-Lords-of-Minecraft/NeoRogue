package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class StaticSurge extends Equipment {
	private static final String ID = "StaticSurge";
	private int damage, electrified;
	
	public StaticSurge(boolean isUpgraded) {
		super(ID, "Static Surge", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damage = isUpgraded ? 50 : 30;
		electrified = isUpgraded ? 50 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		
		// Track when sprinting is toggled
		data.addTrigger(id, Trigger.TOGGLE_SPRINT, (pdata, in) -> {
			if (p.isSprinting()) {
				// Player started sprinting - record the time
				am.setTime(System.currentTimeMillis());
			} else {
				// Player stopped sprinting - reset time
				am.setTime(0);
			}
			return TriggerResult.keep();
		});
		
		// Apply bonus damage and electrified on basic attack if sprinted for 1+ second
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			// Check if player has been sprinting for at least 1 second (1000ms)
			if (am.getTime() == 0 || System.currentTimeMillis() - am.getTime() < 1000) {
				return TriggerResult.keep();
			}
			
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			
			// Deal additional lightning damage
			ev.getMeta().addDamageSlice(
					new DamageSlice(data, damage, DamageType.LIGHTNING, DamageStatTracker.of(id + slot, this)));
			
			// Apply electrified
			FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, electrified, -1);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				"Passive. If you have been sprinting for at least <white>1s</white>, your basic attacks deal an additional " + 
				GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and apply " + 
				GlossaryTag.ELECTRIFIED.tag(this, electrified, true) + ".");
	}
}
