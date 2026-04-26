package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class ChargeBolt extends Equipment {
	private static final String ID = "ChargeBolt";
	private static final TargetProperties tp = TargetProperties.line(12, 2, TargetType.ENEMY);
	private static final ParticleContainer tick = new ParticleContainer(Particle.ELECTRIC_SPARK).count(3).spread(0.2, 0.2);
	private int damage, manaReduction;

	public ChargeBolt(boolean isUpgraded) {
		super(ID, "Charge Bolt", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 8, tp.range));
		damage = isUpgraded ? 300 : 200;
		manaReduction = isUpgraded ? 30 : 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta hasReduction = new ActionMeta();
		Equipment eq = this;
		String procId = id + slot;
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			LivingEntity target = TargetHelper.getNearestInSight(p, tp);
			if (target == null) return TriggerResult.keep();

			Location start = p.getLocation().add(0, 1, 0);
			Vector dir = p.getEyeLocation().getDirection();
			Location end = start.clone().add(dir.clone().multiply(properties.get(PropertyType.RANGE)));
			ParticleUtil.drawLine(p, tick, start, end, 0.3);
			Sounds.thunder.play(p, p);

			FightData targetData = FightInstance.getFightData(target.getUniqueId());
			boolean hadElectrified = targetData != null && targetData.hasStatus(StatusType.ELECTRIFIED);
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING,
					DamageStatTracker.of(procId, eq)), target);
			if (hadElectrified) {
				hasReduction.setBool(true);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			if (!hasReduction.getBool()) return TriggerResult.keep();
			PreCastUsableEvent ev = (PreCastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();
			ev.addBuff(PropertyType.MANA_COST, procId,
					Buff.increase(data, manaReduction, BuffStatTracker.of(procId, eq, "Mana Cost Reduced")));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			if (!hasReduction.getBool()) return TriggerResult.keep();
			CastUsableEvent ev = (CastUsableEvent) in;
			if (!ev.hasTag(procId)) return TriggerResult.keep();
			hasReduction.setBool(false);
			Sounds.success.play(data.getPlayer(), data.getPlayer());
			return TriggerResult.keep();
		});

		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				"On cast, fire a bolt that deals " + GlossaryTag.LIGHTNING.tag(this, damage, true) +
				" damage. If you hit an enemy with " + GlossaryTag.ELECTRIFIED.tag(this) +
				", your next ability cast costs " + DescUtil.yellow(manaReduction) + " less mana.");
	}
}
