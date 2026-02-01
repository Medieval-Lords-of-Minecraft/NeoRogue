package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Shield;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Inexorable extends Equipment {
	private static final String ID = "Inexorable";
	private int shields, refresh;
	
	public Inexorable(boolean isUpgraded) {
		super(ID, "Inexorable", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		shields = isUpgraded ? 100 : 60;
		refresh = 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Player p = data.getPlayer();
		ActionMeta am = new ActionMeta();
		am.setDouble(shields);
		Shield shield = data.addPermanentShield(p.getUniqueId(), shields, true);
		am.setObject(shield);
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {			am.addCount(1);
			if (am.getCount() == refresh) {
				Sounds.equip.play(p, p);
				Shield s = (Shield) am.getObject();
				s.remove();
				s = data.addPermanentShield(p.getUniqueId(), shields, true);
				am.setObject(s);
				am.setCount(0);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_BLOCK,
				"Passive. This ability grants " + GlossaryTag.SHIELDS.tag(this, shields, true) + " at the start of the fight. Automatically regenerates every " + 
				DescUtil.white(refresh + "s") + ".");
	}
}
