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
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Zone2 extends Equipment {
	private static final String ID = "zone2";
	private int damage;
	private static final TargetProperties tp = TargetProperties.radius(8, true, TargetType.ALLY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final Circle circ = new Circle(tp.range);
	
	public Zone2(boolean isUpgraded) {
		super(ID, "Zone II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 15, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
		damage = isUpgraded ? 30 : 20;
	}
	
	@Override
	public void setupReforges() {

	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			int inc = damage * data.getStatus(StatusType.FOCUS).getStacks();
			Location loc = p.getLocation();
			double rangesq = tp.range * tp.range;
			Equipment eq = this;
			data.addTask(new BukkitRunnable() {
				int count = 0;
				boolean stayed = true;
				public void run() {
					circ.play(pc, loc, LocalAxes.xz(), null);
					if (p.getLocation().distanceSquared(loc) >= rangesq) {
						data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, inc, StatTracker.damageBuffAlly(eq)), 40);
					}
					else {
						stayed = false;
					}
					if (++count >= 8) {
						this.cancel();
						if (stayed) {
							data.applyStatus(StatusType.FOCUS, data, 1, -1);
						}
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 20L));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COPPER_GRATE,
				"On cast, drop a zone that lasts <white>8s</white> which buffs your " + GlossaryTag.PHYSICAL.tag(this) + " damage by " + DescUtil.yellow(damage) +
				" multiplied by up to <white>3</white> stacks of " + GlossaryTag.FOCUS.tag(this) + " while you're in it. Staying within the zone for the entire duration will grant " +
				GlossaryTag.FOCUS.tag(this, 1, false) + ".");
	}
}
