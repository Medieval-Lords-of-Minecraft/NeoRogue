package me.neoblade298.neorogue.equipment.offhands;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffList;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageBarrierEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class BatteringRam extends Equipment {
	private static final String ID = "batteringRam";
	private int reduction, damage, thres, conc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CRIT);
	private static final TargetProperties tp = TargetProperties.cone(60, 4, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	
	public BatteringRam(boolean isUpgraded) {
		super(ID, "Battering Ram", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = isUpgraded ? 6 : 8;
		damage = isUpgraded ? 120 : 80;
		thres = 6;
		conc = isUpgraded ? 45 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RAISE_SHIELD, (pdata, inputs) -> {
			if (am.getCount() >= 5) return TriggerResult.remove();
			Barrier b = Barrier.centered(p, 4, 2, 2, 0, new HashMap<DamageBuffType, BuffList>(), null, true);
			UUID uuid = data.addBarrier(b);
			b.tick();
			am.setUniqueId(uuid);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.SHIELD_TICK, (pdata, inputs) -> {
			if (am.getCount() >= thres) return TriggerResult.remove();
			Barrier b = data.getBarrier(am.getUniqueId());
			if (b == null) return TriggerResult.keep();
			b.tick();
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, inputs) -> {
			if (am.getCount() >= thres) return TriggerResult.remove();
			data.removeBarrier(am.getUniqueId());
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.RECEIVED_DAMAGE_BARRIER, (pdata, in) -> {
			ReceivedDamageBarrierEvent ev = (ReceivedDamageBarrierEvent) in;
			Barrier b = ev.getBarrier();
			if (!b.getUniqueId().equals(am.getUniqueId())) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() >= thres) {
				data.removeBarrier(am.getUniqueId());
				p.playSound(p, Sound.ITEM_SHIELD_BREAK, 1F, 1F);
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, inputs) -> {
			if (p.getHandRaised() != EquipmentSlot.OFF_HAND || !p.isHandRaised()) return TriggerResult.keep();
			ReceivedDamageEvent ev = (ReceivedDamageEvent) inputs;
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, reduction, 0, StatTracker.defenseBuffAlly(am.getId(), this)));
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			cone.play(pc, p.getLocation(), LocalAxes.usingEyeLocation(p), pc);
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.BLUNT, DamageStatTracker.of(id + slot, this)), ent);
				FightInstance.applyStatus(ent, StatusType.CONCUSSED, data, conc, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "When raised, reduces all damage by <yellow>" + reduction + "</yellow> and " +
		"creates a " + GlossaryTag.BARRIER.tag(this) + " that blocks " + DescUtil.white(thres) + " projectiles before breaking. Receiving damage while your shield is raised " +
		"deals " + GlossaryTag.BLUNT.tag(this, damage, true) + " and applies " + GlossaryTag.CONCUSSED.tag(this, conc, true) + " in a cone in front of you.");
	}
}
