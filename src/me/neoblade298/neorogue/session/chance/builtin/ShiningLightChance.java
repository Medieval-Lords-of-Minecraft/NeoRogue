package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData.PlayerSlot;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import net.kyori.adventure.text.Component;

public class ShiningLightChance extends ChanceSet {

	public ShiningLightChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "ShiningLight", "Shining Light", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "A bright light emanates from the next room. You can feel a sensation, as if both burning"
				+ " and purifying you.");

		stage.addChoice(new ChanceChoice(Material.DIAMOND_SWORD, "Enter the room",
				"Two pieces of equipment get upgraded at the cost of taking <yellow>20%</yellow> of your max health as damage.",
				"Not enough health",
				(s, inst, data) -> {
					return data.getMaxHealth() * 0.2 < data.getHealth();
				},
				(s, inst, data) -> {
					Player p = data.getPlayer();
					Util.msg(p, "You are engulfed by the light. You feel a sharp pain, but you can tell your equipment has absorbed some power.");
					
					data.damagePercent(0.2);
					for (int i = 0; i < 2; i++) {
						PlayerSlot ps = data.getRandomEquipment(false);
						if (ps == null) {
							Util.msg(p, "You had nothing else to upgrade!");
							continue;
						}
						data.upgradeEquipment(ps.getEquipSlot(), ps.getSlot());
						Equipment eq = data.getEquipment(ps.getEquipSlot())[ps.getSlot()];
						Util.msg(p, Component.text("You upgraded your ").append(eq.getHoverable()));
						s.broadcastOthers(SharedUtil.color("<yellow>" + p.getName() + "</yellow> upgraded their ").append(eq.getHoverable()), p);
					}
					return null;
				}));
		
		stage.addChoice(new ChanceChoice(Material.LEATHER_BOOTS, "Find another way",
				"Looks dangerous. I'd rather not.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					Util.msg(p, "No way, that's too bright, it's bad for my skin.");
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> decided not to enter the light!", p);
					return null;
				}));
	}
}
