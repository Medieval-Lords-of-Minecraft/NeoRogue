package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
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

public class Crystallize extends Equipment {
	private static final String ID = "Crystallize";
	private static final TargetProperties tp = TargetProperties.radius(8, false, TargetType.ENEMY),
		aoe = TargetProperties.radius(8, false, TargetType.ENEMY);
	private static final Circle circ = new Circle(8);
	private int thres, frost;
	private static final ParticleContainer pc = new ParticleContainer(Particle.SNOWFLAKE).count(30).spread(1, 1).offsetY(1),
		fill = new ParticleContainer(Particle.BLOCK).blockData(Material.ICE.createBlockData()),
		edges = new ParticleContainer(Particle.FIREWORK);
	
	public Crystallize(boolean isUpgraded) {
		super(ID, "Crystallize", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(35, 0, 20, tp.range).add(PropertyType.AREA_OF_EFFECT, aoe.range));
		thres = isUpgraded ? 240 : 160;
		frost = isUpgraded ? 120 : 80;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			LivingEntity trg = TargetHelper.getNearest(p, tp);
			circ.play(p, edges, trg.getLocation(), LocalAxes.xz(), fill);
			Sounds.glass.play(p, trg.getLocation());
			DamageMeta dm = new DamageMeta(data);
			dm.addDamageSlice(new DamageSlice(data, thres, DamageType.ICE, true, DamageStatTracker.of(ID + slot, this)));
			FightInstance.dealDamage(dm, trg);
			pc.play(p, trg);
			for (LivingEntity le : TargetHelper.getEntitiesInRadius(p, aoe)) {
				FightInstance.applyStatus(le, StatusType.FROST, data, frost, -1);
			}
			return TriggerResult.keep();
		}), (p2, data2, in) -> {
			LivingEntity trg = TargetHelper.getNearest(p, tp);
			if (trg == null) return false;
			return trg.getHealth() <= thres;
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD,
				"On cast, if the enemy you're looking at has at most " + DescUtil.yellow(thres) + " HP, instantly kill them " +
				"and apply " + GlossaryTag.FROST.tag(this, frost, true) + " to nearby enemies.");
	}
}
