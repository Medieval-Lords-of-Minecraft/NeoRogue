package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class Gravity extends Equipment {
	private static final String ID = "gravity";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST).dustOptions(new DustOptions(Color.BLACK, 1F));
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);
	private int damage;
	
	public Gravity(boolean isUpgraded) {
		super(ID, "Gravity", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(30, 0, 12, 10));
		damage = isUpgraded ? 300 : 200;
		properties.setCastType(CastType.POST_TRIGGER);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			data.charge(40).then(new Runnable() {
				public void run() {
					Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
					if (b == null) {
						data.addMana(properties.get(PropertyType.MANA_COST));
						inst.setCooldown(0);
						Sounds.error.play(p, p);
						return;
					}

					data.runActions(data, Trigger.CAST_USABLE, new CastUsableEvent(inst, CastType.POST_TRIGGER));
					Location loc = b.getLocation().add(0, 1, 0);
					Sounds.fire.play(p, loc);
					data.addRift(new Rift(data, loc, 160));

					for (Rift rift : data.getRifts().values()) {
						circ.play(pc, rift.getLocation(), LocalAxes.xz(), null);
						LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInRadius(p, rift.getLocation(), tp);
						for (LivingEntity ent : targets) {
							ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
							Vector v = rift.getLocation().toVector().subtract(ent.getLocation().toVector());
							if (v.isZero()) continue;
							v = v.setY(0).normalize().setY(0.3);
							FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK, DamageStatTracker.of(id + slot, eq)), ent);
							FightInstance.knockback(ent, v);
						}
					}
				}
			});
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COAL, "On cast, " + DescUtil.charge(this, 1, 2) + " before dropping a " +  GlossaryTag.RIFT.tag(this) + " [<white>8s</white>] on the block you aim at. " +
				"Then, deal " + GlossaryTag.DARK.tag(this, damage, true) + " damage, pull in, and apply " + DescUtil.potion("Slowness", 1, 2) + " to " +
				"nearby enemies of all your " + GlossaryTag.RIFT.tagPlural(this) + ".");
	}
}
