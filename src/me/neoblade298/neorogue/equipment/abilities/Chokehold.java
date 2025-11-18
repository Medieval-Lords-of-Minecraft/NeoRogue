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
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Chokehold extends Equipment {
	private static final String ID = "Chokehold";
	private double damage;
	private static final TargetProperties tp = TargetProperties.radius(10, true, TargetType.ALLY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final Circle circ = new Circle(tp.range);
	
	public Chokehold(boolean isUpgraded) {
		super(ID, "Chokehold", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 0, 15, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
		damage = isUpgraded ? 0.5 : 0.3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		String buffId = UUID.randomUUID().toString();

		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			Location loc = p.getLocation();
			am.setLocation(loc);
			am.setTime(System.currentTimeMillis());
			data.addTask(new BukkitRunnable() {
				int count = 0;
				public void run() {
					circ.play(pc, loc, LocalAxes.xz(), null);
					pc.play(p, loc);
					if (++count >= 15) {
						this.cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 20L));
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			double rangeSq = tp.range * tp.range;
			if (!ev.getMeta().hasOrigin(DamageOrigin.TRAP)) return TriggerResult.keep();
			if (am.getTime() + 15000 < System.currentTimeMillis()) return TriggerResult.keep();
			if (ev.getTarget().getLocation().distanceSquared(am.getLocation()) > rangeSq) return TriggerResult.keep();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL, DamageOrigin.TRAP), Buff.multiplier(data, damage, StatTracker.damageBuffAlly(buffId, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COPPER_GRATE,
				"On cast, drop a zone that lasts <white>15s</white> which buffs damage of " + GlossaryTag.TRAP.tagPlural(this) + " within the zone by "
				+ DescUtil.yellow(damage * 100 + "%") + ".");
	}
}
