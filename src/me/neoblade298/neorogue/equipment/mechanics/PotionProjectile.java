package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.equipment.mechanics.PotionProjectileInstance.PotionCallback;
import me.neoblade298.neorogue.session.fight.FightData;

public class PotionProjectile extends IProjectile {
	protected PotionCallback callback;
	protected Color color;
	public PotionProjectile(Color color, PotionCallback callback) {
		this.color = color;
		this.callback = callback;
	}
	
	public PotionProjectile(PotionCallback callback) {
		this(Color.GREEN, callback);
	}
	
	@Override
	protected PotionProjectileInstance start(FightData owner, Location source, Vector direction) {
		LivingEntity ent = owner.getEntity();
		ThrownPotion thrown = (ThrownPotion) ent.getWorld().spawnEntity(ent.getLocation().add(0, 1.25, 0), EntityType.SPLASH_POTION);
		thrown.setVelocity(owner.getEntity().getEyeLocation().getDirection());
		return new PotionProjectileInstance(this, thrown);
	}
}
