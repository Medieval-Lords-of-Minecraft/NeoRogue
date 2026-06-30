package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Tempest extends Equipment implements Power {
	private static final String ID = "Tempest";
	private int damage, electrified;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(50)
			.spread(0.5, 2)
			.offsetY(1)
			.speed(0.2);
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	
	public Tempest(boolean isUpgraded) {
		super(ID, "Tempest", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0, 5));
		damage = isUpgraded ? 500 : 340;
		electrified = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.ELECTRIFIED)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 5) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata2, in2) -> {
			DealDamageEvent dealEv = (DealDamageEvent) in2;
			if (!dealEv.getMeta().containsType(DamageType.ELECTRIFIED)) return TriggerResult.keep();
			LivingEntity originalTarget = dealEv.getTarget();
			Location targetLoc = originalTarget.getLocation();
			Player p2 = data.getPlayer();
			pc.play(p2, targetLoc);
			Sounds.levelup.play(p2, targetLoc);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p2, targetLoc, tp)) {
				if (ent.getUniqueId().equals(originalTarget.getUniqueId())) continue;
				FightInstance.dealDamage(pdata2, DamageType.LIGHTNING, damage, ent, DamageStatTracker.of(id + slot, this));
				FightInstance.applyStatus(ent, StatusType.ELECTRIFIED, data, electrified, -1, this);
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.END_ROD,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.ELECTRIFIED.tag(this) + " " + DescUtil.white(5) + " times. Whenever you deal damage with " + GlossaryTag.ELECTRIFIED.tag(this) + ", drop a lightning bolt " +
				"onto the target and deal " + GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and apply " +
				GlossaryTag.ELECTRIFIED.tag(this, electrified, true) + " to enemies in the radius, except the original target.");
	}
}
