package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.BasicElementMastery;
import me.neoblade298.neorogue.equipment.abilities.Sear;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageType;

public class Firefly extends Ammunition {
	private static final String ID = "Firefly";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	
	public Firefly(boolean isUpgraded) {
		super(ID, "Firefly", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(isUpgraded ? 15 : 10, 0.1, DamageType.FIRE));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void setupReforges() {
		addReforge(Sear.get(), SearingArrow.get());
		addReforge(BasicElementMastery.get(), StickyBomb.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void onStart(ProjectileInstance inst) {
		inst.getVelocity().add(new Vector(0, 0.08, 0));
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		if (interpolation % 2 == 0) return;
		pc.play(p, proj.getLocation());
		proj.getVelocity().add(new Vector(0, -0.002, 0));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TIPPED_ARROW, "Launches in an arc.");
		PotionMeta pm = (PotionMeta) item.getItemMeta();
		pm.setColor(Color.RED);
		item.setItemMeta(pm);
	}
}
