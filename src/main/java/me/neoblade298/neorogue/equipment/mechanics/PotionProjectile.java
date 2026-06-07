package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.equipment.mechanics.PotionProjectileInstance.PotionCallback;
import me.neoblade298.neorogue.session.fight.FightData;

public class PotionProjectile {
	protected PotionCallback callback;
	protected Color color;
	public PotionProjectile(Color color, PotionCallback callback) {
		this.color = color;
		this.callback = callback;
	}
	
	public PotionProjectile(PotionCallback callback) {
		this(Color.GREEN, callback);
	}
	
	public PotionProjectileInstance launch(FightData owner) {
		LivingEntity ent = owner.getEntity();
		Player p = (Player) ent;
		Location source = p.getLocation().add(0, p.isSneaking() ? 1.0 : 1.4, 0);
		Vector direction = p.getEyeLocation().getDirection();
		ThrownPotion thrown = (ThrownPotion) ent.getWorld().spawnEntity(source, EntityType.SPLASH_POTION);
		thrown.setVelocity(direction);
		return new PotionProjectileInstance(this, thrown, source);
	}
}
