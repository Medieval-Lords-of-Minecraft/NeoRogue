package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Wildfire extends Equipment {
	private static final String ID = "wildfire";
	private static final TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME), fill = new ParticleContainer(Particle.LAVA);
	private static final Circle circ = new Circle(tp.range);
	private int damage, thres, burn = 30;
	
	public Wildfire(boolean isUpgraded) {
		super(ID, "Wildfire", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.none().add(PropertyType.COOLDOWN, 1));
		damage = isUpgraded ? 90 : 60;
		thres = isUpgraded ? 400 : 600;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			am.addDouble(ev.getMeta().getPostMitigationDamage().getOrDefault(DamageType.FIRE, 0D));
			if (am.getDouble() >= thres && System.currentTimeMillis() - am.getTime() >= 1000) {
				am.addDouble(-thres);
				am.setTime(System.currentTimeMillis());
				ProjectileGroup group = new ProjectileGroup(new WildfireProjectile(data, this, slot));
				group.start(data);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER, "Passive. Every time you do " + GlossaryTag.FIRE.tag(this, thres, true) + " damage, launch a fireball randomly in front of you that arcs, " +
			"dealing " + GlossaryTag.FIRE.tag(this, damage, true) + " damage and applying " + GlossaryTag.BURN.tag(this, burn, false) + 
			" in an area upon hitting an enemy or block.");
	}

	private class WildfireProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private Equipment eq;
		private int slot;

		public WildfireProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(1, 10, 2);
			this.gravity(0.05);
			this.rotation(NeoRogue.gen.nextDouble(-30, 30));
			this.arc(0.5);
			this.p = data.getPlayer();
			this.data = data;
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			dealDamageArea(hit.getEntity().getLocation(), eq, slot);
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			dealDamageArea(b.getLocation(), eq, slot);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
		}

		private void dealDamageArea(Location loc, Equipment eq, int slot) {
			while (loc.getBlock().getType().isAir()) {
				loc.add(0, -1, 0);
			}
			Sounds.explode.play(p, loc);
			circ.play(p, pc, loc, LocalAxes.xz(), fill);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
				DamageMeta dm = new DamageMeta(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, eq));
				FightInstance.dealDamage(dm, ent);
				FightInstance.applyStatus(ent, StatusType.BURN, data, burn, -1);
			}
		}
		
	}
}
