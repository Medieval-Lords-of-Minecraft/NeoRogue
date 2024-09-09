package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryInventory;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class CmdGlossary extends Subcommand {

	public CmdGlossary(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		ArrayList<String> tab = new ArrayList<String>(Equipment.getEquipmentIds());
		for (GlossaryTag tag : GlossaryTag.values()) {
			tab.add(tag.getId());
		}
		args.add(new Arg("id", false).setTabOptions(tab));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		
		if (args.length == 0) {
			Util.displayError(p, "This command doesn't do anything yet without arguments!");
			return;
		}
		Equipment eq = Equipment.get(args[0], false);
		if (eq != null) {
			new GlossaryInventory(p, eq, null);
			return;
		}
		
		try {
			GlossaryTag tag = GlossaryTag.valueOf(args[0]);
			new GlossaryInventory(p, tag, null);
			return;
		}
		catch (IllegalArgumentException ex) {
			
		}
		
		Util.displayError(p, "Couldn't find an equipment or glossary tag with that name!");
	}
}
