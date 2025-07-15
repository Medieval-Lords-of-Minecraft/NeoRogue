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

public class LimitBreak extends Equipment {
	private static final String ID = "limitBreak";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_RAVAGER_ROAR);
	
	public LimitBreak(boolean isUpgraded) {
		super(ID, "Limit Break", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 100, 40, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DIAMOND,
				"On cast, double your " + GlossaryTag.STRENGTH.tag(this) + ". Can be cast <yellow>" +
					(isUpgraded ? "twice" : "once") + "</yellow> per fight.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new LimitBreakInstance(data, this, slot, es));
	}
	
	private class LimitBreakInstance extends EquipmentInstance {
		private int count = 0;
		private int max;
		public LimitBreakInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			Player p = data.getPlayer();
			max = isUpgraded ? 2 : 1;
			action = (pdata, in) -> {
				count++;
				sc.play(p, p);
				pc.play(p, p);
				int str = data.getStatus(StatusType.STRENGTH).getStacks();
				data.applyStatus(StatusType.STRENGTH, data, str, -1);
				if (count < max) return TriggerResult.keep();

				if (es == EquipSlot.HOTBAR) p.getInventory().setItem(slot, null);
				return TriggerResult.remove();
			};
		}
		
	}
}
