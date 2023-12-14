package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SavageCry extends Ability {
	private static final TargetProperties tp = new TargetProperties(15, true, TargetType.ENEMY);
	private int threat;
	private ParticleContainer taunt = new ParticleContainer(Particle.VILLAGER_ANGRY);
	private static BukkitAPIHelper api = MythicBukkit.inst().getAPIHelper();
	
	public SavageCry(boolean isUpgraded) {
		super("savageCry", "Savage Cry", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR);
		setBaseProperties(5, 0, 60, (int) tp.range);
		threat = isUpgraded ? 40000 : 25000;
		item = createItem(this, Material.FLINT, null,
				"On cast, threaten all enemies around you for <yellow>" + threat + "</yellow>.");
		taunt.count(50).spread(0.5, 0.5).speed(0.2);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, bind, (pd, in) -> {
			Util.playSound(p, Sound.ENTITY_ENDER_DRAGON_AMBIENT, false);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				api.addThreat(p, ent, threat);
				taunt.spawn(ent);
			}
			return TriggerResult.keep();
		});
	}
}
