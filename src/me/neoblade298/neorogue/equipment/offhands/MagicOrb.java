package me.neoblade298.neorogue.equipment.offhands;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class MagicOrb extends Equipment {
	private int percentDmgBuff;
	
	public MagicOrb(boolean isUpgraded) {
		super("magicOrb", "Magic Orb", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND);
		percentDmgBuff = isUpgraded ? 20 : 10;
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger Bind, EquipSlot es, int slot) {
		SpareScrollInstance inst = new SpareScrollInstance();
		data.addTrigger(id, Trigger.CAST_USABLE, inst);
	}

	public class SpareScrollInstance implements TriggerAction {
		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			CastUsableEvent ev = (CastUsableEvent) inputs;
			Player p = data.getPlayer();
			ev.addBuff(PropertyType.DAMAGE, p.getUniqueId(), UUID.randomUUID().toString(), percentDmgBuff / 100.0, true);
			return TriggerResult.keep();
		}
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_PEARL, "Deal <yellow>" + percentDmgBuff + "%</yellow> increased damage with skills.");
	}
}
