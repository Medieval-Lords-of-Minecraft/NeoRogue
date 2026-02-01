package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class AdaptiveChemvest extends Equipment {
	private static final String ID = "AdaptiveChemvest";
	private int shields;
	
	public AdaptiveChemvest(boolean isUpgraded) {
		super(ID, "Adaptive Chemvest", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		shields = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.POISON)) return TriggerResult.keep();
			Player p = data.getPlayer();
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Dealing " + GlossaryTag.POISON.tag(this) + " damage grants you " + 
				GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>].");
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.GREEN);
		item.setItemMeta(meta);
	}
}
