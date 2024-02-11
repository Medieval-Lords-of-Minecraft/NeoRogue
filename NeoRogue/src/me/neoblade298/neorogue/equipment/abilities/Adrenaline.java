package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Adrenaline extends Equipment {
	private ParticleContainer pc = new ParticleContainer(Particle.REDSTONE);
	private static final int stamina = 100;
	
	public Adrenaline(boolean isUpgraded) {
		super("adrenaline", "Adrenaline", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 5, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
		
		addReforgeOption("battleCry", "warCry", "command");
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On cast, give yourself <white>" + stamina + " </white> stamina. Can be cast <yellow>" +
					(isUpgraded ? "once" : "twice") + "</yellow> per fight.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new AdrenalineInstance(p, this, slot, es));
	}
	
	private class AdrenalineInstance extends EquipmentInstance {
		private int count = 0;
		private int max;
		public AdrenalineInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			max = isUpgraded ? 2 : 1;
			action = (pdata, in) -> {
				count++;
				Util.playSound(p, Sound.ENTITY_BLAZE_DEATH, 1F, 1F, false);
				pc.spawn(p);
				pdata.addStamina(stamina);
				if (count < max) return TriggerResult.keep();

				if (es == EquipSlot.HOTBAR) p.getInventory().setItem(slot, null);
				return TriggerResult.remove();
			};
		}
		
	}
}
