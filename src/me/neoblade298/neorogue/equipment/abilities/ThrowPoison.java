package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.PotionProjectile;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ThrowPoison extends Equipment {
	private static final String ID = "throwPoison";
	private int poison;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	
	public ThrowPoison(boolean isUpgraded) {
		super(ID, "Throw Poison", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 0));
		poison = isUpgraded ? 15 : 10;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		PotionProjectile pot = new PotionProjectile((loc, hit) -> {
			for (LivingEntity ent : hit) {
				FightInstance.applyStatus(ent, StatusType.POISON, data, poison, -1);
			}
		});
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			pc.play(p, p);
			Sounds.threw.play(p, p);
			pot.
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On cast, gain " + GlossaryTag.POISON.tag(this, poison, true) + " for 5 seconds.");
	}
}
