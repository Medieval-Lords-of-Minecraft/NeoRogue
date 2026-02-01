package me.neoblade298.neorogue.equipment;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.UseConsumableEvent;

public abstract class Consumable extends Equipment {
	protected SoundContainer drink = new SoundContainer(Sound.ENTITY_WITCH_DRINK);
	public Consumable(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, display, isUpgraded, rarity, ec, EquipmentType.CONSUMABLE, EquipmentProperties.none());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, (pdata, in) -> {
			Player p = data.getPlayer();
			drink.play(p, p);
			data.getSessionData().removeEquipment(es, slot);
			data.runActions(data, Trigger.USE_CONSUMABLE, new UseConsumableEvent(this));
			runConsumableEffects(p, data);
			return TriggerResult.remove();
		});
	}
	
	protected abstract void runConsumableEffects(Player p, PlayerFightData data);
}
