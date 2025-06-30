package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class EarthenRing extends Equipment {
	private static final String ID = "earthenRing";
	private int damage;
	
	public EarthenRing(boolean isUpgraded) {
		super(ID, "Earthen Ring", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		damage = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.EARTHEN, DamageStatTracker.of(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CACTUS, "Basic attacks additionally deal <yellow>" + damage + "</yellow> " + GlossaryTag.EARTHEN.tag(this) + " damage.");
	}
}
