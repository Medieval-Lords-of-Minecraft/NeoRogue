package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.EvadeEvent;

public class AbyssalCarve extends Equipment {
	private static final String ID = "AbyssalCarve";
	private static final TargetProperties tp = TargetProperties.cone(90, 7, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(7, 90);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
		.dustOptions(new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(75, 0, 130), 1.5F))
		.count(1).spread(0, 0);
	private static final ParticleContainer fill = pc.clone().count(1).spread(0.1, 0);
	private int damage;
	
	public AbyssalCarve(boolean isUpgraded) {
		super(ID, "Abyssal Carve", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damage = isUpgraded ? 200 : 150;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			Player p = data.getPlayer();
			EvadeEvent ev = (EvadeEvent) in;
			
			// Get the damager entity from the DamageMeta
			if (ev.getDamageMeta() == null || ev.getDamageMeta().getOwner() == null) {
				return TriggerResult.keep();
			}
			
			LivingEntity damager = ev.getDamageMeta().getOwner().getEntity();
			Location playerLoc = p.getLocation();
			Location damagerLoc = damager.getLocation();

			// Calculate direction toward the attacker
			Vector towardEnemy = damagerLoc.toVector().subtract(playerLoc.toVector()).normalize();
			
			// Dash away from the enemy
			Vector awayFromEnemy = towardEnemy.clone().multiply(-1);
			data.dash(awayFromEnemy);
			
			// Create cone in direction of attacker
			cone.play(p, pc, playerLoc.add(0, 1, 0), LocalAxes.usingEyeLocation(p), fill);
			
			// Deal damage to enemies in cone
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, towardEnemy, tp)) {
				FightInstance.dealDamage(pdata, DamageType.DARK, damage, ent, 
						DamageStatTracker.of(id + slot, this));
			}
			Sounds.attackSweep.play(p, p);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SWORD,
				"Passive. Upon " + GlossaryTag.EVADE.tag(this) + ", deal " + 
				GlossaryTag.DARK.tag(this, damage, true) + " damage in a cone toward the attacker and " + 
				GlossaryTag.DASH.tag(this) + " away from them.");
	}
}
