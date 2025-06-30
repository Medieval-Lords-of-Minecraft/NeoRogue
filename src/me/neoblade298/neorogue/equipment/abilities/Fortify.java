package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Fortify extends Equipment {
	private static final String ID = "fortify";
	private int damage, fortitude;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.DUST);
	
	public Fortify(boolean isUpgraded) {
		super(ID, "Fortify", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 30, isUpgraded ? 5 : 7, 0));
		damage = 100;
		fortitude = isUpgraded ? 2 : 1;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.equip.play(p, p);
			pc.play(p, p);
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in) -> {
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				Sounds.anvil.play(p, ev.getTarget());
				hit.play(p, ev.getTarget());
				FightInstance.dealDamage(data, DamageType.PIERCING, damage, ev.getTarget(), DamageStatTracker.of(id + slot, this));
				data.applyStatus(Status.createByGenericType(GenericStatusType.BASIC, "Fortitude", data), data, fortitude, -1);
				data.addSimpleShield(p.getUniqueId(), data.getStatus("Fortitude").getStacks(), 40);
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				"On cast, your next basic attack while in the air deals <white>" + damage + " </white>" + GlossaryTag.PIERCING.tag(this) + " damage, "
								+ "gain <yellow>" + fortitude + "</yellow> stacks of fortitude, a shield that lasts <white>2</white> seconds with "
						+ "the number of stacks of fortitude you have.");
	}
}
