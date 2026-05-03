package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Dissonance extends Equipment {
	private static final String ID = "Dissonance";
	private int mana, shields;
	
	public Dissonance(boolean isUpgraded) {
		super(ID, "Dissonance", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 20, 0));
		mana = isUpgraded ? 9 : 6;
		shields = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.KNOWLEDGE_BOOK,
				"On cast, anytime you deal a damage type that is different from your previous damage type, " +
				"gain " + DescUtil.yellow(mana) + " mana and " + GlossaryTag.SHIELDS.tag(this, shields, true) +
				" [" + DescUtil.white("5s") + "]. Can only be cast <yellow>once</yellow> per fight.");
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta lastDamageType = new ActionMeta(); // Stores the last damage type as an Object
		DissonanceInstance inst = new DissonanceInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
		
		// Track damage dealt and compare types
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			if (!inst.isActive()) return TriggerResult.keep();
			
			DealDamageEvent ev = (DealDamageEvent) in;
			Player p = data.getPlayer();
			
			// Get the primary damage type from the first slice
			if (ev.getMeta().getSlices().isEmpty()) return TriggerResult.keep();
			DamageSlice primarySlice = ev.getMeta().getSlices().getFirst();
			DamageType currentType = primarySlice.getPostBuffType();
			
			// Check if this is a different type from the last damage dealt
			DamageType lastType = (DamageType) lastDamageType.getObject();
			if (lastType == null || currentType != lastType) {
				// Grant mana and shields
				data.addMana(mana);
				data.addSimpleShield(p.getUniqueId(), shields, 100); // 5 seconds = 100 ticks
				
				// Update last damage type
				lastDamageType.setObject(currentType);
			}
			
			return TriggerResult.keep();
		});
	}
	
	private class DissonanceInstance extends EquipmentInstance {
		private boolean active = false;
		
		public DissonanceInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			
			action = (pdata, in) -> {
				active = true;
				return TriggerResult.keep();
			};
		}
		
		public boolean isActive() {
			return active;
		}
	}
}
