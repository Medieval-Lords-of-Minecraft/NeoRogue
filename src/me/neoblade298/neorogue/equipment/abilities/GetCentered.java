package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class GetCentered extends Equipment {
	private static final String ID = "getCentered";
	private int thres, damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANTMENT_TABLE).count(25).spread(1, 1);
	
	public GetCentered(boolean isUpgraded) {
		super(ID, "Get Centered", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 0));
		thres = isUpgraded ? 10 : 7;
		damage = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(id);
		inst.setAction((pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBowProjectile()) return TriggerResult.keep();
			BowProjectile bp = (BowProjectile) ev.getInstances().getFirst().getParent();
			if (bp.getInitialVelocity() < 3)  return TriggerResult.keep();
			
			inst.addCount(1);
			if (inst.getCount() >= thres) {
				inst.addCount(-thres);
				data.applyStatus(StatusType.FOCUS, data, 1, -1);
				pc.play(p, p);
				Sounds.enchant.play(p, p);
			}

			for (IProjectileInstance pi : ev.getInstances()) {
				((ProjectileInstance) pi).getMeta().addBuff(BuffType.GENERAL, new Buff(data, damage * data.getStatus(StatusType.FOCUS).getStacks(),
					0), BuffOrigin.PROJECTILE, true);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.LAUNCH_PROJECTILE_GROUP, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK,
				"Passive. Every " + DescUtil.yellow(thres) + " shots fired at max draw grants you " + GlossaryTag.FOCUS.tag(this, 1, false) + 
				". Basic attack damage at max draw is increased by " + DescUtil.yellow(damage) + " per stack of " + GlossaryTag.FOCUS.tag(this) + ".");
	}
}
