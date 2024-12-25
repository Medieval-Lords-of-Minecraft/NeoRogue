package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.LimitedAmmunition;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;

public class ExplosiveArrow extends LimitedAmmunition {
	private static final String ID = "explosiveArrow";
	private static final TargetProperties tp = TargetProperties.radius(2, true, TargetType.ENEMY);
	
	public ExplosiveArrow(boolean isUpgraded) {
		super(ID, "Explosive Arrow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(30, 0.2, DamageType.FIRE).add(PropertyType.AREA_OF_EFFECT, tp.range), isUpgraded ? 15 : 10);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW, "Explodes on hiting an enemy or block, " +
			"dealing damage to all nearby enemies. Limited to " + DescUtil.yellow(uses) + " uses per fight.");
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity target) {
		explode(inst, target.getLocation(), target);
	}

	@Override
	public void onHitBlock(ProjectileInstance inst, Block b) {
		explode(inst, inst.getLocation(), null);
	}

	private void explode(ProjectileInstance inst, Location loc, LivingEntity hit) {
		FightData owner = inst.getOwner();
		for (LivingEntity ent : TargetHelper.getEntitiesInRadius(owner.getEntity(), loc, tp)) {
			if (ent == hit) continue;
			FightInstance.dealDamage(new DamageMeta(owner, properties.get(PropertyType.DAMAGE), properties.getType()), hit);
			FightInstance.knockback(ent,
					inst.getVelocity().setY(0).normalize().multiply(properties.get(PropertyType.KNOCKBACK)));
		}
	}
}
