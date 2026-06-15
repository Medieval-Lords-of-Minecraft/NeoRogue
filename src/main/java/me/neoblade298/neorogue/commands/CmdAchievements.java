package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.inventory.AchievementsInventory;
import me.neoblade298.neorogue.player.inventory.AchievementsMenuInventory;

public class CmdAchievements extends Subcommand {

	public CmdAchievements(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		if (args.length > 1) {
			String scope = args[1].toLowerCase();
			PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
			if (pd != null) {
				EquipmentClass ec = null;
				if (!scope.equals("global")) {
					for (EquipmentClass cls : EquipmentClass.values()) {
						if (cls.name().equalsIgnoreCase(scope)) {
							ec = cls;
							break;
						}
					}
				}
				new AchievementsInventory(p, pd, ec);
				return;
			}
		}
		new AchievementsMenuInventory(p);
	}
}
