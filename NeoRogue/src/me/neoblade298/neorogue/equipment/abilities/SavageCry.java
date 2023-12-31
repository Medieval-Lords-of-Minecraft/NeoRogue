package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SavageCry extends Equipment {
	private static final TargetProperties tp = new TargetProperties(15, true, TargetType.ENEMY);
	private int threat;
	private ParticleContainer taunt = new ParticleContainer(Particle.VILLAGER_ANGRY);
	
	public SavageCry(boolean isUpgraded) {
		super("savageCry", "Savage Cry", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 60, 5, tp.range));
		threat = isUpgraded ? 1500 : 1000;
		taunt.count(50).spread(0.5, 0.5).speed(0.2);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(this, (pd, in) -> {
			Util.playSound(p, Sound.ENTITY_ENDER_DRAGON_AMBIENT, false);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				NeoRogue.mythicApi.addThreat(p, ent, threat);
				taunt.spawn(ent);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, threaten all enemies around you for <yellow>" + threat + "</yellow>.");
	}
}
