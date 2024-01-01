package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.MinibossFightInstance;

public class CmdAdminTestMiniboss extends Subcommand {

	public CmdAdminTestMiniboss(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		args.add(new Arg("miniboss map ID").setTabOptions(new ArrayList<String>(Map.getAllPieces().keySet())));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player host = (Player) s;
		Session sess = SessionManager.createSession(host, "test", 1);
		for (Player p : Bukkit.getOnlinePlayers()) {
			SessionManager.addToSession(p.getUniqueId(), sess);
			sess.addPlayer(p.getUniqueId(), EquipmentClass.WARRIOR);
		}
		sess.generateArea(AreaType.LOW_DISTRICT);
		sess.setNode(sess.getArea().getNodes()[0][2]);
		MinibossFightInstance inst = new MinibossFightInstance(sess.getParty().keySet(), Map.generate(AreaType.LOW_DISTRICT, 0, MapPiece.get(args[0])));
		sess.setInstance(inst);
	}
}
