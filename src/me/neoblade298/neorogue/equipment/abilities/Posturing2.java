package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class Posturing2 extends Equipment {
	private static final String ID = "Posturing2";
	private int time, inc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(1, 1).offsetY(1);
	
	public Posturing2(boolean isUpgraded) {
		super(ID, "Posturing II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		time = 6;
		inc = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		act.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			if (!p.isSneaking()) return TriggerResult.keep();
			act.addCount(1);
			if (act.getCount() >= time) {
				pc.play(p, p);
				Sounds.enchant.play(p, p);
				data.applyStatus(StatusType.FOCUS, data, 1, -1);
				act.addCount(-time);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PLAYER_TICK, act);

		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			Player p = data.getPlayer();
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			for (IProjectileInstance ipi : ev.getInstances()) {
				if (ipi instanceof ProjectileInstance) {
					ProjectileInstance inst = (ProjectileInstance) ipi;
					double damage = inc * Math.min(4, data.getStatus(StatusType.FOCUS).getStacks());
					inst.getMeta().addDamageSlice(new DamageSlice(data, p.isSneaking() ? damage * 2 : damage, DamageType.PIERCING, DamageStatTracker.of(ID + slot, this)));
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SCAFFOLDING,
				"Passive. Every " + DescUtil.yellow(time +"s") + " spent crouched during a fight, gain " + GlossaryTag.FOCUS.tag(this, 1, false) + 
				". Projectiles fired deal an additional " + GlossaryTag.PIERCING.tag(this, inc, true) + " damage per stack of " + GlossaryTag.FOCUS.tag(this) +
				", up to <white>4</white>. This bonus is doubled if the projectile is fired while crouching.");
	}
}
