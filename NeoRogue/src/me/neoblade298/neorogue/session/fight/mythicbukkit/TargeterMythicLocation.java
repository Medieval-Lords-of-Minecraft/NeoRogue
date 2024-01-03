package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.HashSet;


import org.bukkit.Location;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.targeters.ILocationSelector;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Plot;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class TargeterMythicLocation extends ILocationSelector {

	protected final String key;

	public TargeterMythicLocation(MythicLineConfig config) {
		super(MythicBukkit.inst().getSkillManager(), config);
		this.key = config.getString("id");
	}
	
	@Override
	public HashSet<AbstractLocation> getLocations(SkillMetadata data) {
		HashSet<AbstractLocation> locs = new HashSet<AbstractLocation>();
		try {
			Location loc = data.getCaster().getEntity().getBukkitEntity().getLocation();
			Instance temp = SessionManager.getSession(Plot.locationToPlot(loc)).getInstance();
			
			if (!(temp instanceof FightInstance)) return null;
			FightInstance inst = (FightInstance) temp;
			Location mythicLoc = inst.getMythicLocation(key);
			if (loc == null) return null;
	
	        locs.add(new AbstractLocation(data.getCaster().getLocation().getWorld(), mythicLoc.getX(), mythicLoc.getY(), mythicLoc.getZ()));
		} catch (Exception e) {
			e.printStackTrace();
		}
        return locs;
	}
}
