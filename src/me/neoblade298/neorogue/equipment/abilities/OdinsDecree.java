package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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

public class OdinsDecree extends Equipment {
	private static final String ID = "OdinsDecree";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK)
			.count(100)
			.spread(0.3, 2)
			.offsetY(1)
			.speed(0.3);
	private static final TargetProperties tp = TargetProperties.radius(20, false, TargetType.ENEMY);
	private int damage, electrified;
	private double chance;
	
	public OdinsDecree(boolean isUpgraded) {
		super(ID, "Odin's Decree", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damage = isUpgraded ? 150 : 100;
		electrified = isUpgraded ? 60 : 40;
		chance = isUpgraded ? 1.0 : 0.5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			am.addCount(1);
			// PLAYER_TICK triggers every tick, so 40 ticks = 2 seconds
			if (am.getCount() >= 2) {
				Player p = data.getPlayer();
				am.setCount(0);
				
				// Check chance
				if (Math.random() >= chance) return TriggerResult.keep();
				
				// Find nearest enemy
				LivingEntity target = TargetHelper.getNearest(p, tp);
				if (target == null) return TriggerResult.keep();
				
				// Drop lightning bolt
				pc.play(p, target.getLocation());
				Sounds.thunder.play(p, target.getLocation());
				
				// Deal damage and apply electrified
				FightInstance.dealDamage(pdata, DamageType.LIGHTNING, damage, target, 
						DamageStatTracker.of(id + slot, this));
				FightInstance.applyStatus(target, StatusType.ELECTRIFIED, data, electrified, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				"Passive. Every <white>2s</white>, <yellow>" + (int)(chance * 100) + "%</yellow> " +
				"chance to drop a lightning bolt on the nearest enemy, dealing " + 
				GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and applying " + 
				GlossaryTag.ELECTRIFIED.tag(this, electrified, true) + ".");
	}
}
