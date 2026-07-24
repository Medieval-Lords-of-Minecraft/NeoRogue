package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

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
import me.neoblade298.neorogue.session.fight.BossFightInstance;
import me.neoblade298.neorogue.session.fight.MobModifier;

public class CmdAdminBoss extends Subcommand {
	private ArrayList<String> bossSet = new ArrayList<String>();
	private static final ArrayList<String> notorietyTab = new ArrayList<String>();
	static {
		for (int i = 0; i <= 10; i++) notorietyTab.add(String.valueOf(i));
	}

	public CmdAdminBoss(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		for (ArrayList<MapPiece> pieces : Map.getBossPieces().values()) {
			for (MapPiece piece : pieces) {
				bossSet.add(piece.getId());
			}
		}
		this.enableTabComplete();
		args.add(new Arg("boss map ID").setTabOptions(bossSet),
				new Arg("level", false),
				new Arg("notoriety (0-10)", false).setTabOptions(notorietyTab),
				new Arg("mob modifier", false).setTabOptions(MobModifier.getIds()));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player host = (Player) s;
		Session sess = SessionManager.createSession(host, 9);
		sess.addPlayer(host.getUniqueId(), EquipmentClass.WARRIOR);

		sess.generateRegion(RegionType.LOW_DISTRICT);
		sess.setNode(sess.getRegion().getNodes()[0][2]);
		if (args.length > 1) {
			sess.setNodesVisited(Integer.parseInt(args[1]));
		}
		if (args.length > 2) {
			sess.setNotoriety(Integer.parseInt(args[2]));
		}
		BossFightInstance inst = new BossFightInstance(sess, sess.getParty().keySet(), Map.generate(RegionType.LOW_DISTRICT, 0, MapPiece.get(args[0]), true));
		if (args.length > 3) {
			MobModifier mod = MobModifier.get(args[3]);
			if (mod != null) inst.setModifier(mod);
		}
		ChanceInstance ci = new ChanceInstance(sess, new TestChance(inst));
		ci.setNextInstance(inst);
		sess.setInstance(ci);
	}
}
