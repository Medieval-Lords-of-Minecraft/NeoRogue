package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.LimitedAmmunition;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class ExplosiveArrow2 extends LimitedAmmunition {
	private static final String ID = "ExplosiveArrow2";
	private static final int BASE_DAMAGE = 35;
	private static final int UPGRADED_DAMAGE = 40;
	private static final int USES = 15;
	private static final double KNOCKBACK = 0.2;
	private static final TargetProperties tp = TargetProperties.radius(3, true, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION);
	private int burn;

	public ExplosiveArrow2(boolean isUpgraded) {
		super(ID, "Explosive Arrow II", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(isUpgraded ? UPGRADED_DAMAGE : BASE_DAMAGE, KNOCKBACK, DamageType.FIRE)
						.add(PropertyType.AREA_OF_EFFECT, tp.range), USES);
		burn = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW, "Explodes on hitting an enemy or block, dealing damage and applying "
				+ GlossaryTag.BURN.tag(this, burn) + " to all nearby enemies. Limited to " + DescUtil.val(uses)
				+ " uses per fight.");
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity target) {
		FightInstance.applyStatus(target, StatusType.BURN, inst.getOwner(), burn, -1, this);
		explode(inst, target.getLocation(), target);
	}

	@Override
	public void onHitBlock(ProjectileInstance inst, Block block) {
		explode(inst, inst.getLocation(), null);
	}

	private void explode(ProjectileInstance inst, Location location, LivingEntity hit) {
		FightData owner = inst.getOwner();
		Player player = (Player) owner.getEntity();
		Sounds.explode.play(player, location);
		pc.play(player, location);
		for (LivingEntity target : TargetHelper.getEntitiesInRadius(owner.getEntity(), location, tp)) {
			if (target == hit) continue;
			FightInstance.dealDamage(new DamageMeta(owner, properties.get(PropertyType.DAMAGE), properties.getType(),
					DamageStatTracker.of(id, this)), target);
			FightInstance.applyStatus(target, StatusType.BURN, owner, burn, -1, this);
			FightInstance.knockback(target,
					inst.getVelocity().setY(0).normalize().multiply(properties.get(PropertyType.KNOCKBACK)));
		}
	}
}