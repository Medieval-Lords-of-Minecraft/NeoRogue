package me.neoblade298.neorogue.equipment.mechanics;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionType;

import me.neoblade298.neorogue.NeoRogue;

public class PotionProjectileInstance extends IProjectileInstance {
	private static final HashMap<UUID, PotionProjectileInstance> insts = new HashMap<UUID, PotionProjectileInstance>();
	
	private PotionProjectile potion;
	private UUID uuid;
	
	public PotionProjectileInstance(PotionProjectile potion, ThrownPotion thrown, Location origin) {
		super(origin);
		uuid = UUID.randomUUID();
		insts.put(uuid, this);
		this.potion = potion;

        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta pm = (PotionMeta) item.getItemMeta();
        pm.setBasePotionType(PotionType.AWKWARD);
        pm.setColor(potion.color);
        item.setItemMeta(pm);
        thrown.setItem(item);
        thrown.setMetadata("uuid", new FixedMetadataValue(NeoRogue.inst(), uuid));
	}

	public IProjectile getParent() {
		return this.potion;
	}
	
	public static PotionProjectileInstance get(UUID uuid) {
		return insts.get(uuid);
	}
	
	public static void remove(UUID uuid) {
		insts.remove(uuid);
	}
	
	public void callback(Location loc, Collection<LivingEntity> collection) {
		potion.callback.run(loc, collection);
	}
	
	public interface PotionCallback {
		public void run(Location loc, Collection<LivingEntity> hit);
	}
}
