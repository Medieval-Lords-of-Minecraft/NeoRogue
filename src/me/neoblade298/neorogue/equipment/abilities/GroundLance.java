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
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class GroundLance extends Equipment {
	private static final String ID = "groundLance";
	private static final TargetProperties tp = TargetProperties.radius(2, false);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			grnd = new ParticleContainer(Particle.DUST).count(20).spread(0.5, 0.5).dustOptions(new DustOptions(Color.fromRGB(139, 69, 19), 1F));
	private static final Circle circ = new Circle(tp.range);
	private int damage;
	
	public GroundLance(boolean isUpgraded) {
		super(ID, "Ground Lance", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(40, 0, 10, 14, tp.range));
				damage = isUpgraded ? 150 : 120;
	}
	
	@Override
	public void setupReforges() {

	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			circ.play(pc, p.getTargetBlockExact((int) properties.get(PropertyType.RANGE)).getLocation().add(0, 1, 0), LocalAxes.xz(), null);

			data.charge(20).then(new Runnable() {
				public void run() {
					Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
					if (b.getType().isAir()) {
						data.addMana(properties.get(PropertyType.MANA_COST));
						data.addMana(properties.get(PropertyType.STAMINA_COST));
						inst.setCooldown(0);
						Sounds.error.play(p, p);
						return;
					}

					Location loc = b.getLocation().add(0, 1, 0);
					circ.play(pc, loc, LocalAxes.xz(), null);
					ParticleUtil.drawLine(p, grnd, loc, loc.clone().add(0, 4, 0), 1);
					grnd.play(p, loc);
					Sounds.explode.play(p, loc);
					LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInRadius(p, tp);
					if (targets.size() == 1) {
						Sounds.wither.play(p, loc);
						FightInstance.dealDamage(new DamageMeta(data, damage * 3, DamageType.EARTHEN), targets.getFirst());
					}
					else {
						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
							FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN), ent);
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
				"On cast, " + GlossaryTag.CHARGE.tag(this) + " for <white>1s</white> before dealing " + GlossaryTag.EARTHEN.tag(this, damage, true) + 
				" in an area around the block you aim at. Deal triple damage if you hit exactly one target.");
	}
}
