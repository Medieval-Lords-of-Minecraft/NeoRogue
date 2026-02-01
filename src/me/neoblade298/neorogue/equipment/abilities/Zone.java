package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Zone extends Equipment {
	private static final String ID = "Zone";
	private int damage;
	private static final TargetProperties tp = TargetProperties.radius(8, true, TargetType.ALLY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final Circle circ = new Circle(tp.range);
	
	public Zone(boolean isUpgraded) {
		super(ID, "Zone", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 15, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
		damage = isUpgraded ? 30 : 20;
	}
	
	@Override
	public void setupReforges() {
		addReforge(KeenSenses.get(), Zone2.get(), Chokehold.get());
		addReforge(AgilityTraining.get(), Pressure.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			Location loc = p.getLocation();
			double rangesq = tp.range * tp.range;
			Equipment eq = this;
			data.addTask(new BukkitRunnable() {
				int count = 0;
				public void run() {
					circ.play(pc, loc, LocalAxes.xz(), null);
					if (p.getLocation().distanceSquared(loc) <= rangesq) {
						data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, damage, StatTracker.damageBuffAlly(buffId, eq)), 40);
					}
					if (++count >= 8) {
						this.cancel();
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
				" while you're in it.");
	}
}
