package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.Collection;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.targeters.IEntitySelector;
import me.neoblade298.neorogue.session.Plot;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class TargeterSession extends IEntitySelector {

	protected final String key;

	public TargeterSession(MythicLineConfig config) {
		super(MythicBukkit.inst().getSkillManager(), config);
		this.key = config.getString("id");
	}

	@Override
	public Collection<AbstractEntity> getEntities(SkillMetadata data) {
		LinkedList<AbstractEntity> targets = new LinkedList<AbstractEntity>();
		try {
			Location loc = data.getCaster().getEntity().getBukkitEntity().getLocation();
			Session sess = SessionManager.getSession(Plot.locationToPlot(loc));
			for (Player p : sess.getOnlinePlayers()) {
				targets.add(BukkitAdapter.adapt(p));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        return targets;
	}
}
