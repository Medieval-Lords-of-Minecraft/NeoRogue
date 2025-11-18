package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
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

public class Condemn extends Equipment {
	private static final String ID = "Condemn";
	private static final ParticleContainer lancePart = new ParticleContainer(Particle.ELECTRIC_SPARK).count(5).spread(0.1, 0.1);
	private static final TargetProperties tp = TargetProperties.line(6, 1, TargetType.ENEMY);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.5F);
	private int damage, bonus, multStr;
	private double mult;

	public Condemn(boolean isUpgraded) {
		super(
				ID, "Condemn", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofUsable(0, 50, 8, tp.range)
		);
		damage = 200;
		bonus = 300;
		mult = isUpgraded ? 0.6 : 0.3;
		multStr = (int) (mult * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			data.charge(20).then(new Runnable() {
				public void run() {
					LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInSight(p, tp);
					sc.play(p, p);
					Location start = p.getLocation().add(0, 1, 0);
					Vector v = p.getLocation().getDirection().setY(0).normalize().multiply(tp.range);
					ParticleUtil.drawLine(p, lancePart, p.getLocation().add(0, 1, 0), start.clone().add(v), 0.5);
					Block b = p.getTargetBlockExact((int) tp.range);
					if (b != null) {
						Sounds.explode.play(p, p);
					}
					DamageStatTracker tracker = DamageStatTracker.of(id + slot, eq);
					for (LivingEntity target : targets) {
						DamageMeta dm = new DamageMeta(data, damage, DamageType.PIERCING, tracker).setKnockback(0.5);
						if (b != null) {
							dm.addDamageSlice(new DamageSlice(data, bonus, DamageType.PIERCING, tracker));
							target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
							FightData fd = FightInstance.getFightData(target);
							fd.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.multiplier(data, -mult,
									BuffStatTracker.defenseDebuffEnemy(id + slot, eq, true)), 100);
						}
					FightInstance.dealDamage(dm, target);
					}
				}
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.POINTED_DRIPSTONE,
				"On cast, " + DescUtil.charge(this, 1, 1) + ", then deal " + GlossaryTag.PIERCING.tag(this, damage, true) + " and knock back enemies in a line. " +
				"If the line includes a wall, deal " + DescUtil.yellow(bonus) + " bonus damage to all enemies hit, apply " + DescUtil.potion("Slowness", 2, 2) +
				", and reduce their " + GlossaryTag.PHYSICAL.tag(this) + " defense by " + DescUtil.yellow(multStr + "%") + " for <white>5s</white>.");
	}
}
