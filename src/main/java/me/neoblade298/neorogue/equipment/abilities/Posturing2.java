package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class Posturing2 extends Equipment implements Power {
	private static final String ID = "Posturing2";
	private int time, inc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(1, 1).offsetY(1);
	
	public Posturing2(boolean isUpgraded) {
		super(ID, "Posturing II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		time = 6;
		inc = isUpgraded ? 8 : 6;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FOCUS)) return TriggerResult.keep();
			if (count.addCount(ev.getStacks()) < 2) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		act.setAction((pdata2, in2) -> {
			Player p2 = data.getPlayer();
			if (!p2.isSneaking()) return TriggerResult.keep();
			act.addCount(1);
			if (act.getCount() >= time) {
				pc.play(p2, p2);
				Sounds.enchant.play(p2, p2);
				data.applyStatus(StatusType.FOCUS, data, 1, -1, this);
				act.addCount(-time);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PLAYER_TICK, act);

		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata3, in3) -> {
			Player p3 = data.getPlayer();
			LaunchProjectileGroupEvent ev3 = (LaunchProjectileGroupEvent) in3;
			for (ProjectileInstance inst : ev3.getInstances()) {
				double damage = inc * Math.min(4, data.getStatus(StatusType.FOCUS).getStacks());
				inst.getMeta().addDamageSlice(new DamageSlice(data, p3.isSneaking() ? damage * 2 : damage, DamageType.PIERCING, DamageStatTracker.of(ID + slot, this)));
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.SCAFFOLDING,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after receiving " + DescUtil.white(2) + " " + GlossaryTag.FOCUS.tag(this) + " stacks. Every " + DescUtil.yellow(time +"s") + " spent crouched during a fight, gain " + GlossaryTag.FOCUS.tag(this, 1, false) + 
				". Projectiles fired deal an additional " + GlossaryTag.PIERCING.tag(this, inc, true) + " damage per stack of " + GlossaryTag.FOCUS.tag(this) +
				", up to " + DescUtil.white(4) + ". This bonus is doubled if the projectile is fired while crouching.");
	}
}
