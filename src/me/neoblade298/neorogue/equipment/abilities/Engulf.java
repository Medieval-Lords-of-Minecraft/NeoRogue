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
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Engulf extends Equipment {
	private static final String ID = "Engulf";
	private static final TargetProperties tp = TargetProperties.radius(3, false);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).offsetY(0.3).spread(0.2, 0.2).count(5);
	private static final Circle circ = new Circle(tp.range);
	private int damage, thres;

	public Engulf(boolean isUpgraded) {
		super(ID, "Engulf", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0, tp.range));
		damage = isUpgraded ? 60 : 90;
		thres = isUpgraded ? 300 : 500;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			HashMap<DamageType, Double> dmg = ev.getMeta().getPostMitigationDamage();

			if (!dmg.containsKey(DamageType.FIRE)) return TriggerResult.keep();
			am.addCount((int) (dmg.get(DamageType.FIRE) + 0));

			if (am.getCount() >= thres) {
				am.addCount(-thres);
				data.addTask(new BukkitRunnable() {
					private int count = 0;

					public void run() {
						Player p = data.getPlayer();
						Sounds.fire.play(p, p);
						circ.play(pc, p.getLocation(), LocalAxes.xz(), null);
						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
							FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, Engulf.this)), ent);
						}

						if (++count >= 3) {
							cancel();
						}
					}
				}.runTaskTimer(NeoRogue.inst(), 20, 20));
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				"Passive. Every time you deal " + GlossaryTag.FIRE.tag(this, thres, true) + " damage, deal "
						+ GlossaryTag.FIRE.tag(this, damage, true)
						+ " damage to all enemies near you <white>3</white> times over <white>3s</white>.");
	}
}
