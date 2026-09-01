package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MiniMechanism extends Equipment {
	private static final String ID = "MiniMechanism";
	private static final int INTERVAL_SECONDS = 20;
	private static final int TRAP_DURATION_TICKS = 400;
	private static final TargetProperties STEP_TARGET = TargetProperties.radius(1.5, false, TargetType.ENEMY);
	private static final TargetProperties BLAST_TARGET = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static final Circle TRIGGER_AREA = new Circle(STEP_TARGET.range);
	private static final Circle BLAST_AREA = new Circle(BLAST_TARGET.range);
	private static final ParticleContainer TRAP_PARTICLE = new ParticleContainer(Particle.CRIT).count(1)
			.spread(0, 0).speed(0);
	private static final ParticleContainer BLAST_EDGE = new ParticleContainer(Particle.CLOUD).count(1)
			.spread(0, 0).speed(0);
	private static final ParticleContainer BLAST_BURST = new ParticleContainer(Particle.EXPLOSION).count(3)
			.spread(0.1, 0.1).offsetY(0.2).speed(0);
	private int damage;

	public MiniMechanism(boolean isUpgraded) {
		super(ID, "Mini Mechanism", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER, EquipmentType.ACCESSORY);
		damage = isUpgraded ? 120 : 80;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta timer = new ActionMeta();
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (timer.addCount(1) < INTERVAL_SECONDS) return TriggerResult.keep();
			timer.setCount(0);
			placeTrap(data, slot);
			return TriggerResult.keep();
		});
	}

	private void placeTrap(PlayerFightData data, int slot) {
		Location location = data.getPlayer().getLocation().clone();
		data.addTrap(new Trap(data, location, TRAP_DURATION_TICKS, this) {
			@Override
			public void tick() {
				Player player = data.getPlayer();
				TRIGGER_AREA.play(TRAP_PARTICLE, location, LocalAxes.xz(), null);
				if (TargetHelper.getNearest(player, location, STEP_TARGET) == null) return;

				BLAST_AREA.play(BLAST_EDGE, location, LocalAxes.xz(), null);
				BLAST_BURST.play(player, location);
				Sounds.explode.play(player, location);
				for (LivingEntity target : TargetHelper.getEntitiesInRadius(player, location, BLAST_TARGET)) {
					DamageMeta damageMeta = new DamageMeta(data, damage, DamageType.BLUNT,
							DamageStatTracker.of(id + slot, MiniMechanism.this), DamageOrigin.TRAP);
					FightInstance.dealDamage(damageMeta, target);
				}
				data.removeTrap(this);
			}
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TRIPWIRE_HOOK, "Every " + DescUtil.val(INTERVAL_SECONDS + "s")
				+ ", drop a " + GlossaryTag.TRAP.tag(this) + " at your location ["
				+ DescUtil.val("20s") + "]. When an enemy steps on it, deal "
				+ GlossaryTag.BLUNT.tag(this, damage) + " damage to enemies within "
				+ DescUtil.val((int) BLAST_TARGET.range) + " blocks.");
	}
}