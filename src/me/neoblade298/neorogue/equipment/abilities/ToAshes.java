package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageBarrierEvent;

public class ToAshes extends Equipment {
	private static final String ID = "toAshes";
	private static final TargetProperties tp = TargetProperties.cone(60, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private int damage, selfDmg = 3, inc;
	
	public ToAshes(boolean isUpgraded) {
		super(ID, "ToAshes", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 0, 12, tp.range));
				damage = 300;
				inc = isUpgraded ? 100 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		ItemStack icon = item.clone();
		EquipmentInstance eqi = new EquipmentInstance(data, this, slot, es,(pdata, in) -> {
			Sounds.fire.play(p, p);
			cone.play(pc, p.getLocation(), LocalAxes.usingEyeLocation(p), null);
			LinkedList<LivingEntity> trgs = TargetHelper.getEntitiesInCone(p, tp);
			for (LivingEntity ent : trgs) {
				FightInstance.dealDamage(new DamageMeta(data, damage + (inc * am.getCount()), DamageType.FIRE), ent);
			}
			FightInstance.dealDamage(new DamageMeta(data, selfDmg, DamageType.FIRE), p);

			
			Barrier b = Barrier.stationary(p, 4, tp.range, 3, p.getLocation(), LocalAxes.usingEyeLocation(p), null, null, true);
			am.setUniqueId(b.getUniqueId());
			data.addBarrier(b);
			data.addTask(new BukkitRunnable() {
				public void run() {
					am.setUniqueId(null);
					data.removeBarrier(b);
				}
			}.runTaskLater(NeoRogue.inst(), 20));
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, eqi);

		data.addTrigger(id, Trigger.RECEIVED_DAMAGE_BARRIER, (pdata, in) -> {
			ReceivedDamageBarrierEvent ev = (ReceivedDamageBarrierEvent) in;
			if (ev.getBarrier().getUniqueId() == am.getUniqueId()) {
				am.setUniqueId(null);
				am.addCount(1);
				icon.setAmount(am.getCount());
				eqi.setIcon(icon);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, deal " + GlossaryTag.FIRE.tag(this, damage, true) + " to all enemies in a cone in front of you, but " +
				"deal " + GlossaryTag.FIRE.tag(this, selfDmg, false) + " to yourself. All projectiles in the cone are destroyed. If you destroy at least <white>1</white>" +
				" projectile, increase the damage of this ability by " + DescUtil.yellow(inc) + " for the rest of the fight.");
	}
}
