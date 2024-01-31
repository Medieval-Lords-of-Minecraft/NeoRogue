package me.neoblade298.neorogue.map;

import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class MapSpawnerInstance {
	private Session s;
	private MythicMob mythicMob;
	private Mob mob;
	private Location loc;
	private double radius;
	private int maxMobs, activeMobs;
	
	public MapSpawnerInstance(Session s, MapSpawner original, MapPieceInstance inst, int xOff, int zOff) {
		this.s = s;
		this.mythicMob = original.getMythicMob();
		this.mob = original.getMob();
		this.loc = original.getCoordinates().clone().applySettings(inst).toLocation();
		this.loc.add(MapPieceInstance.X_FIGHT_OFFSET + xOff,
				MapPieceInstance.Y_OFFSET,
				MapPieceInstance.Z_FIGHT_OFFSET + zOff + 0.5);
		this.loc.setX(-this.loc.getX() + 0.5);
		
		this.radius = original.getRadius();
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
		return mythicMob;
	}
	
	public void spawnMob() {
		for (int i = 0; i < mob.getAmount(); i++) {
			Location loc = this.loc;
			if (radius > 0) {
				loc = loc.clone().add(NeoRogue.gen.nextDouble(-radius, radius), 0, NeoRogue.gen.nextDouble(-radius, radius));
			}
			ActiveMob am = FightInstance.spawnScaledMob(s, loc, mythicMob);
			
			UUID uuid = am.getEntity().getUniqueId();
			FightData fd = new FightData((LivingEntity) am.getEntity().getBukkitEntity(), this);
			for (Entry<BuffType, Integer> ent : mob.getResistances().entrySet()) {
				fd.addBuff(uuid, false, true, ent.getKey(), (double) ent.getValue() / 100);
			}
			FightInstance.putFightData(uuid, fd);
			activeMobs += mob.getAmount();
		}
	}
	
	public void subtractActiveMobs() {
		activeMobs--;
	}
}
