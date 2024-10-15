package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.entity.Player;

public interface ProjectileTickAction {
	public void onTick(Player p, ProjectileInstance inst, int interpolation);
}
