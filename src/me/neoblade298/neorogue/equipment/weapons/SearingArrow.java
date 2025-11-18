package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class SearingArrow extends Ammunition {
	private static final String ID = "SearingArrow";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private int burn;
	
	public SearingArrow(boolean isUpgraded) {
		super(ID, "Searing Arrow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(20, 0.1, DamageType.FIRE));
				burn = isUpgraded ? 35 : 25;

	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity target) {
		FightInstance.applyStatus(target, StatusType.BURN, inst.getOwner(), burn, -1);
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
		item = createItem(Material.TIPPED_ARROW, "Launches in an arc. Applies " + GlossaryTag.BURN.tag(this, burn, true) + " on hit.");
		PotionMeta pm = (PotionMeta) item.getItemMeta();
		pm.setColor(Color.RED);
		item.setItemMeta(pm);
	}
}
