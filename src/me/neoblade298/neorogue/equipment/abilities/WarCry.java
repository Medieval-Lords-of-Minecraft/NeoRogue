package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WarCry extends Equipment {
	private static final String ID = "WarCry";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST);
	private int strength, shields;
	
	public WarCry(boolean isUpgraded) {
		super(ID, "War Cry", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 25, 15, 0));
		strength = isUpgraded ? 15 : 10;
		shields = isUpgraded ? 15 : 10;
		
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"On cast, give yourself <yellow>" + strength + "</yellow> " + GlossaryTag.STRENGTH.tag(this) + " and <yellow>"
						+ strength + "</yellow> " + GlossaryTag.SHIELDS.tag(this) + " that last <white>5</white> seconds.");
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Player p = data.getPlayer();
			Sounds.blazeDeath.play(p, p);
			pc.play(p, p);
			data.applyStatus(StatusType.STRENGTH, data, strength, 100);
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			return TriggerResult.keep();
		}));
	}
}
