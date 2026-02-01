package me.neoblade298.neorogue.equipment.offhands;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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

public class PalmBlast extends Equipment {
	private static final String ID = "PalmBlast";
	private static final TargetProperties tp = TargetProperties.cone(30, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).offsetY(0.3), 
		expl = new ParticleContainer(Particle.EXPLOSION).offsetY(0.3).count(2).spread(0.2, 0.2);
	private int damage, burn;

	public PalmBlast(boolean isUpgraded) {
		super(ID, "Palm Blast", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(30, 0, 16, tp.range));
		damage = isUpgraded ? 450 : 300;
		burn = isUpgraded ? 150 : 100;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Player p = data.getPlayer();
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			data.charge(20, 2).then(new Runnable() {
				public void run() {
					Sounds.fire.play(p, p);
					Sounds.explode.play(p, p);
					expl.play(p, p);
					cone.play(pc, p.getLocation(), LocalAxes.usingEyeLocation(p), pc);
					LinkedList<LivingEntity> trgs = TargetHelper.getEntitiesInCone(p, tp);
					Vector v = p.getEyeLocation().getDirection();
					p.setVelocity(v.setX(-v.getX()).setZ(-v.getZ()).setY(0).normalize().setY(0.3));
					for (LivingEntity ent : trgs) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, eq)), ent);
						FightInstance.applyStatus(ent, StatusType.BURN, data, burn, -1);
					}
				}
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT_AND_STEEL, "On cast, " + DescUtil.charge(this, 2, 1) + " before dealing " + GlossaryTag.FIRE.tag(this, damage, true)
				+ " damage and applying " + GlossaryTag.BURN.tag(this, burn, true) + " to all enemies in a thin cone in front of you.");
	}
}
