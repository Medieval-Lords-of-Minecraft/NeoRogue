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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Entrench extends Equipment {
	private static final String ID = "Entrench";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK)
			.count(20).spread(0.5, 0.5).offsetY(1);
	
	private int shields;

	public Entrench(boolean isUpgraded) {
		super(ID, "Entrench", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 10, 0, 0));
		shields = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			data.addTrigger(id, Trigger.LAY_TRAP, (pdata2, in2) -> {
				Player p = data.getPlayer();
				data.addPermanentShield(p.getUniqueId(), shields);
				Sounds.equip.play(p, p);
				pc.play(p, p);
				return TriggerResult.keep();
			});

			data.addTrigger(id, Trigger.DEACTIVATE_TRAP, (pdata3, in3) -> {
				Player p = data.getPlayer();
				data.addPermanentShield(p.getUniqueId(), shields);
				Sounds.equip.play(p, p);
				pc.play(p, p);
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_BLOCK,
				GlossaryTag.POWER.tag(this) + ". Gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " every time you place or remove a " +
				GlossaryTag.TRAP.tag(this) + ".");
	}
}
