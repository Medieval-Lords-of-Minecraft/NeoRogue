package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EarthStaff extends Equipment {
	private static final String ID = "earthStaff";
	
	private static final double innerRadius = 1.5, outerRadius = 4;
	private static final Circle innerRing = new Circle(innerRadius), outerRing = new Circle(outerRadius);
	private static final ParticleContainer aoe = new ParticleContainer(Particle.GLOW).count(1).spread(0.1, 0.1).speed(0);
	
	private static final TargetProperties innerProps = TargetProperties.radius(innerRadius, true, TargetType.ENEMY);
	private static final TargetProperties outerProps = TargetProperties.radius(outerRadius, true, TargetType.ENEMY);
	private int conc = 15;
	
	public EarthStaff(boolean isUpgraded) {
		super(
				ID, "Earth Staff", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(10, 0, isUpgraded ? 140 : 100, 0.5, DamageType.EARTHEN, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			if (!p.isOnGround()) {
				Util.displayError(p, "Can't use in the air!");
			}
			weaponSwing(p, data);
			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					Sounds.explode.play(p, p);
					innerRing.play(aoe, p.getLocation(), LocalAxes.xz(), aoe);
					LinkedList<LivingEntity> closeEnemies = TargetHelper.getEntitiesInRadius(p, innerProps);
					for (LivingEntity ent : closeEnemies) {
						weaponDamage(p, data, ent);
						FightInstance.getFightData(ent.getUniqueId()).applyStatus(StatusType.CONCUSSED, data, 3, 0);
					}

					data.addTask(new BukkitRunnable() {
						@Override
						public void run() {
							Sounds.explode.play(p, p);
							outerRing.play(aoe, p.getLocation(), LocalAxes.xz(), aoe);
							LinkedList<LivingEntity> farEnemies = TargetHelper.getEntitiesInRadius(p, outerProps);
							farEnemies.removeAll(closeEnemies);
							for (LivingEntity ent : farEnemies) {
								weaponDamage(p, data, ent);
								FightInstance.getFightData(ent.getUniqueId()).applyStatus(StatusType.CONCUSSED, data, conc, 0);
							}
						}
					}.runTaskLater(NeoRogue.inst(), 5));
				}
			}.runTaskLater(NeoRogue.inst(), 12));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.STICK,
				"After a windup, smashes the ground beneath you, dealing damage in an area spreading outwards. Enemies closest to you receive "
						+ GlossaryTag.CONCUSSED.tag(this, conc, true) + ". Must be cast while on the ground."
		);
	}
}
