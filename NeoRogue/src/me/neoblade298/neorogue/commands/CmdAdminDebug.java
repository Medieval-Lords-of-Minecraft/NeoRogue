package me.neoblade298.neorogue.commands;

import java.util.LinkedList;

import org.bukkit.command.CommandSender;
import org.enginehub.piston.part.CommandArgument;

import me.neoblade298.neocore.bukkit.bungee.BungeeAPI;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.CommandArguments;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.map.MapPieceInstance;

public class CmdAdminDebug extends Subcommand {

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("Piece", false), new Arg("FlipX", false), new Arg("FlipY", false), new Arg("Rotations", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Map.load();
		LinkedList<MapPiece> pieces = Map.getPieces(AreaType.LOW_DISTRICT);
		MapPieceInstance inst = pieces.get(Integer.parseInt(args[0])).getInstance();
		inst.setRotations(Integer.parseInt(args[3]));
		if (Boolean.parseBoolean(args[1])) inst.flip(true);
		if (Boolean.parseBoolean(args[2])) inst.flip(false);
		inst.instantiate(null, 16, 0);
	}
}
