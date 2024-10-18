package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BattleCry extends Equipment {
	private static final String ID = "battleCry";
	private static final ParticleContainer pc = new ParticleContainer(Particle.REDSTONE);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_BLAZE_DEATH);
	private int strength;
	
	public BattleCry(boolean isUpgraded) {
		super(ID, "Battle Cry", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 20, 15, 0));
		strength = isUpgraded ? 20 : 14;
		
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}
	
	@Override
	public void setupReforges() {
		addSelfReforge(BerserkersCall.get(), WarCry.get(), Roar.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"On cast, give yourself <yellow>" + strength + "</yellow> " + GlossaryTag.STRENGTH.tag(this) + " damage for <white>10</white> seconds.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			sc.play(p, p);
			pc.play(p, p);
			data.applyStatus(StatusType.STRENGTH, data, strength, 200);
			return TriggerResult.keep();
		}));
	}
}
