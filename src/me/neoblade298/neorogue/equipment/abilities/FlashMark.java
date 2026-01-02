package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

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

public class FlashMark extends Equipment {
	private static final String ID = "FlashMark";
	private static final TargetProperties tp = TargetProperties.line(30, 1.5, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.ELECTRIC_SPARK).count(50).spread(0.3, 0.3);
	private int damage, electrified;
	
	public FlashMark(boolean isUpgraded) {
		super(ID, "Flash Mark", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 0, 8, 30));
		damage = isUpgraded ? 250 : 180;
		electrified = isUpgraded ? 150 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
			
			// Check if block exists and is solid
			if (b == null || !b.getType().isSolid()) {
				Sounds.extinguish.play(p, p);
				data.addMana(properties.get(PropertyType.MANA_COST));
				inst.setCooldown(0);
				return TriggerResult.keep();
			}
			
			// Get the target location and prepare for dash
			Location targetLoc = b.getLocation();
			Location start = p.getLocation().add(0, 1, 0);
			Location end = targetLoc.clone().add(0, 1, 0);
			
			// Draw particle line
			ParticleUtil.drawLine(p, pc, start, end, 0.5);
			Sounds.firework.play(p, p);
			
			// Deal damage and apply electrified to all enemies in line
			for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING, DamageStatTracker.of(id + slot, this)), ent);
				FightInstance.applyStatus(ent, StatusType.ELECTRIFIED, data, electrified, -1);
			}
			
			// Dash
			Location playerLoc = p.getLocation();
			playerLoc.setDirection(end.toVector().subtract(start.toVector()).normalize());
			p.teleport(playerLoc);
			data.dash(end.toVector().subtract(start.toVector()).normalize());
			
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				"On cast, throw a projectile. If it hits a block, " + GlossaryTag.DASH.tag(this) + " in that direction, " +
				"dealing " + GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and applying " + 
				GlossaryTag.ELECTRIFIED.tag(this, electrified, true) + " to enemies in a line between you and the block.");
	}
}
