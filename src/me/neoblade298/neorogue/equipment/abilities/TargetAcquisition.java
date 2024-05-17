package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class TargetAcquisition extends Equipment {
	private static final String ID = "targetAcquisition";
	private int damage;
	private static final ParticleContainer part = new ParticleContainer(Particle.SMOKE_LARGE).offsetY(1).count(25).spread(0.5, 0.5).speed(0.01),
			hit = new ParticleContainer(Particle.REDSTONE).count(25).offsetY(1).spread(0.5, 0.5);
	private static final TargetProperties tp = TargetProperties.radius(15, true, TargetType.ENEMY);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_ZOMBIE_INFECT);
	
	public TargetAcquisition(boolean isUpgraded) {
		super(ID, "Target Acquisition", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, tp.range));
		
		damage = isUpgraded ? 70 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		TargetAcquisitionInstance inst = new TargetAcquisitionInstance(id);
		data.addTrigger(id, Trigger.KILL, inst);

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			if (inst.trg != ev.getTarget()) return TriggerResult.keep();
			ev.getMeta().addDamageSlice(new DamageSlice(pdata, damage, DamageType.PIERCING));
			Sounds.anvil.play(p, ev.getTarget());
			hit.play(p, ev.getTarget());
			inst.trg = null;
			return TriggerResult.keep();
		});
	}
	
	private class TargetAcquisitionInstance extends PriorityAction {
		private LivingEntity trg;
		
		public TargetAcquisitionInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				pdata.addTask(new BukkitRunnable() {
					public void run() {
						Player p = pdata.getPlayer();
						trg = TargetHelper.getNearest(p, tp);
						if (trg == null) return;
						sc.play(p, trg);
						part.play(p, trg);
					}
				}.runTaskLater(NeoRogue.inst(), 1L));
				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				"Passive. Killing an enemy marks the nearest enemy to you. Dealing damage to this enemy deals an additional "
				+ GlossaryTag.PIERCING.tag(this, damage, true) + " to them and removes the mark.");
	}
}
