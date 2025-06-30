package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EarthenDomain extends Equipment {
	private static final String ID = "earthenDomain";
	private int damage, conc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DIRT.createBlockData());
	private static final TargetProperties tp = TargetProperties.radius(5, true);
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK);
	private static final Circle circ = new Circle(tp.range);

	public EarthenDomain(boolean isUpgraded) {
		super(ID, "Earthen Domain", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(30, 15, 18, 12, tp.range));
		damage = isUpgraded ? 150 : 100;
		conc = isUpgraded ? 45 : 30;
	}

	@Override
	public void setupReforges() {

	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup projs = new ProjectileGroup(new AnchoringEarthProjectile(data, this, slot));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			data.charge(20).then(new Runnable() {
				public void run() {
					projs.start(data);
				}
			});
			return TriggerResult.keep();
		}));
	}

	private class AnchoringEarthProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public AnchoringEarthProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(1, properties.get(PropertyType.RANGE), 1);
			this.data = data;
			this.p = data.getPlayer();
			this.pierce(-1);
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {

		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			Location loc = b.getLocation();
			final Location fLoc = loc.add(0, 1, 0).getBlock().getType() == Material.AIR ? loc.add(0, 1, 0) : loc;

			data.addTask(new BukkitRunnable() {
				public void run() {
					LinkedList<LivingEntity> trgs = TargetHelper.getEntitiesInRadius(p, loc, tp);
					if (trgs.isEmpty()) {
						cancel();
						return;
					}

					circ.play(pc, fLoc, LocalAxes.xz(), null);
					sc.play(p, fLoc);
					for (LivingEntity trg : trgs) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN, DamageStatTracker.of(id + slot, eq)), trg);
						FightInstance.applyStatus(trg, StatusType.CONCUSSED, data, conc, -1);
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 20, 20));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STRING,
				"On cast, " + DescUtil.charge(this, 1, 1) + " before firing a projectile. "
						+ "If it hits a block, it will deal " + GlossaryTag.BLUNT.tag(this, damage, true)
						+ " damage and apply " + GlossaryTag.CONCUSSED.tag(this, conc, true)
						+ " to enemies near the block once per second until no enemies are nearby.");
	}
}
