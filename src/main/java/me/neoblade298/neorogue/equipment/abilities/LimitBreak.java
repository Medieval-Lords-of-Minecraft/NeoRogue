package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LimitBreak extends Equipment {
	private static final String ID = "LimitBreak";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_RAVAGER_ROAR);
	
	public LimitBreak(boolean isUpgraded) {
		super(ID, "Limit Break", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 80, 40, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DIAMOND,
				"On cast, double your " + GlossaryTag.STRENGTH.tag(this) + ". Can only be cast " + DescUtil.white("once")
					+ " per fight, and not until " + DescUtil.yellow((isUpgraded ? 20 : 30) + "s") + " into the fight.");
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new LimitBreakInstance(data, sessionEq, slot, es));
	}
	
	private class LimitBreakInstance extends EquipmentInstance {
		private boolean ready = false;
		private int charge = 0;
		public LimitBreakInstance(PlayerFightData data, SessionEquipment sessionEq, int slot, EquipSlot es) {
			super(data, sessionEq, slot, es);
			int delay = isUpgraded ? 20 : 30;
			ItemStack readyIcon = icon;
			setIcon(icon.withType(Material.COAL));

			// Block casting until the ability has charged for [delay] seconds into the fight
			setCondition((pl, pdata, in) -> {
				if (!ready) {
					Util.msgRaw(pl, hoverable.append(Component.text(" can't be cast until " + delay + "s into the fight!", NamedTextColor.RED)));
					return false;
				}
				return true;
			});

			// Charge over the first [delay] seconds, then swap to the castable icon
			data.addTrigger(id + "_charge", Trigger.PLAYER_TICK, (pdata, in) -> {
				if (++charge < delay) return TriggerResult.keep();
				ready = true;
				setIcon(readyIcon);
				return TriggerResult.remove();
			});

			action = (pdata, in) -> {
				Player pl = data.getPlayer();
				sc.play(pl, pl);
				pc.play(pl, pl);
				int str = data.getStatus(StatusType.STRENGTH).getStacks();
				data.applyStatus(StatusType.STRENGTH, data, str, -1, LimitBreak.this);
				if (es == EquipSlot.HOTBAR) pl.getInventory().setItem(slot, null);
				return TriggerResult.remove();
			};
		}
		
	}
}
