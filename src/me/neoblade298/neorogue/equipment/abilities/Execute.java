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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Execute extends Equipment {
	private static final String ID = "execute";
	private int damage, strength;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.DUST);
	
	public Execute(boolean isUpgraded) {
		super(ID, "Execute", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 7, 0));
		damage = isUpgraded ? 180 : 120;
		strength = isUpgraded ? 15 : 10;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}

	@Override
	public void setupReforges() {
		addReforge(BasicInfusionMastery.get(), SiphoningStrike.get());
		addReforge(EnduranceTraining.get(), Fortify.get(), MightySwing.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.enchant.play(p, p);
			pc.play(p, p);
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in) -> {
				if (p.isOnGround()) return TriggerResult.keep();
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				FightInstance.dealDamage(data, DamageType.PIERCING, damage, ev.getTarget(), DamageStatTracker.of(id + slot, this));
				Sounds.anvil.play(p, ev.getTarget());
				hit.play(p, ev.getTarget());
				if (ev.getTarget().getHealth() <= 0) {
					Sounds.success.play(p, p);
					data.applyStatus(StatusType.STRENGTH, data, strength, -1);
				}
				
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SKULL_POTTERY_SHERD,
				"On cast, your next basic attack while in the air deals <yellow>" + damage + "</yellow> " + GlossaryTag.PIERCING.tag(this) + " damage. If the enemy is"
						+ " killed by this attack, gain <yellow>" + strength + "</yellow> " + GlossaryTag.STRENGTH.tag(this) + ".");
	}
}
