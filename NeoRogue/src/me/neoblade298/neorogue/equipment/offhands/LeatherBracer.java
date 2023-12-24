package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;

import me.neoblade298.neorogue.equipment.Offhand;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class LeatherBracer extends Offhand {
	private int instances;
	
	public LeatherBracer(boolean isUpgraded) {
		super("leatherBracer", "Leather Bracer", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		instances = isUpgraded ? 3 : 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new LeatherBracerInstance(p));
	}
	
	private class LeatherBracerInstance implements TriggerAction {
		private Player p;
		public LeatherBracerInstance(Player p) {
			this.p = p;
		}

		@Override
		public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
			Util.playSound(p, Sound.ITEM_SHIELD_BREAK, 1F, 1F, false);
			return TriggerResult.of(true, true);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(this, Material.LEATHER, null, "Prevents the first <yellow>" + instances + "</yellow> instances of taking damage in a fight.");
	}
}
