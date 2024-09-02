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
import me.neoblade298.neorogue.session.fight.DamageType;
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

public class Disorient extends Equipment {
	private static final String ID = "disorient";
	private int inc, damage, insanity;
	private static final ParticleContainer part = new ParticleContainer(Particle.PORTAL).offsetForward(2).count(10).spread(2.5, 0.2);
	private static final TargetProperties tp = TargetProperties.cone(90, 5, false, TargetType.ENEMY);
	
	public Disorient(boolean isUpgraded) {
		super(ID, "Disorient", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 0, 15, tp.range));
		
		inc = 21;
		damage = isUpgraded ? 60 : 40;
		insanity = isUpgraded ? 5 : 3;
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
				fd.addBuff(data, UUID.randomUUID().toString(), false, false, BuffType.PHYSICAL, -inc, 200);
				fd.applyStatus(StatusType.INSANITY, data, insanity, -1);
				FightInstance.dealDamage(fd, DamageType.DARK, damage, ent);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARMOR_STAND,
				"On cast, reduce the " + GlossaryTag.PHYSICAL.tag(this) + " damage of enemies in a cone in front of you by <yellow>" + inc + "</yellow>"
						+ " [<white>10s</white>]. Also deal " + GlossaryTag.DARK.tag(this, damage, true) + " damage and apply " +
						GlossaryTag.INSANITY.tag(this, insanity, true) + ".");
	}
}
