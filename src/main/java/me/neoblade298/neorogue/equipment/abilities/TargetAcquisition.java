package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class TargetAcquisition extends Equipment implements Power {
	private static final String ID = "TargetAcquisition";
	private int damage, shields;
	private static final ParticleContainer part = new ParticleContainer(Particle.LARGE_SMOKE).offsetY(1).count(25).spread(0.5, 0.5).speed(0.01),
			hit = new ParticleContainer(Particle.DUST).count(25).offsetY(1).spread(0.5, 0.5);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_ZOMBIE_INFECT);
	
	public TargetAcquisition(boolean isUpgraded) {
		super(ID, "Target Acquisition", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		
		damage = isUpgraded ? 50 : 30;
		shields = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			am.addCount((int) ev.getTotalDamage());
			if (am.getCount() < 300) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata2, in2) -> {
			Player p2 = data.getPlayer();
			sc.play(p2, p2);
			part.play(p2, p2);
			inst.addCount(1);
			data.addSimpleShield(p2.getUniqueId(), shields, 100, this);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.KILL, inst);

		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata3, in3) -> {
			Player p3 = data.getPlayer();
			PreBasicAttackEvent ev3 = (PreBasicAttackEvent) in3;
			if (inst.getCount() <= 0) return TriggerResult.keep();
			inst.addCount(-1);
			ev3.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(id + slot, this)));
			Sounds.anvil.play(p3, p3);
			hit.play(p3, ev3.getTarget());
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after dealing " + DescUtil.white(300) + " damage. Killing an enemy grants your next basic attack an additional " +
				GlossaryTag.PIERCING.tag(this, damage, true) + " damage and grants " +
				GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>].");
	}
}
