package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
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
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Electrode extends Equipment {
	private static final String ID = "Electrode";
	private int damage, elec;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK);

	public Electrode(boolean isUpgraded) {
		super(ID, "Electrode", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(25, 0, 18, 16));
		damage = isUpgraded ? 300 : 200;
		elec = isUpgraded ? 105 : 70;
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
		private Equipment eq;
		private int slot;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public AnchoringEarthProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(0.7, properties.get(PropertyType.RANGE), 1);
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
			Location end = proj.getLocation();
			Location start = p.getLocation().add(0, 1, 0);
			Sounds.firework.play(p, end);
			ParticleUtil.drawLine(p, pc, end, start, 1);
			for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end,
					TargetProperties.line(end.distance(start), 2, TargetType.ENEMY))) {
				boolean marked = enemiesHit.contains(ent.getUniqueId());
				FightInstance.dealDamage(new DamageMeta(data, marked ? damage * 2 : damage, DamageType.LIGHTNING, DamageStatTracker.of(id + slot, eq)), ent);
				if (marked) {
					FightInstance.applyStatus(ent, StatusType.ELECTRIFIED, data, elec, -1);
				}
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FURNACE,
				"On cast, " + DescUtil.charge(this, 1, 1)
						+ " before firing a projectile that pierces and marks enemies. " + "If it hits a block, deal "
						+ GlossaryTag.LIGHTNING.tag(this, damage, true)
						+ " damage in a line from yourself to the block. If the enemy is marked, "
						+ "deal double damage and apply " + GlossaryTag.ELECTRIFIED.tag(this, elec, true) + ".");
	}
}
