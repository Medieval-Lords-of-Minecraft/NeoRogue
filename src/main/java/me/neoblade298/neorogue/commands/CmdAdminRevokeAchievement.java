package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;

public class CmdAdminRevokeAchievement extends Subcommand {
	public CmdAdminRevokeAchievement(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> achTab = new ArrayList<>();
		for (Achievement ach : AchievementManager.getAll()) {
			achTab.add(ach.getId());
		}
		ArrayList<String> classTab = new ArrayList<>();
		classTab.add("global");
		classTab.add("warrior");
		classTab.add("thief");
		classTab.add("mage");
		classTab.add("archer");
		args.add(new Arg("achievement").setTabOptions(achTab),
				new Arg("scope", false).setTabOptions(classTab),
				new Arg("player", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length < 1) {
			Util.msgRaw(s, "<red>Usage: /nradmin revokeachievement <id> [scope] [player]");
			return;
		}

		Achievement ach = AchievementManager.get(args[0]);
		if (ach == null) {
			Util.msgRaw(s, "<red>Unknown achievement: " + args[0]);
			return;
		}

		EquipmentClass ec = null;
		Player target = null;

		for (int i = 1; i < args.length; i++) {
			EquipmentClass parsed = parseClass(args[i]);
			if (parsed != null || args[i].equalsIgnoreCase("global")) {
				ec = parsed;
			} else {
				target = Bukkit.getPlayer(args[i]);
			}
		}

		if (target == null) {
			if (s instanceof Player) {
				target = (Player) s;
			} else {
				Util.msgRaw(s, "<red>You must specify a player from console.");
				return;
			}
		}

		PlayerData pdata = PlayerManager.getPlayerData(target.getUniqueId());
		if (pdata == null) {
			Util.msgRaw(s, "<red>No loaded player data for " + target.getName());
			return;
		}

		AchievementProgress progress = ec == null
				? pdata.getGlobalAchievementProgress(ach.getId())
				: pdata.getClassAchievementProgress(ach.getId(), ec);

		if (progress == null) {
			Util.msgRaw(s, "<red>Could not get progress for that achievement.");
			return;
		}

		int currentMastery = progress.getMastery();
		if (currentMastery <= 0) {
			Util.msgRaw(s, "<red>" + target.getName() + " has no mastery to revoke for " + ach.getId());
			return;
		}

		// Set progress back to the previous mastery threshold (or 0 if revoking first tier)
		int[] thresholds = ach.getMasteryThresholds();
		int targetProgress = currentMastery >= 2 ? thresholds[currentMastery - 2] : 0;
		int reduction = progress.getProgress() - targetProgress;
		progress.addProgress(-reduction);

		String scope = ec == null ? "Global" : ec.getDisplay();
		Util.msgRaw(s, "<green>Revoked 1 mastery of " + ach.getId() + " [" + scope + "] from " + target.getName()
				+ " (" + progress.getMastery() + "/" + progress.getMaxMastery() + ")");
	}

	private EquipmentClass parseClass(String arg) {
		if (arg.equalsIgnoreCase("global")) return null;
		try {
			return EquipmentClass.valueOf(arg.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
