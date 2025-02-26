package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.BowProjectile;
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
import me.neoblade298.neorogue.session.fight.DamageSlice;
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
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class MarkTarget2 extends Equipment {
	private static final String ID = "markTarget2";
	private static final TargetProperties tp = TargetProperties.radius(8, false, TargetType.ENEMY);
	private int rend;
	private double damage;
	private static final ParticleContainer taunt = new ParticleContainer(Particle.CRIMSON_SPORE).count(50).spread(0.3, 0.3).offsetY(2);
	
	public MarkTarget2(boolean isUpgraded) {
		super(ID, "Mark Target II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 0, 12, tp.range));
		rend = isUpgraded ? 40 : 30;
		damage = 0.4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			LivingEntity trg = TargetHelper.getNearest(p, tp);
			if (trg == null) return TriggerResult.keep();
			taunt.play(p, trg);
			Sounds.infect.play(p, trg);
			am.setEntity(trg);
			data.addTask(new BukkitRunnable() {
				public void run() {
					am.setEntity(null);
				}
			}.runTaskLater(NeoRogue.inst(), 160));
			FightInstance.applyStatus(trg, StatusType.REND, data, rend, -1);
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (!fd.hasStatus(StatusType.REND)) return TriggerResult.keep();
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage * fd.getStatus(StatusType.REND).getStacks(), DamageType.SLASHING));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			if (am.getEntity() == null) return TriggerResult.keep();
			if (!am.getEntity().getUniqueId().equals(ev.getTarget().getUniqueId())) return TriggerResult.keep();
			ProjectileGroup projs = new ProjectileGroup(new MarkTarget2Projectile(data));
			projs.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_BRICK,
				"On cast, mark [<white>8s</white>] and apply " + GlossaryTag.REND.tag(this, rend, false) + " to the enemy you're looking at. " +
				"Damaging a marked enemy with a basic attack fires a homing projectile using your current ammunition with a base damage of " + DescUtil.yellow(damage) + ". " +
				"Additionally, you passively " +
				"deal an additional " + GlossaryTag.SLASHING.tag(this, damage, false) + " damage per stack of " + GlossaryTag.REND.tag(this) + ".");
	}

	private class MarkTarget2Projectile extends Projectile {
		private AmmunitionInstance ammo;
		private PlayerFightData data;
		private Player p;
		public MarkTarget2Projectile(PlayerFightData data) {
			super(tp.range, 1);
			this.blocksPerTick(3);
			this.homing(0.02);
			this.data = data;
			this.p = data.getPlayer();
			ammo = data.getAmmoInstance();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			BowProjectile.tick.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			ammo.onHit(proj, meta, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			double dmg = ammoProps.get(PropertyType.DAMAGE);
			dm.addDamageSlice(new DamageSlice(data, damage + dmg, ammoProps.getType()));
			ammo.onStart(proj);
		}
	}
}
