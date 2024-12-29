package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Windcall extends Equipment {
	private static final String ID = "windcall";
	private int reduc, dur;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final TargetProperties tp = TargetProperties.cone(75, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	
	public Windcall(boolean isUpgraded) {
		super(ID, "Windcall", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, tp.range));
		reduc = isUpgraded ? 15 : 10;
		dur = isUpgraded ? 7 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.attackSweep.play(p, p);
			cone.play(p, pc, p.getLocation(), LocalAxes.usingGroundedEyeLocation(p), pc);
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
				FightData fd = FightInstance.getFightData(ent);
				if (ent.getLocation().distanceSquared(p.getLocation()) <= 3 * 3) {
					fd.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
						Buff.increase(data, -reduc, StatTracker.damageDebuffEnemy(this)), dur);
				}
				FightInstance.knockback(p, ent, 1);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WIND_CHARGE,
				"On cast, knock back nearby enemies in a cone in front of you. Enemies within <white>3</white> blocks of you have their damage reduced by "
				+ DescUtil.yellow(reduc) + " " + DescUtil.duration(dur, false) + ".");
	}
}
