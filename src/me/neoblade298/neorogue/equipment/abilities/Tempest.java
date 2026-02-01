package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Tempest extends Equipment {
	private static final String ID = "Tempest";
	private int damage, electrified;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(50)
			.spread(0.5, 2)
			.offsetY(1)
			.speed(0.2);
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	
	public Tempest(boolean isUpgraded) {
		super(ID, "Tempest", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0, 5));
		damage = isUpgraded ? 600 : 400;
		electrified = isUpgraded ? 500 : 300;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			
			// Only trigger when dealing electrified status damage
			if (!ev.getMeta().containsType(DamageType.ELECTRIFIED)) return TriggerResult.keep();
			
			LivingEntity originalTarget = ev.getTarget();
			Location targetLoc = originalTarget.getLocation();
			
			// Visual lightning bolt effect
			Player p = data.getPlayer();
			pc.play(p, targetLoc);
			Sounds.levelup.play(p, targetLoc);
			
			// Deal damage and apply electrified to nearby enemies (excluding the original target)
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, targetLoc, tp)) {
				if (ent.getUniqueId().equals(originalTarget.getUniqueId())) continue; // Skip original target
				
				FightInstance.dealDamage(pdata, DamageType.LIGHTNING, damage, ent, DamageStatTracker.of(id + slot, this));
				FightInstance.applyStatus(ent, StatusType.ELECTRIFIED, data, electrified, -1);
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.END_ROD,
				"Passive. Whenever you deal damage with " + GlossaryTag.ELECTRIFIED.tag(this) + ", drop a lightning bolt " +
				"onto the target and deal " + GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and apply " +
				GlossaryTag.ELECTRIFIED.tag(this, electrified, true) + " to enemies in the radius, except the original target.");
	}
}
