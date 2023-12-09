package me.neoblade298.neorogue.map;

import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.Damageable;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class MapSpawnerInstance {
	private MythicMob mythicMob;
	private Mob mob;
	private Location loc;
	private double radius;
	private int maxMobs, activeMobs;
	
	public MapSpawnerInstance(MapSpawner original, MapPieceInstance inst, int xOff, int zOff) {
		this.mythicMob = original.getMythicMob();
		this.mob = original.getMob();
		this.loc = original.getCoordinates().clone().applySettings(inst).toLocation();
		this.loc.add(MapPieceInstance.X_FIGHT_OFFSET + xOff - 0.5,
				MapPieceInstance.Y_OFFSET,
				MapPieceInstance.Z_FIGHT_OFFSET + zOff + 0.5);
		this.loc.setX(-this.loc.getX());
		
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
	
	public void spawnMob(double lvl) {
		for (int i = 0; i < mob.getAmount(); i++) {
			Location loc = this.loc;
			if (radius > 0) {
				loc = loc.clone().add(NeoCore.gen.nextDouble(-radius, radius), 0, NeoCore.gen.nextDouble(-radius, radius));
			}
			ActiveMob am = mythicMob.spawn(BukkitAdapter.adapt(loc), lvl);
			double mhealth = am.getEntity().getMaxHealth();
			mhealth *= lvl / 5;
			am.getEntity().setMaxHealth(mhealth);
			am.getEntity().setHealth(mhealth);
			
			UUID uuid = am.getEntity().getUniqueId();
			FightData fd = new FightData((Damageable) am.getEntity().getBukkitEntity(), this);
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
