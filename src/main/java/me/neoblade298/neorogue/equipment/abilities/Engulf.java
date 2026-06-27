package me.neoblade298.neorogue.equipment.abilities;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Engulf extends Equipment implements Power {
	private static final String ID = "Engulf";
	private static final TargetProperties tp = TargetProperties.radius(5, false);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).offsetY(0.3).spread(0.2, 0.2)
			.count(5);
	private static final Circle circ = new Circle(tp.range);
	private int damage, thres;

	public Engulf(boolean isUpgraded) {
		super(ID, "Engulf", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		damage = isUpgraded ? 45 : 30;
		thres = isUpgraded ? 200 : 300;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 3;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta activationAm = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.FIRE)) return TriggerResult.keep();
			activationAm.addCount(1);
			if (activationAm.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	private static final int COOLDOWN_TICKS = 100; // 5 seconds

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta am = new ActionMeta();
		boolean[] onCooldown = new boolean[] { false };
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.DEAL_DAMAGE, (pdata2, in2) -> {
					if (onCooldown[0]) return TriggerResult.keep();
					DealDamageEvent ev2 = (DealDamageEvent) in2;
					// Don't count Engulf's own damage
					for (DamageSlice slice : ev2.getMeta().getSlices()) {
						if (slice.getTracker().getId().equals(id + slot)) return TriggerResult.keep();
					}
					HashMap<DamageType, Double> dmg = ev2.getMeta().getPostMitigationDamage();
					if (!dmg.containsKey(DamageType.FIRE))
						return TriggerResult.keep();
					am.addCount((int) (dmg.get(DamageType.FIRE) + 0));
					if (am.getCount() >= thres) {
						am.addCount(-thres);
						onCooldown[0] = true;
						data.addTask(new BukkitRunnable() {
							public void run() {
								onCooldown[0] = false;
							}
						}.runTaskLater(NeoRogue.inst(), COOLDOWN_TICKS));
						data.addTask(new BukkitRunnable() {
							private int count = 0;
							public void run() {
								Player p2 = data.getPlayer();
								Sounds.fire.play(p2, p2);
								circ.play(pc, p2.getLocation(), LocalAxes.xz(), null);
								for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p2, tp)) {
									FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE,
											DamageStatTracker.of(id + slot, Engulf.this)), ent);
								}
								if (++count >= 3) {
									cancel();
								}
							}
						}.runTaskTimer(NeoRogue.inst(), 20, 20));
					}
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				GlossaryTag.POWER.tag(this) + ". Activates after dealing " + GlossaryTag.FIRE.tag(this) + " damage " + DescUtil.white(3) + " times. Every time you deal " + GlossaryTag.FIRE.tag(this, thres, true) + " damage from other sources, deal "
						+ GlossaryTag.FIRE.tag(this, damage, true)
						+ " damage to all enemies near you " + DescUtil.white(3) + " times over " + DescUtil.white("3s") + ". " + DescUtil.white("5s") + " cooldown.");
	}
}
