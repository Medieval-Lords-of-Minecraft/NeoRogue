package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

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
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Demoralize extends Equipment {
	private static final String ID = "demoralize";
	private static final TargetProperties tp = TargetProperties.radius(8, false, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private int injure, dec;
	
	public Demoralize(boolean isUpgraded) {
		super(ID, "Demoralize", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 5, 16, 0, tp.range));
		injure = isUpgraded ? 120 : 80;
		dec = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		LinkedList<ActionMeta> insts = new LinkedList<ActionMeta>();
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			ActionMeta am = new ActionMeta();
			am.setBool(true);
			am.setTime(System.currentTimeMillis() + 5000);
			insts.add(am);
			data.addTask(new BukkitRunnable() {
				public void run() {
					if (am.getBool()) {
						am.setBool(false);
						Sounds.fire.play(p, p);
						circ.play(p, pc, p.getLocation(), LocalAxes.xz(), null);
						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
							FightData fd = FightInstance.getFightData(ent);
							fd.applyStatus(StatusType.INJURY, data, injure, -1);
							fd.addBuff(data, id, false, true, DamageBuffType.GENERAL, -dec * 0.01, 160);
						}
					}
					insts.removeFirst();
				}
			}.runTaskLater(NeoRogue.inst(), 100));
			return TriggerResult.keep();
		}));
		
		data.addTrigger(id, Trigger.RECEIVED_HEALTH_DAMAGE, (pdata, in) -> {
			if (insts.isEmpty()) return TriggerResult.keep();
			for (ActionMeta am : insts) {
				am.setBool(false);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TARGET,
				"On cast, if you do not take health damage for the next <white>5s</white>, all nearby enemies are granted " + GlossaryTag.INJURY.tag(this, injure, true) + " and have " +
				"their defense decreased by " + DescUtil.yellow(dec + "%") + " [<white>8s</white>]." );
	}
}
