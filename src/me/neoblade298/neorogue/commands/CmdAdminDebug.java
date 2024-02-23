package me.neoblade298.neorogue.commands;

import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;

public class CmdAdminDebug extends Subcommand {
	HashMap<String, HashMap<String, Integer>> results = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> failedResults = new HashMap<String, HashMap<String, Integer>>();
	HashSet<String> resultKeys = new HashSet<String>();

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("damage", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = Bukkit.getPlayer("Ascheladd");
		p.setVelocity(p.getLocation().getDirection().add(new Vector(0, 0.15, 0)).multiply(2));
		World w = p.getWorld();
		
		new BukkitRunnable() {
			int count = 0;
			public void run() {
				if (++count >= 10) {
					this.cancel();
				}
				if (p.getVelocity().lengthSquared() == 0) {
					this.cancel();
					return;
				}
				RayTraceResult rtr = w.rayTrace(p.getLocation(), p.getVelocity(), 2, FluidCollisionMode.NEVER, true, 0.5, null);
				RayTraceResult rtr2 = w.rayTrace(p.getEyeLocation(), p.getVelocity(), 2, FluidCollisionMode.NEVER, true, 0.5, null);
				if (rtr.getHitBlock() != null && rtr.getHitBlockFace() != BlockFace.UP) {
					p.setVelocity(rtr.getHitBlockFace().getDirection());
					this.cancel();
				}
				if (rtr2.getHitBlock() != null && rtr2.getHitBlockFace() != BlockFace.UP) {
					p.setVelocity(rtr.getHitBlockFace().getDirection());
					this.cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 1L, 1L);
	}
}
