package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
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
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class Overload extends Equipment implements Power {
	private static final String ID = "Overload";
	private int damage, mult;
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORK)
			.count(50).spread(0.2, 3).offsetY(2);
	private static final TargetProperties tp = TargetProperties.radius(3, false, TargetType.ENEMY);
	
	public Overload(boolean isUpgraded) {
		super(ID, "Overload", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, tp.range));
		
		damage = isUpgraded ? 90 : 60;
		mult = 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
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
		data.addTrigger(id, Trigger.KILL, (pd, in2) -> {
			KillEvent killEv = (KillEvent) in2;
			int stacks = FightInstance.getFightData(killEv.getTarget()).getStatus(StatusType.ELECTRIFIED).getStacks();
			Player p2 = data.getPlayer();
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(killEv.getTarget(), tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage + (stacks * mult), DamageType.LIGHTNING, DamageStatTracker.of(id + slot, this)), ent);
				part.play(p2, ent);
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.GLOWSTONE_DUST,
				GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.ELECTRIFIED.tag(this) + " " + DescUtil.white(5) + " times. On kill, deal " + GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage + the number of "
				+ GlossaryTag.ELECTRIFIED.tag(this) + " stacks the killed enemy has multiplied by " + DescUtil.white(mult) + " in an area.");
	}
}
