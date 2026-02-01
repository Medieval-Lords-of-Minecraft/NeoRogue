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
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class AnchoringEarth2 extends Equipment {
	private static final String ID = "AnchoringEarth2";
	private int damage, reduc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DIRT.createBlockData());
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK);

	public AnchoringEarth2(boolean isUpgraded) {
		super(ID, "Anchoring Earth II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 10, 18, 12));
		damage = 250;
		reduc = isUpgraded ? 90 : 60;
	}

	@Override
	public void setupReforges() {

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

		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			Player p = data.getPlayer();
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			FightData fd = ev.getTarget();
			if (!fd.hasStatus("anchoringEarth2-" + p.getName()))
				return TriggerResult.keep();

			ev.getStacksBuffList().add(Buff.multiplier(data, 1D, BuffStatTracker.of(buffId, this, "Statuses doubled")));
			return TriggerResult.keep();
		});
	}

	private class AnchoringEarthProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private HashSet<UUID> enemiesHit = new HashSet<UUID>();
		private Equipment eq;
		private int slot;

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
				fd.applyStatus(
						Status.createByGenericType(GenericStatusType.BASIC, "anchoringEarth2-" + p.getName(), fd, true),
						fd, 1, 160);
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STRING, "On cast, " + DescUtil.charge(this, 1, 1)
				+ " before firing a projectile that pierces enemies and lowers their magic defense by "
				+ DescUtil.yellow(reduc) + ". " + "If it hits a block, all enemies it pierced will take "
				+ GlossaryTag.EARTHEN.tag(this, damage, true) + ", receive " + DescUtil.potion("slowness", 1, 2)
				+ ", be pulled towards the block, and all statuses applied to them by you are doubled for <white>8s</white>. ");
	}
}
