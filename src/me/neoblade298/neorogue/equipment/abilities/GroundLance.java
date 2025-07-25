package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
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
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class GroundLance extends Equipment {
	private static final String ID = "groundLance";
	private static final TargetProperties tp = TargetProperties.radius(2, true);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			grnd = new ParticleContainer(Particle.DUST).count(20).spread(0.5, 0.5)
					.dustOptions(new DustOptions(Color.fromRGB(139, 69, 19), 1F));
	private static final Circle circ = new Circle(tp.range);
	private int damage;

	public GroundLance(boolean isUpgraded) {
		super(ID, "Ground Lance", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 10, 10, 14, tp.range));
		damage = isUpgraded ? 150 : 120;
		properties.setCastType(CastType.POST_TRIGGER);
	}

	@Override
	public void setupReforges() {

	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
			if (b == null) {
				data.addMana(properties.get(PropertyType.MANA_COST));
				inst.setCooldown(0);
				Sounds.error.play(p, p);
				return TriggerResult.keep();
			}
			circ.play(pc, b.getLocation().add(0, 1, 0), LocalAxes.xz(), null);

			data.charge(20).then(new Runnable() {
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
					circ.play(pc, loc, LocalAxes.xz(), null);
					ParticleUtil.drawLine(p, grnd, loc, loc.clone().add(0, 4, 0), 1);
					grnd.play(p, loc);
					Sounds.explode.play(p, loc);
					LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInRadius(p, loc, tp);
					if (targets.size() == 1) {
						Sounds.wither.play(p, loc);
						FightInstance.dealDamage(new DamageMeta(data, damage * 3, DamageType.EARTHEN, DamageStatTracker.of(id + slot, eq)),
								targets.getFirst());
					} else {
						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
							FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN,
									DamageStatTracker.of(id + slot, eq)), ent);
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
		item = createItem(Material.POINTED_DRIPSTONE,
				"On cast, " + DescUtil.charge(this, 1, 1) + " before dealing "
						+ GlossaryTag.EARTHEN.tag(this, damage, true)
						+ " in an area around the block you aim at. Deal triple damage if you hit exactly one target.");
	}
}
