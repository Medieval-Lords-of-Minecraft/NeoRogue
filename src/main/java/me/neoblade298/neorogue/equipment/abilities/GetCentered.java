package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class GetCentered extends Equipment implements Power {
	private static final String ID = "GetCentered";
	private int thres, damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(1, 1);
	
	public GetCentered(boolean isUpgraded) {
		super(ID, "Get Centered", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		thres = isUpgraded ? 6 : 8;
		damage = isUpgraded ? 15 : 12;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBowProjectile()) return TriggerResult.keep();
			BowProjectile bp = (BowProjectile) ev.getInstances().getFirst().getParent();
			if (bp.getInitialVelocity() < 2.9) return TriggerResult.keep();
			if (count.addCount(1) < 2) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		String buffId = UUID.randomUUID().toString();
		StandardPriorityAction inst = new StandardPriorityAction(id);
		inst.setAction((pdata2, in2) -> {
			Player p2 = data.getPlayer();
			LaunchProjectileGroupEvent ev2 = (LaunchProjectileGroupEvent) in2;
			if (!ev2.isBowProjectile()) return TriggerResult.keep();
			BowProjectile bp2 = (BowProjectile) ev2.getInstances().getFirst().getParent();
			if (bp2.getInitialVelocity() < 2.9) return TriggerResult.keep();

			inst.addCount(1);
			if (inst.getCount() >= thres) {
				inst.addCount(-thres);
				data.applyStatus(StatusType.FOCUS, data, 1, -1);
				pc.play(p2, p2);
				Sounds.enchant.play(p2, p2);
			}

			for (IProjectileInstance pi : ev2.getInstances()) {
				((ProjectileInstance) pi).getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					new Buff(data, damage * data.getStatus(StatusType.FOCUS).getStacks(), 0, StatTracker.damageBuffAlly(buffId, GetCentered.this)));
			}
			return TriggerResult.keep();
		});
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.LAUNCH_PROJECTILE_GROUP, inst);
			}
		}.runTask(NeoRogue.inst()));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.BOOK,
				GlossaryTag.POWER.tag(this) + ". Activates after firing " + DescUtil.white(2) + " max draw projectiles. Every " + DescUtil.yellow(thres) + " shots fired at max draw grants you " + GlossaryTag.FOCUS.tag(this, 1, false) + 
				". Basic attack damage at max draw is increased by " + DescUtil.yellow(damage) + " per stack of " + GlossaryTag.FOCUS.tag(this) + ".");
	}
}
