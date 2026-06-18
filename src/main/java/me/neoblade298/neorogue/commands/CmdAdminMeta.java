package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminMeta extends Subcommand {
	public CmdAdminMeta(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		args.add(new Arg("key").setTabOptions(new ArrayList<>(List.of("dur"))), new Arg("value"));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);
		if (sess == null) {
			Util.displayError(p, "You're not currently in a session!");
			return;
		}

		PlayerSessionData data = sess.getParty().get(p.getUniqueId());
		if (data == null) {
			Util.displayError(p, "Could not find your session data!");
			return;
		}

		// Try held item first, fall back to first storage slot
		SessionEquipment se = null;
		ItemStack held = p.getInventory().getItemInMainHand();
		if (!held.getType().isAir()) {
			NBTItem nbti = new NBTItem(held);
			if (nbti.hasTag("equipId")) {
				int heldSlot = p.getInventory().getHeldItemSlot();
				se = data.getSessionEquipment(EquipSlot.HOTBAR)[heldSlot];
			}
		}
		if (se == null) {
			SessionEquipment[] storage = data.getStorage();
			if (storage[0] != null) {
				se = storage[0];
			} else {
				Util.displayError(p, "You're not holding equipment and storage is empty!");
				return;
			}
		}

		String key = args[0];
		String rawValue = args[1];

		if (rawValue.equals("remove")) {
			se.remove(key);
			Util.msg(p, "Removed metadata key <yellow>" + key + "</yellow>.");
		} else if (rawValue.startsWith("d:")) {
			double val = Double.parseDouble(rawValue.substring(2));
			se.setDouble(key, val);
			Util.msg(p, "Set <yellow>" + key + "</yellow> to <yellow>" + val + "</yellow> (double).");
		} else if (rawValue.startsWith("s:")) {
			String val = rawValue.substring(2);
			se.setString(key, val);
			Util.msg(p, "Set <yellow>" + key + "</yellow> to <yellow>" + val + "</yellow> (string).");
		} else {
			String numStr = rawValue.startsWith("i:") ? rawValue.substring(2) : rawValue;
			int val = Integer.parseInt(numStr);
			se.setInt(key, val);
			Util.msg(p, "Set <yellow>" + key + "</yellow> to <yellow>" + val + "</yellow> (int).");
		}

		PlayerSessionInventory.setupInventory(p.getInventory(), data);
	}
}
