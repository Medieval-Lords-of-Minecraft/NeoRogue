package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Prayer extends Equipment {
	private static final TargetProperties tp = TargetProperties.radius(15, false, TargetType.ENEMY);
	private static final ParticleContainer healPart = new ParticleContainer(Particle.VILLAGER_HAPPY).count(25).spread(0.5, 0.5);
	private int heal;
	private double healPct;
	
	public Prayer(boolean isUpgraded) {
		super("prayer", "Prayer", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 10, 25, tp.range));
		heal = isUpgraded ? 15 : 10;
		healPct = heal * 0.01;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pd, in) -> {
			double total = 0;
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				FightData fd = FightInstance.getFightData(ent.getUniqueId());
				if (!fd.hasStatus(StatusType.SANCTIFIED)) continue;
				total += fd.getStatus(StatusType.SANCTIFIED).getStacks();
			}
			
			total /= data.getInstance().getParty().size();
			total *= healPct;
			for (UUID uuid : data.getInstance().getParty()) {
				Player pl = Bukkit.getPlayer(uuid);
				if (pl == null) continue;
				FightInstance.getUserData(uuid).addHealth(total);
				Sounds.levelup.play(p, pl);
				healPart.play(p, pl);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				"On cast, add up " + GlossaryTag.SANCTIFIED.tag(this) + " stacks of all enemies in range, and heal"
				+ " the party for <yellow>" + heal + "%</yellow> of all stacks, split evenly amonst party members.");
	}
}
