package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

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
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class CripplingPoison extends Equipment {
	private static final String ID = "cripplingPoison";
	private int reduc, poisonThreshold;
	private static final ParticleContainer part = new ParticleContainer(Particle.CRIT).offsetForward(2).count(10).spread(2.5, 0.2);
	private static final TargetProperties tp = TargetProperties.cone(90, 5, false, TargetType.ENEMY);
	
	public CripplingPoison(boolean isUpgraded) {
		super(ID, "Crippling Poison", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 20, 15, tp.range));
		
		reduc = 8;
		poisonThreshold = isUpgraded ? 7 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pd, in) -> {
			Sounds.attackSweep.play(p, p);
			part.play(p, p);
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
				FightData fd = FightInstance.getFightData(ent);
				int add = fd.getStatus(StatusType.POISON).getStacks() / poisonThreshold;
				fd.addBuff(data, UUID.randomUUID().toString(), true, false, BuffType.PHYSICAL, -reduc - add, 10);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARMOR_STAND,
				"On cast, reduce the " + GlossaryTag.PHYSICAL.tag(this) + " damage of enemies in a cone in front of you by <yellow>" + reduc + "</yellow>"
						+ " for <white>10</white> seconds.");
	}
}
