package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class Overload extends Equipment {
	private static final String ID = "overload";
	private int damage;
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORKS_SPARK)
			.count(50).spread(0.2, 3).offsetY(2);
	private static final TargetProperties tp = TargetProperties.radius(3, false, TargetType.ENEMY);
	
	public Overload(boolean isUpgraded) {
		super(ID, "Overload", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		
		damage = isUpgraded ? 40 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.KILL, new EquipmentInstance(p, this, slot, es, (pd, in) -> {
			KillEvent ev = (KillEvent) in;
			int stacks = FightInstance.getFightData(ev.getTarget()).getStatus(StatusType.ELECTRIFIED).getStacks();
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(ev.getTarget(), tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage + (stacks * 5), DamageType.LIGHTNING), ent);
				part.play(p, ent);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLOWSTONE_DUST,
				"On kill, deal " + GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage + the number of "
				+ GlossaryTag.ELECTRIFIED.tag(this) + " stacks the killed enemy has multiplied by <white>5</white>.");
	}
}
