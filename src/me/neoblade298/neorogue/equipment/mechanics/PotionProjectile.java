package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.Location;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.equipment.mechanics.PotionProjectileInstance.PotionCallback;
import me.neoblade298.neorogue.session.fight.FightData;

public class PotionProjectile extends IProjectile {
	protected PotionCallback callback;
	protected PotionType type;
	public PotionProjectile(PotionType type, PotionCallback callback) {
		this.type = type;
		this.callback = callback;
	}
	
	public PotionProjectile(PotionCallback callback) {
		this(PotionType.SLOWNESS, callback);
	}
	
	@Override
	protected PotionProjectileInstance start(FightData owner, Location source, Vector direction) {
		ThrownPotion thrown = owner.getEntity().launchProjectile(ThrownPotion.class);
		return new PotionProjectileInstance(this, thrown);
	}
}
