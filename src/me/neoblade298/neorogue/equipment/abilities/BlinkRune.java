package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BlinkRune extends Equipment {
	private static final String ID = "blinkRune";
	private int reps;
	
	public BlinkRune(boolean isUpgraded) {
		super(ID, "Blink Rune", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS,
				EquipmentType.OFFHAND, EquipmentProperties.none());
		reps = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		icon.setAmount(reps);
		ActionMeta am = new ActionMeta();
		am.setCount(reps);
		Trigger tr = data.getSessionData().getPlayerClass() == EquipmentClass.ARCHER ? Trigger.LEFT_CLICK : Trigger.RIGHT_CLICK;
		data.addTrigger(id, tr, (pdata, in) -> {
			Vector v = p.getEyeLocation().getDirection();
			if (p.isOnGround()) {
				p.teleport(p.getLocation().add(0, 0.2, 0));
			}
			p.setVelocity(v.multiply(1.2));

			am.addCount(-1);
			if (am.getCount() <= 0) {
				Sounds.breaks.play(p, p);
				p.getInventory().setItemInOffHand(null);
				return TriggerResult.remove();
			}
			else {
				Sounds.teleport.play(p, p);
				icon.setAmount(am.getCount());
				p.getInventory().setItemInOffHand(icon);
				return TriggerResult.keep();
			}
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				"On right click (left click for <gold>Archer</gold>), dash in the direction you're looking. Works " + DescUtil.yellow(reps + "x") + " per fight.");
	}
}
