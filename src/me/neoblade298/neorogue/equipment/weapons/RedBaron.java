package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
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

public class RedBaron extends Bow {
	private static final String ID = "redBaron";
	private static final TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME), fill = new ParticleContainer(Particle.LAVA);
	private static final Circle circ = new Circle(tp.range);
	private int damage, thres, burn = 30;
	
	public RedBaron(boolean isUpgraded) {
		super(ID, "Red Baron", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(50, 1, 0, 12, 0, 1.5).add(PropertyType.AREA_OF_EFFECT, tp.range));
		damage = isUpgraded ? 90 : 60;
		thres = isUpgraded ? 4 : 5;
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta md = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			ProjectileGroup proj = new ProjectileGroup(new BowProjectile(data, ev.getEntity().getVelocity(), this));
			md.addCount(1);
			if (md.getCount() >= thres) {
				proj.add(new RedBaronProjectile(data));
				md.addCount(-thres);
			}
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW, "Every " + DescUtil.yellow(thres + "th") + " shot also launches a fireball randomly in front of you that arcs, " +
			"dealing " + GlossaryTag.FIRE.tag(this, damage, true) + " damage and applying " + GlossaryTag.BURN.tag(this, burn, false) + 
			" in an area upon hitting an enemy or block.");
	}

	private class RedBaronProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;

		public RedBaronProjectile(PlayerFightData data) {
			super(1, 10, 2);
			this.gravity(0.05);
			this.rotation(NeoRogue.gen.nextDouble(-30, 30));
			this.arc(0.5);
			this.p = data.getPlayer();
			this.data = data;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			dealDamageArea(hit.getEntity().getLocation());
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			dealDamageArea(b.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
		}

		private void dealDamageArea(Location loc) {
			while (loc.getBlock().getType().isAir()) {
				loc.add(0, -1, 0);
			}
			Sounds.explode.play(p, loc);
			circ.play(p, pc, loc, LocalAxes.xz(), fill);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
				DamageMeta dm = new DamageMeta(data, damage, DamageType.FIRE);
				FightInstance.dealDamage(dm, ent);
				FightInstance.applyStatus(ent, StatusType.BURN, data, burn, -1);
			}
		}
		
	}
}
