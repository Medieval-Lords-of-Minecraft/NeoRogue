package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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

public class ThunderboltScepter extends Equipment {
	private static final String ID = "ThunderboltScepter";
	private static final int HIT_SCAN_RANGE = 12;
	private static final ParticleContainer TICK = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(25).spread(0.1, 0.1).speed(0.01);

	private final int bonusDamage;
	private final int electrified;

	public ThunderboltScepter(boolean isUpgraded) {
		super(ID, "Thunderbolt Scepter", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(3, 0, isUpgraded ? 100 : 80, 0.5, DamageType.LIGHTNING,
						Sound.ITEM_AXE_SCRAPE));
		bonusDamage = isUpgraded ? 50 : 30;
		electrified = isUpgraded ? 6 : 4;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ProjectileGroup proj = new ProjectileGroup(new ThunderboltRay(data, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			Player p = data.getPlayer();
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);

			RayTraceResult result = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(),
					HIT_SCAN_RANGE, FluidCollisionMode.NEVER, false);
			if (result != null) {
				Location spawnLoc;
				Vector spawnVec;
				if (result.getHitBlock().isPassable()) {
					spawnLoc = result.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
					spawnVec = p.getEyeLocation().getDirection();
				} else {
					double yOff = 0.5;
					spawnVec = result.getHitBlockFace().getDirection();
					if (spawnVec.getY() > 0) {
						yOff = 0;
					} else if (spawnVec.getY() < 0) {
						yOff = 1;
					}
					spawnLoc = result.getHitBlock().getLocation().add(0.5, yOff, 0.5);
					spawnLoc = spawnLoc.add(spawnVec.clone().multiply(0.75));
				}
				proj.start(data, spawnLoc, spawnVec);
			}

			return TriggerResult.keep();
		});
	}

	private class ThunderboltRay extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		public ThunderboltRay(PlayerFightData data, int slot) {
			super(0.5, 2, 1);
			this.size(1.25, 1.25).pierce(-1);
			this.ignore(false, true, false);
			this.data = data;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			TICK.play(data.getPlayer(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			if (hitBarrier != null || proj.getActionMeta().addCount(1) <= 1)
				return;

			meta.addDamageSlice(new DamageSlice(data, bonusDamage, DamageType.LIGHTNING,
					DamageStatTracker.of(id + slot, ThunderboltScepter.this)));
			FightInstance.applyStatus(hit.getEntity(), StatusType.ELECTRIFIED, data, electrified, -1,
					ThunderboltScepter.this);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyWeapon(data, ThunderboltScepter.this, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_HOE,
				"Lightning rays shoot out of the targeted surface. The second and each subsequent enemy hit "
						+ "takes an additional " + GlossaryTag.LIGHTNING.tag(this, bonusDamage)
						+ " damage and gains " + GlossaryTag.ELECTRIFIED.tag(this, electrified) + ".");
	}
}