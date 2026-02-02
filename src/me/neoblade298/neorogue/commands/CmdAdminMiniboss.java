package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.chance.builtin.TestChance;
import me.neoblade298.neorogue.session.fight.MinibossFightInstance;

public class CmdAdminMiniboss extends Subcommand {
	private ArrayList<String> minibossSet = new ArrayList<String>();

	public CmdAdminMiniboss(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		for (ArrayList<MapPiece> pieces : Map.getMinibossPieces().values()) {
			for (MapPiece piece : pieces) {
				minibossSet.add(piece.getId());
			}
		}
		this.enableTabComplete();
		args.add(new Arg("miniboss map ID").setTabOptions(minibossSet),
				new Arg("level", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player host = (Player) s;
		Session sess = SessionManager.createSession(host, "test", 1);
		for (Player p : Bukkit.getOnlinePlayers()) {
			SessionManager.addToSession(p.getUniqueId(), sess);
			sess.addPlayer(p.getUniqueId(), EquipmentClass.WARRIOR);
			p.setHealth(p.getAttribute(Attribute.MAX_HEALTH).getValue());
		}
		sess.generateRegion(RegionType.LOW_DISTRICT);
		sess.setNode(sess.getRegion().getNodes()[0][2]);
		if (args.length > 1) {
			sess.setNodesVisited(Integer.parseInt(args[1]));
		}
		MinibossFightInstance inst = new MinibossFightInstance(sess, sess.getParty().keySet(), Map.generate(RegionType.LOW_DISTRICT, 0, MapPiece.get(args[0])));
		sess.setInstance(new ChanceInstance(sess, new TestChance(inst)));
	}
}
