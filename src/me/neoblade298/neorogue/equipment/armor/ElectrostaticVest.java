package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class ElectrostaticVest extends Equipment {
	private static final String ID = "ElectrostaticVest";
	private int baseShields;
	
	public ElectrostaticVest(boolean isUpgraded) {
		super(ID, "Electrostatic Vest", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		baseShields = isUpgraded ? 6 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta stackCounter = new ActionMeta();
		
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.ELECTRIFIED)) return TriggerResult.keep();
			
			// Calculate shields (base + stacks)
			int shieldAmount = baseShields + stackCounter.getCount();
			
			// Grant shields for 5 seconds (100 ticks)
			Player p = data.getPlayer();
			data.addSimpleShield(p.getUniqueId(), shieldAmount, 100);
			
			// Increment stack counter permanently
			stackCounter.addCount(1);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CHAINMAIL_CHESTPLATE,
				"Passive. Upon dealing " + GlossaryTag.ELECTRIFIED.tag(this) + " damage, gain " + 
				GlossaryTag.SHIELDS.tag(this, baseShields, true) + " [<white>5s</white>], increased by <white>1</white> " +
				"every time this happens.");
	}
}
