package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Pressure extends Equipment {
	private static final String ID = "Pressure";
	private int damage;
	private static final TargetProperties tp = TargetProperties.radius(8, true, TargetType.ALLY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final Circle circ = new Circle(tp.range);
	
	public Pressure(boolean isUpgraded) {
		super(ID, "Pressure", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 0, 15, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
		damage = isUpgraded ? 75 : 50;
	}
	
	@Override
	public void setupReforges() {

	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			Location loc = p.getLocation();
			am.setLocation(loc);
			am.setBool(true);
			data.addTask(new BukkitRunnable() {
				int count = 0;
				public void run() {
					circ.play(pc, loc, LocalAxes.xz(), null);
					if (++count >= 8) {
						am.setBool(false);
						this.cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 20L));
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			double rangesq = tp.range * tp.range;
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			// Check if the zone is active
			if (!am.getBool()) return TriggerResult.keep();
			if (ev.getTarget().getLocation().distanceSquared(am.getLocation()) <= rangesq &&
				p.getLocation().distanceSquared(am.getLocation()) <= rangesq) {
					ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, damage, StatTracker.damageBuffAlly(am.getId(), this)));
			}
			return TriggerResult.keep();

		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COPPER_GRATE,
				"On cast, drop a zone that lasts <white>8s</white>. Dealing damage to an enemy while you are both within the zone increases " +
				"the damage by " + DescUtil.yellow(damage) + ".");
	}
}
