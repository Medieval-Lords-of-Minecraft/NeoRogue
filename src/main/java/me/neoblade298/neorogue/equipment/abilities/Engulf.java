package me.neoblade298.neorogue.equipment.abilities;

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
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
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
				EquipmentProperties.none());
		damage = isUpgraded ? 45 : 30;
		thres = isUpgraded ? 15 : 20;
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

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta am = new ActionMeta();
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata2, in2) -> {
					ApplyStatusEvent ev2 = (ApplyStatusEvent) in2;
					if (!ev2.isStatus(StatusType.BURN)) return TriggerResult.keep();
					am.addCount(ev2.getStacks());
					if (am.getCount() >= thres) {
						am.addCount(-thres);
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
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after dealing " + GlossaryTag.FIRE.tag(this) + " damage " + DescUtil.val(3) + " times. Every time you apply " + GlossaryTag.BURN.tag(this, thres) + ", deal "
						+ GlossaryTag.FIRE.tag(this, damage)
						+ " damage to all enemies near you " + DescUtil.val(3) + " times over " + DescUtil.val("3s") + ".");
	}
}
