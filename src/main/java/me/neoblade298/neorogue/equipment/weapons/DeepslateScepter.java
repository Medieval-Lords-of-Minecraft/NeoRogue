package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DeepslateScepter extends Equipment {
	private static final String ID = "DeepslateScepter";
	private static final int HIT_SCAN_RANGE = 12;
	private static final ParticleContainer TICK = new ParticleContainer(Particle.SMOKE)
			.count(25).spread(0.1, 0.1).speed(0.01);

	private final int damageMultiplier;
	private final int concussed;

	public DeepslateScepter(boolean isUpgraded) {
		super(ID, "Deepslate Scepter", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(3, 0, isUpgraded ? 100 : 80, 0.5, DamageType.DARK,
						Sound.ITEM_AXE_SCRAPE));
		damageMultiplier = 2;
		concussed = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ProjectileGroup projectileGroup = new ProjectileGroup(new DeepslateRay(data, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			Player player = data.getPlayer();
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(player, data);

			RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(),
					player.getEyeLocation().getDirection(), HIT_SCAN_RANGE, FluidCollisionMode.NEVER, false);
			if (result != null) {
				Block target = result.getHitBlock();
				boolean empowered = target.getType() == Material.DEEPSLATE;
				Location spawnLocation;
				Vector spawnVector;
				if (target.isPassable()) {
					spawnLocation = target.getLocation().add(0.5, 0.5, 0.5);
					spawnVector = player.getEyeLocation().getDirection();
				} else {
					double yOffset = 0.5;
					spawnVector = result.getHitBlockFace().getDirection();
					if (spawnVector.getY() > 0) {
						yOffset = 0;
					} else if (spawnVector.getY() < 0) {
						yOffset = 1;
					}
					spawnLocation = target.getLocation().add(0.5, yOffset, 0.5);
					spawnLocation = spawnLocation.add(spawnVector.clone().multiply(0.75));
				}

				target.setType(Material.DEEPSLATE);
				LinkedList<ProjectileInstance> projectiles = projectileGroup.start(data, spawnLocation, spawnVector);
				if (projectiles != null) {
					for (ProjectileInstance projectile : projectiles) {
						projectile.getActionMeta().setBool(empowered);
					}
				}
			}

			return TriggerResult.keep();
		});
	}

	private class DeepslateRay extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		public DeepslateRay(PlayerFightData data, int slot) {
			super(0.5, 2, 1);
			this.size(1.25, 1.25).pierce(-1);
			this.ignore(false, true, false);
			this.data = data;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			TICK.play(data.getPlayer(), projectile.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance projectile) {
			if (!projectile.getActionMeta().getBool())
				return;

			meta.addDamageSlice(new DamageSlice(data,
					properties.get(PropertyType.DAMAGE) * (damageMultiplier - 1), properties.getType(),
					DamageStatTracker.of(id + slot, DeepslateScepter.this)));
			if (hitBarrier == null) {
				FightInstance.applyStatus(hit.getEntity(), StatusType.CONCUSSED, data, concussed, -1,
						DeepslateScepter.this);
			}
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
			projectile.applyWeapon(data, DeepslateScepter.this, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_HOE,
				"Dark rays shoot out of the targeted surface and turn the targeted block into deepslate. If it was "
						+ "already deepslate, the rays deal " + DescUtil.val(damageMultiplier + "x")
						+ " damage and apply " + GlossaryTag.CONCUSSED.tag(this, concussed) + ".");
	}
}