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

public class Burst extends Equipment {
	private static final String ID = "Burst";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST);
	private int stamina, buff;
	private static final int seconds = 15;
	
	public Burst(boolean isUpgraded) {
		super(ID, "Burst", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 25, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
		buff = isUpgraded ? 30 : 20;
		stamina = isUpgraded ? 80 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On cast, give yourself <yellow>" + stamina + "</yellow> stamina and <yellow>" + buff +
				"</yellow> " + GlossaryTag.STRENGTH.tag(this) + " for <white>" + seconds + "</white> seconds.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.blazeDeath.play(p, p);
			pc.play(p, p);
			pdata.addStamina(stamina);
			data.applyStatus(StatusType.STRENGTH, data, buff, seconds * 20);
			return TriggerResult.keep();
		}));
	}
}
