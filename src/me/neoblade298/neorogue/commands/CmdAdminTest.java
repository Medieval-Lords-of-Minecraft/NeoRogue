package me.neoblade298.neorogue.commands;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;

public class CmdAdminTest extends Subcommand {
	// Rose components
	private static final ParticleContainer stem = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.fromRGB(34, 139, 34), 1F))  // Forest green
		.count(1).spread(0, 0);
	
	private static final ParticleContainer leaf = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.fromRGB(50, 205, 50), 1F))  // Lime green
		.count(1).spread(0, 0);
	
	private static final ParticleContainer petal = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.fromRGB(220, 20, 60), 1.2F))  // Crimson red
		.count(1).spread(0, 0);
	
	private static final ParticleContainer innerPetal = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.fromRGB(255, 105, 180), 1F))  // Hot pink (inner petals)
		.count(1).spread(0, 0);
	
	private static final ParticleAnimation stemAnim, leafAnim, petalAnim, innerPetalAnim;
	
	static {
		// Stem animation (green)
		stemAnim = new ParticleAnimation(stem, (loc, tick) -> {
			LinkedList<Location> partLocs = new LinkedList<Location>();
			double progress = tick / 40.0;
			
			// Stem grows upward (0-2 blocks)
			if (progress <= 0.3) {
				double stemProgress = progress / 0.3;
				int stemPoints = (int)(stemProgress * 20);
				for (int i = 0; i <= stemPoints; i++) {
					double y = (i / 20.0) * 2;
					partLocs.add(loc.clone().add(0, y, 0));
				}
			} else {
				// Full stem
				for (int i = 0; i <= 20; i++) {
					double y = (i / 20.0) * 2;
					partLocs.add(loc.clone().add(0, y, 0));
				}
			}
			return partLocs;
		}, 40);
		
		// Leaf animation (lime green)
		leafAnim = new ParticleAnimation(leaf, (loc, tick) -> {
			LinkedList<Location> partLocs = new LinkedList<Location>();
			double progress = tick / 40.0;
			
			// Leaves appear at 30% and 50% up the stem
			if (progress > 0.35) {
				double leafSize = Math.min(1.0, (progress - 0.35) / 0.15);
				
				// Lower left leaf
				for (int i = 0; i <= 8; i++) {
					double t = (i / 8.0) * leafSize;
					double x = -0.3 * t;
					double y = 0.6 + 0.2 * t;
					double z = -0.15 * t;
					partLocs.add(loc.clone().add(x, y, z));
				}
				
				// Lower right leaf
				for (int i = 0; i <= 8; i++) {
					double t = (i / 8.0) * leafSize;
					double x = 0.3 * t;
					double y = 0.6 + 0.15 * t;
					double z = 0.15 * t;
					partLocs.add(loc.clone().add(x, y, z));
				}
			}
			
			if (progress > 0.5) {
				double leafSize = Math.min(1.0, (progress - 0.5) / 0.15);
				
				// Upper left leaf
				for (int i = 0; i <= 8; i++) {
					double t = (i / 8.0) * leafSize;
					double x = -0.25 * t;
					double y = 1.2 + 0.15 * t;
					double z = 0.1 * t;
					partLocs.add(loc.clone().add(x, y, z));
				}
				
				// Upper right leaf
				for (int i = 0; i <= 8; i++) {
					double t = (i / 8.0) * leafSize;
					double x = 0.25 * t;
					double y = 1.2 + 0.18 * t;
					double z = -0.1 * t;
					partLocs.add(loc.clone().add(x, y, z));
				}
			}
			return partLocs;
		}, 40);
		
		// Outer/middle petal animation (crimson red)
		petalAnim = new ParticleAnimation(petal, (loc, tick) -> {
			LinkedList<Location> partLocs = new LinkedList<Location>();
			double progress = tick / 40.0;
			
			// Rose bloom starts forming at top (after 65%)
			if (progress > 0.65) {
				Location bloomCenter = loc.clone().add(0, 2, 0);
				double bloomProgress = (progress - 0.65) / 0.35;
				
				// Outer petals (5 petals)
				if (bloomProgress > 0.2) {
					double petalOpenness = Math.min(1.0, (bloomProgress - 0.2) / 0.3);
					for (int petalNum = 0; petalNum < 5; petalNum++) {
						double angle = (petalNum / 5.0) * Math.PI * 2;
						
						// Each petal is a curve
						for (int i = 0; i <= 10; i++) {
							double t = i / 10.0;
							// Petal curves outward and slightly down
							double radius = 0.4 * petalOpenness * (0.3 + 0.7 * t);
							double height = -0.1 * t * petalOpenness;
							double petalAngle = angle + (t - 0.5) * 0.8;  // Petal spreads
							
							double x = Math.cos(petalAngle) * radius;
							double z = Math.sin(petalAngle) * radius;
							partLocs.add(bloomCenter.clone().add(x, height, z));
						}
					}
				}
				
				// Middle layer petals (5 petals, offset)
				if (bloomProgress > 0.4) {
					double petalOpenness = Math.min(1.0, (bloomProgress - 0.4) / 0.25);
					for (int petalNum = 0; petalNum < 5; petalNum++) {
						double angle = (petalNum / 5.0) * Math.PI * 2 + 0.314;  // Offset by 36 degrees
						
						for (int i = 0; i <= 8; i++) {
							double t = i / 8.0;
							double radius = 0.3 * petalOpenness * (0.3 + 0.7 * t);
							double height = 0.05 - 0.08 * t * petalOpenness;
							double petalAngle = angle + (t - 0.5) * 0.6;
							
							double x = Math.cos(petalAngle) * radius;
							double z = Math.sin(petalAngle) * radius;
							partLocs.add(bloomCenter.clone().add(x, height, z));
						}
					}
				}
			}
			return partLocs;
		}, 40);
		
		// Inner petal animation (hot pink)
		innerPetalAnim = new ParticleAnimation(innerPetal, (loc, tick) -> {
			LinkedList<Location> partLocs = new LinkedList<Location>();
			double progress = tick / 40.0;
			
			if (progress > 0.65) {
				Location bloomCenter = loc.clone().add(0, 2, 0);
				double bloomProgress = (progress - 0.65) / 0.35;
				
				// Inner tight petals (spiral formation)
				if (bloomProgress > 0.6) {
					double innerProgress = Math.min(1.0, (bloomProgress - 0.6) / 0.2);
					int innerPetals = (int)(innerProgress * 8);
					for (int i = 0; i < innerPetals; i++) {
						double spiralAngle = (i / 8.0) * Math.PI * 4;  // 2 rotations
						double spiralRadius = 0.15 * (1 - i / 8.0);
						double spiralHeight = 0.1 + 0.05 * (i / 8.0);
						
						double x = Math.cos(spiralAngle) * spiralRadius;
						double z = Math.sin(spiralAngle) * spiralRadius;
						partLocs.add(bloomCenter.clone().add(x, spiralHeight, z));
						
						// Add a few points around each spiral point
						for (int j = 0; j < 3; j++) {
							double miniAngle = spiralAngle + (j / 3.0) * 0.5;
							double miniRadius = spiralRadius * 0.7;
							double mx = Math.cos(miniAngle) * miniRadius;
							double mz = Math.sin(miniAngle) * miniRadius;
							partLocs.add(bloomCenter.clone().add(mx, spiralHeight, mz));
						}
					}
				}
			}
			return partLocs;
		}, 40);
	}

	public CmdAdminTest(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Location spawnLoc = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(3));
		
		// Play all animations simultaneously at the same location
		stemAnim.play(p, spawnLoc);
		leafAnim.play(p, spawnLoc);
		petalAnim.play(p, spawnLoc);
		innerPetalAnim.play(p, spawnLoc);
	}
}
