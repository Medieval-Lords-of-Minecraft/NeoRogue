package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class TargetAcquisition extends Equipment {
	private static final String ID = "targetAcquisition";
	private int damage;
	private static final ParticleContainer part = new ParticleContainer(Particle.LARGE_SMOKE).offsetY(1).count(25).spread(0.5, 0.5).speed(0.01),
			hit = new ParticleContainer(Particle.DUST).count(25).offsetY(1).spread(0.5, 0.5);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_ZOMBIE_INFECT);
	
	public TargetAcquisition(boolean isUpgraded) {
		super(ID, "Target Acquisition", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 10, 0));
		
		damage = isUpgraded ? 70 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata, in) -> {
			if (!inst.canUse()) return TriggerResult.keep();
			inst.setNextUse((long) (System.currentTimeMillis() + (properties.get(PropertyType.COOLDOWN) * 1000)));
			sc.play(p, p);
			part.play(p, p);
			inst.addCount(1);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.KILL, inst);
		
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			if (inst.getCount() <= 0) return TriggerResult.keep();
			inst.addCount(-1);
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(id + slot, this)));
			Sounds.anvil.play(p, p);
			hit.play(p, ev.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				"Passive. Killing an enemy grants your next basic attack an additional " +
				GlossaryTag.PIERCING.tag(this, damage, true) + " damage.");
	}
}
