package me.neoblade298.neorogue.equipment.offhands;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class TomeOfGravity extends Equipment {
	private static final String ID = "TomeOfGravity";
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);
	private static final ParticleContainer edge = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.fromRGB(50, 0, 50), 1F))
		.count(1).spread(0, 0);
	private static final ParticleContainer fill = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.fromRGB(50, 0, 50), 1F))
		.count(1).spread(0.1, 0);
	private int defenseReduction;

	public TomeOfGravity(boolean isUpgraded) {
		super(ID, "Tome of Gravity", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(15, 0, 12, tp.range));
		defenseReduction = isUpgraded ? 30 : 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.LEFT_CLICK, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			p.swingOffHand();
			Sounds.enchant.play(p, p);
			circ.play(edge, p.getLocation(), LocalAxes.xz(), fill);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				// Apply slowness
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
				
				// Decrease physical defense
				FightData fd = FightInstance.getFightData(ent);
				fd.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL),
						Buff.increase(data, -defenseReduction, BuffStatTracker.defenseDebuffEnemy(buffId, this, false)), 60);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK, "On left click, slow nearby enemies in radius <white>" + (int)tp.range + "</white> with " +
				DescUtil.potion("Slowness", 1, 3) + " and reduce their " + GlossaryTag.PHYSICAL.tag(this) + 
				" defense by <yellow>" + defenseReduction + "</yellow> [<white>3s</white>].");
	}
}
