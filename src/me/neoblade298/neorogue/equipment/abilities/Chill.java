package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class Chill extends Equipment {
	private static final String ID = "chill";
	private static final ParticleContainer pc = new ParticleContainer(Particle.SNOW_SHOVEL).count(80).spread(2.5, 0.5).speed(0.2);
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private int frost;
	
	public Chill(boolean isUpgraded) {
		super(ID, "Chill", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 12, 0));
		frost = isUpgraded ? 40 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.equip.play(p, p);
			addChill(p, data);
			return TriggerResult.keep();
		}));
	}

	private void addChill(Player p, PlayerFightData data) {
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBowProjectile()) return TriggerResult.keep();
			ProjectileInstance proj = (ProjectileInstance) ev.getInstances().getFirst();
			proj.addHitBlockAction((proj2, block) -> {
				chillHit(p, data, block.getLocation().add(0, 1, 0));
			});
			proj.addHitAction((hit, hitBarrier, proj2) -> {
				chillHit(p, data, hit.getEntity().getLocation().add(0, 1, 0));
			});
			return TriggerResult.remove();
		});
	}

	private void chillHit(Player p, PlayerFightData data, Location loc) {
		pc.play(p, loc);
		Sounds.ice.play(p, loc);
		for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
			FightInstance.applyStatus(ent, StatusType.FROST, data, frost, -1);
			ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CYAN_DYE,
				"On cast, Your next basic attack projectile will apply " + GlossaryTag.FROST.tag(this, frost, true) + " and " +
				DescUtil.potion("Slowness", 1, 3) + " to all enemies in a radius of <white>5</white> around the target or block hit.");
	}
}
