package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class FrozenDomain extends Bow {
	private static final String ID = "FrozenDomain";
	private static final int WEAPON_DAMAGE = 80, RADIUS = 4;
	private static final TargetProperties TARGETS = TargetProperties.radius(RADIUS, false, TargetType.ENEMY);
	private static final Circle IMPACT_AREA = new Circle(RADIUS);
	private static final ParticleContainer IMPACT_EDGE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(130, 220, 255), 1F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer IMPACT_FILL = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.ICE.createBlockData()).count(1).spread(0.05, 0).speed(0);
	private int iceDamage, frost;

	public FrozenDomain(boolean isUpgraded) {
		super(ID, "Frozen Domain", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.WEAPON,
				EquipmentProperties.ofBow(WEAPON_DAMAGE, 1, 0, 12, 0, 0));
		iceDamage = isUpgraded ? 60 : 40;
		frost = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onTick(Player player, ProjectileInstance projectile, int interpolation) {
		BowProjectile.tick.play(player, projectile.getLocation());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShootCrossbow(data)) return TriggerResult.keep();
			useBow(data);
			ProjectileLaunchEvent event = (ProjectileLaunchEvent) in;
			new ProjectileGroup(new BowProjectile(data, event.getEntity().getVelocity(), this, id + slot)).start(data);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent event = (PreDealDamageEvent) in;
			if (!event.getMeta().isBasicAttack() || event.getMeta().getWeapon() != this) return TriggerResult.keep();

			Player player = data.getPlayer();
			IMPACT_AREA.play(IMPACT_EDGE, event.getTarget().getLocation(), LocalAxes.xz(), IMPACT_FILL);
			Sounds.glass.play(player, event.getTarget().getLocation());
			for (LivingEntity target : TargetHelper.getEntitiesInRadius(player, event.getTarget().getLocation(), TARGETS)) {
				FightInstance.dealDamage(new DamageMeta(data, iceDamage, DamageType.ICE,
						DamageStatTracker.of(id + slot, this)), target);
				FightInstance.applyStatus(target, StatusType.FROST, data, frost, -1, this);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CROSSBOW, "Basic attacks also deal " + GlossaryTag.ICE.tag(this, iceDamage)
				+ " damage and apply " + GlossaryTag.FROST.tag(this, frost) + " to enemies within "
				+ me.neoblade298.neorogue.DescUtil.val(RADIUS) + " blocks of the enemy hit.");
	}
}