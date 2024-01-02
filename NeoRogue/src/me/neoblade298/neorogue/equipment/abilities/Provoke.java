package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
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

public class Provoke extends Equipment {
	private static final TargetProperties tp = new TargetProperties(15, false, TargetType.ENEMY);
	private int threat;
	private ParticleContainer taunt = new ParticleContainer(Particle.VILLAGER_ANGRY);
	private static BukkitAPIHelper api = MythicBukkit.inst().getAPIHelper();
	
	public Provoke(boolean isUpgraded) {
		super("provoke", "Provoke", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 20, isUpgraded ? 5 : 10, tp.range));
		threat = 500;
		taunt.count(50).spread(0.5, 0.5).speed(0.2);
		addReforgeOption("provoke", "savageCry", "glare");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(this, (pd, in) -> {
			for (LivingEntity ent : TargetHelper.getEntitiesInSight(p, tp)) {
				api.addThreat(p, ent, threat);
				taunt.spawn(ent);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, threaten all enemies you're looking at for <yellow>" + threat + "</yellow>.");
	}
}
