package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData.PlayerSlot;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import net.kyori.adventure.text.Component;

public class ShiningLightChance extends ChanceSet {

	public ShiningLightChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "shiningLight", "Shining Light", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "A bright light emanates from the next room. You can feel a sensation, as if both burning"
				+ " and purifying you.");

		stage.addChoice(new ChanceChoice(Material.DIAMOND_SWORD, "Enter the room",
				"Two pieces of equipment get upgraded at the cost of taking <yellow>20%</yellow> of your max health as damage.",
				(s, inst, data) -> {
					s.broadcast("You are engulfed by the light. You feel a sharp pain, but you can tell your equipment has absorbed some power.");
					
					for (int i = 0; i < 2; i++) {
						PlayerSlot ps = data.getRandomEquipment(false);
						data.upgradeEquipment(ps.getEquipSlot(), ps.getSlot());
						Equipment eq = data.getEquipment(ps.getEquipSlot())[ps.getSlot()];
						Util.msg(data.getPlayer(), Component.text("You upgraded your ").append(eq.getDisplay()));
					}
					return null;
				}));
		
		stage.addChoice(new ChanceChoice(Material.LEATHER_BOOTS, "Find another way",
				"Looks dangerous. I'd rather not.",
				(s, inst, unused) -> {
					return null;
				}));
	}
}
