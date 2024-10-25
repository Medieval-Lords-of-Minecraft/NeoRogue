package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;

public class CmdAdminDebug extends Subcommand {

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}
	
	public void run(CommandSender s, String[] args) {
		Player p = Bukkit.getPlayer("Ascheladd");
		Cone cone = new Cone(5, 45);
		cone.play(p, new ParticleContainer(Particle.FLAME), p.getLocation(), LocalAxes.usingGroundedEyeLocation(p), new ParticleContainer(Particle.REDSTONE));
	}
}
