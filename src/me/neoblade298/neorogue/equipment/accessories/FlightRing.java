package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.EvadeEvent;

public class FlightRing extends Equipment {
	private static final String ID = "FlightRing";
	private int evade;
	
	public FlightRing(boolean isUpgraded) {
		super(ID, "Flight Ring", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		evade = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// Grant evade at the start of the fight
		data.applyStatus(StatusType.EVADE, data, evade, -1);
		
		// Track the number of evades that should trigger dash
		ActionMeta dashCount = new ActionMeta();
		
		// Trigger on evade to dash away
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			EvadeEvent ev = (EvadeEvent) in;
			
			// Check if we still have dashes left
			if (dashCount.getCount() >= evade) {
				return TriggerResult.keep();
			}
			
			// Get the damager entity from the DamageMeta
			if (ev.getDamageMeta() == null || ev.getDamageMeta().getOwner() == null) {
				return TriggerResult.keep();
			}
			
			LivingEntity damager = ev.getDamageMeta().getOwner().getEntity();
			Location playerLoc = p.getLocation();
			Location damagerLoc = damager.getLocation();
			
			// Calculate dash direction away from the enemy
			Vector awayFromEnemy = playerLoc.toVector().subtract(damagerLoc.toVector()).normalize();
			
			// Dash away from the enemy
			data.dash(awayFromEnemy);
			
			// Increment dash counter
			dashCount.addCount(1);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"Start every fight with " + GlossaryTag.EVADE.tag(this, evade, true) + ". " +
				"The first " + (evade == 3 ? "<yellow>3</yellow>" : "<yellow>2</yellow>") + " times you " + 
				GlossaryTag.EVADE.tag(this) + ", " + GlossaryTag.DASH.tag(this) + " away from the enemy.");
	}
}
