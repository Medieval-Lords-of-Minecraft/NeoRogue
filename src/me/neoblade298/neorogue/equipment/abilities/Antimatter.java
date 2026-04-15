package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Antimatter extends Equipment {
	private static final String ID = "Antimatter";
	private static final TargetProperties tp = TargetProperties.line(16, 2, TargetType.ENEMY);
	private static final ParticleContainer bolt = new ParticleContainer(Particle.SOUL)
			.count(30).spread(0.25, 0.25).speed(0.2);
	private int damage;
	
	public Antimatter(boolean isUpgraded) {
		super(ID, "Antimatter", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 5, tp.range));
		damage = isUpgraded ? 180 : 120;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// On cast, consume all mana and fire bolt
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			
			// Consume all mana
			data.setMana(0);
			
			// Fire bolt with consistent damage
			Location start = p.getEyeLocation();
			Vector dir = p.getEyeLocation().getDirection();
			Location end = start.clone().add(dir.clone().multiply(properties.get(PropertyType.RANGE)));
			
			// Draw bolt
			ParticleUtil.drawLine(p, bolt, start, end, 0.3);
			Sounds.teleport.play(p, p);
			
			// Deal damage to all entities in line
			for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK, 
						DamageStatTracker.of(id + slot, eq)), ent);
			}
			
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN,
				"On cast, consume all your current mana and fire a bolt of dark matter dealing " + 
				GlossaryTag.DARK.tag(this, damage, true) + " damage.");
	}
}
