package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AnchoringEarth extends Equipment {
	private static final String ID = "AnchoringEarth";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DIRT.createBlockData());
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK);

	public AnchoringEarth(boolean isUpgraded) {
		super(ID, "Anchoring Earth", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(10, 10, 18, 12));
		damage = isUpgraded ? 180 : 120;
	}

	@Override
	public void setupReforges() {
		addReforge(CalculatingGaze.get(), AnchoringEarth2.get(), EarthenDomain.get());
		addReforge(Intuition.get(), Electrode.get());
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
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
		private HashSet<UUID> enemiesHit = new HashSet<UUID>();
		private int slot;
		private Equipment eq;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public AnchoringEarthProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(1, properties.get(PropertyType.RANGE), 1);
			this.data = data;
			this.p = data.getPlayer();
			this.pierce(-1);
			this.slot = slot;
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			enemiesHit.add(hit.getUniqueId());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
			enemiesHit.clear();
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			Location loc = b.getLocation();
			sc.play(p, loc);
			for (UUID uuid : enemiesHit) {
				FightData fd = FightInstance.getFightData(uuid);
				if (fd == null || fd.getEntity() == null || !fd.getEntity().isValid())
					continue;
				LivingEntity ent = fd.getEntity();
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN, DamageStatTracker.of(id + slot, eq)), ent);
				ParticleUtil.drawLine(p, pc, loc, ent.getLocation(), 1);
				Vector pull = loc.toVector().subtract(fd.getEntity().getLocation().toVector()).normalize()
						.multiply(0.5);
				FightInstance.knockback(ent, pull);
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STRING,
				"On cast, " + DescUtil.charge(this, 1, 1) + " before firing a projectile that pierces enemies. "
						+ "If it hits a block, all enemies it pierced will take "
						+ GlossaryTag.EARTHEN.tag(this, damage, true) + ", receive " + DescUtil.potion("slowness", 1, 2)
						+ ", and be pulled towards the block.");
	}
}
