package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AzureCutter extends Artifact {
	private static final String ID = "azureCutter";
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORK).count(1).speed(0.005).spread(0.1, 0.1);
	private static final TargetProperties tp = TargetProperties.radius(8, false, TargetType.ENEMY);
	private static int thres = 7, damage = 100;

	public AzureCutter() {
		super(ID, "Azure Cutter", Rarity.UNCOMMON, EquipmentClass.THIEF);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		ProjectileGroup proj = new ProjectileGroup(new AzureCutterProjectile(data));
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			act.addCount(1);
			if (act.getCount() < thres)  return TriggerResult.keep();
			LivingEntity trg = TargetHelper.getNearest(p, tp);
			if (trg == null) return TriggerResult.keep();
			act.addCount(-thres);
			Vector dir = trg.getLocation().subtract(p.getLocation()).toVector();
			proj.start(data, p.getLocation(), dir);
			return TriggerResult.keep();
		});
	}

	private class AzureCutterProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;

		public AzureCutterProjectile(PlayerFightData data) {
			super(0.8, tp.range, 1);
			this.size(1, 1);
			this.initialY(1.5);
			this.data = data;
			this.p = data.getPlayer();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			part.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Sounds.breaks.play(p, hit.getEntity());
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.PIERCING), hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.success.play(p, p);
		}
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLUE_BANNER, 
				"Every " + DescUtil.white(thres) + " basic attacks, launch a projectile at the nearest enemy "
				+ "that deals " + GlossaryTag.PIERCING.tag(this, damage, false) + " damage.");
	}
}
