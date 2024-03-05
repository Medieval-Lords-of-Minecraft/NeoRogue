package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Skirmisher extends Equipment {
	private static final SoundContainer sound = new SoundContainer(Sound.ENTITY_ALLAY_HURT, 0.8F);
	private int shields;
	
	public Skirmisher(boolean isUpgraded) {
		super("skirmisher", "Skirmisher", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		shields = isUpgraded ? 10 : 6;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new SkirmisherInstance(id, p, data));
	}
	
	private class SkirmisherInstance extends PriorityAction {
		private int count = 0;
		public SkirmisherInstance(String id, Player p, PlayerFightData data) {
			super(id);
			action = (pdata, in) -> {
				if (++count >= 3) {
					sound.play(p, p);
				}
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BAMBOO,
				"Passive. Every third basic attack, knock back all enemies around you, gain speed <white>1</white> for <white>3</white> seconds,"
				+ " and " + GlossaryTag.SHIELDS.tag(this, shields, true) + " for <white>5</white> seconds.");
	}
}
