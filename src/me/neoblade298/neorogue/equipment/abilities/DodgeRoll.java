package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DodgeRoll extends Equipment {
	private static final String ID = "DodgeRoll";
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5F);
	private int dur = 5;
	
	public DodgeRoll(boolean isUpgraded) {
		super(ID, "Dodge Roll", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 10 : 20, isUpgraded ? 8 : 12, 0));
		properties.addUpgrades(PropertyType.COOLDOWN, PropertyType.STAMINA_COST);
		dur = isUpgraded ? 10 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				"On cast, " + GlossaryTag.DASH.tag(this) + " forward and gain " + GlossaryTag.STEALTH.tag(this, 1, false) + " and "
				+ GlossaryTag.EVADE.tag(this, 1, false) + " for <yellow>" + dur + "</yellow> seconds.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			sc.play(p, p);
			data.dash();
			data.applyStatus(StatusType.STEALTH,data, 1, dur * 20);
			data.applyStatus(StatusType.EVADE,data, 1, dur * 20);
			return TriggerResult.keep();
		}));
	}
}
