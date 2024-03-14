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
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Embolden extends Equipment {
	private static final String ID = "embolden";
	private int damage, shields;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.REDSTONE);
	
	public Embolden(boolean isUpgraded) {
		super(ID, "Embolden", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 20, 10, 0));
		damage = 105;
		shields = isUpgraded ? 5 : 3;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es,
				(pdata, in) -> {
			Sounds.equip.play(p, p);
			data.addPermanentShield(p.getUniqueId(), shields);
			pc.play(p, p);
			pdata.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in2;
				FightInstance.dealDamage(data, DamageType.SLASHING, damage, ev.getTarget());
				hit.play(p, ev.getTarget().getLocation());
				Sounds.anvil.play(p, p);
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SOUL_TORCH,
				"On cast, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + ". "+
						"Your next basic attack additionally deals <white>" + damage + " </white>" + GlossaryTag.SLASHING.tag(this) +
				" damage.");
	}
}
