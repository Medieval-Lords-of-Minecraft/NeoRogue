package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Warmup extends Equipment {
	private static final String ID = "Warmup";
	private int timer, shields;

	public Warmup(boolean isUpgraded) {
		super(ID, "Warmup", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		timer = isUpgraded ? 7 : 10;
		shields = isUpgraded ? 15 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setCount(timer);
		inst.setAction((pdata, in) -> {
			inst.addCount(-1);
			if (inst.getCount() <= 0) {
				Player p = data.getPlayer();
				Sounds.fire.play(p, p);
				Util.msgRaw(p, this.hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));
				data.addStaminaRegen(1);
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.PLAYER_TICK, inst);
		data.addTrigger(ID, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
			inst.addCount(1);
			if (inst.getCount() < -5) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RED_DYE,
				"Passive. After <yellow>" + timer + "</yellow> seconds, gain <white>1</white> stamina regen and " + GlossaryTag.SHIELDS.tag(this, shields, true) + "."
				+ " Taking health damage increases the timer by <white>1</white>.");
	}
}
