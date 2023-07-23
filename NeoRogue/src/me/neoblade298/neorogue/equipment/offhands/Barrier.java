package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import me.neoblade298.neocore.bukkit.particles.Rectangle;

public class Barrier {
	private Player p;
	private double height, width, distanceFromPlayer;
	private BoundingBox bounds;
	
	public Barrier(Player p, double height, double width, double distanceFromPlayer) {
		this.p = p;
		this.height = height;
		this.width = width;
		this.distanceFromPlayer = distanceFromPlayer;
	}
	
	public void tick() {
		new Rectangle(p, width, height, distanceFromPlayer).draw(p, false, Particle.END_ROD, null);
	}
}
