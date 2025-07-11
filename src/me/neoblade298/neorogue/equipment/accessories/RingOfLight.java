package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class RingOfLight extends Equipment {
	private static final String ID = "ringOfLight";
	private static final TargetProperties tp = TargetProperties.radius(5, false);
	private int sanct;
	public RingOfLight(boolean isUpgraded) {
		super(ID, "Ring Of Light", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY, EquipmentProperties.ofUsable(0, 0, 0, 0, tp.range));
		sanct = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				FightInstance.applyStatus(ent, StatusType.SANCTIFIED, data, sanct, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, "Casting abilities applies " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + " to nearby enemies.");
	}
}
