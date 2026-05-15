package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AfterImage extends Equipment {
	private static final String ID = "AfterImage";
	private int shields;
	
	public AfterImage(boolean isUpgraded) {
		super(ID, "After Image", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		shields = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DASH, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() < 1) return TriggerResult.keep();
			Sounds.fire.play(data.getPlayer(), data.getPlayer());
			Util.msg(data.getPlayer(), hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTrigger(id, Trigger.DASH, (pdata2, inputs) -> {
				Player p = data.getPlayer();
				data.addSimpleShield(p.getUniqueId(), shields, 100);
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				GlossaryTag.POWER.tag(this) + ". Every time you " + GlossaryTag.DASH.tag(this) + ", gain " + 
				GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>].");
	}
}