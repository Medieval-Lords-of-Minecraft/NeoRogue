package me.neoblade298.neorogue.map;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Mob;

public class MapSpawnerInstance {
	private Session s;
	private MapSpawner origin;
	private Location loc;
	private int maxMobs, activeMobs;
	
	public MapSpawnerInstance(Session s, MapSpawner original, MapPieceInstance inst, int xOff, int zOff) {
		this.s = s;
		this.origin = original;
		this.loc = original.getCoordinates().clone().applySettings(inst).toLocation();
		this.loc.add(
				MapPieceInstance.X_FIGHT_OFFSET + xOff, MapPieceInstance.Y_OFFSET,
				MapPieceInstance.Z_FIGHT_OFFSET + zOff + 0.5
		);
		this.loc.setX(-this.loc.getX() + 0.5);
		this.maxMobs = original.getMaxMobs();
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public boolean canSpawn() {
		return maxMobs == -1 || activeMobs < maxMobs;
	}
	
	public int getMaxMobs() {
		return maxMobs;
	}
	
	public MythicMob getMythicMob() {
		return origin.getMythicMob();
	}
	
	public Mob getMob() {
		return origin.getMob();
	}
	
	public void spawnMob() {
		for (int i = 0; i < origin.getMob().getAmount(); i++) {
			Location loc = this.loc;
			if (origin.getRadius() > 0) {
				double radius = origin.getRadius();
				loc = loc.clone()
						.add(NeoRogue.gen.nextDouble(-radius, radius), 0, NeoRogue.gen.nextDouble(-radius, radius));
			}
			ActiveMob am = FightInstance.spawnScaledMob(s, loc, origin.getMythicMob());
			
			UUID uuid = am.getEntity().getUniqueId();
			FightData fd = new FightData((LivingEntity) am.getEntity().getBukkitEntity(), am, origin.getMob(), this);
			if (fd.getEntity() == null) continue; // Sometimes happens when a fight ends (despawn triggers more spawnmob)
			fd.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0));
			FightInstance.putFightData(uuid, fd);
			activeMobs += origin.getMob().getAmount();
		}
	}
	
	public void subtractActiveMobs() {
		activeMobs--;
	}
}
