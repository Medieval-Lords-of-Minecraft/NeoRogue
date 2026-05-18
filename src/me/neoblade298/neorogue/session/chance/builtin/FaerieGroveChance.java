package me.neoblade298.neorogue.session.chance.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.PlayerSessionData.EquipmentMetadata;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class FaerieGroveChance extends ChanceSet {

	public FaerieGroveChance() {
		super(RegionType.HARVEST_FIELDS, Material.FLOWERING_AZALEA, "FaerieGrove", "Faerie Grove", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You wander into a grove buzzing with tiny lights. "
				+ "A faerie materializes and eyes your belongings with mischief. "
				+ "\"A trade, perhaps?\"");

		stage.addChoice(new ChanceChoice(Material.GOLD_INGOT, "Trade",
				(s, inst, data) -> {
					return getTradeDescription(inst, data);
				},
				"You no longer have the item the faerie wants!",
				(s, inst, data) -> {
					String chosen = inst.getEventData(data.getUniqueId() + ":chosen");
					if (chosen == null) return false;
					ArrayList<EquipmentMetadata> matches = data.aggregateEquipment((meta) -> {
						return meta.getEquipment().serialize().equals(chosen);
					});
					return !matches.isEmpty();
				},
				(s, inst, data) -> {
					Player p = data.getPlayer();
					String chosen = inst.getEventData(data.getUniqueId() + ":chosen");
					String reward = inst.getEventData(data.getUniqueId() + ":reward");
					// Remove the chosen item
					ArrayList<EquipmentMetadata> matches = data.aggregateEquipment((meta) -> {
						return meta.getEquipment().serialize().equals(chosen);
					});
					if (!matches.isEmpty()) {
						EquipmentMetadata meta = matches.get(0);
						data.removeEquipment(meta.getEquipSlot(), meta.getSlot());
					}
					// Give the reward
					Equipment rewardEq = Equipment.deserialize(reward);
					data.giveEquipment(rewardEq);
					PlayerSessionInventory.setupInventory(p.getInventory(), data);
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> traded with the faerie!", p);
					return null;
				}));

		stage.addChoice(new ChanceChoice(Material.REDSTONE, "Try to escape",
				(s, inst, data) -> {
					return getSacrificeDescription(inst, data);
				},
				(s, inst, data) -> {
					Player p = data.getPlayer();
					double healthLoss = Math.min(15, data.getHealth() - 1);
					data.setHealth(data.getHealth() - healthLoss);
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> ran away and paid the price!", p);
					return null;
				}));
	}

	@Override
	public void initialize(Session s, ChanceInstance inst) {
		// If event data is already populated (deserialized), nothing to do
		if (inst.getEventData(INIT_ID) != null) return;

		// Fresh start: pick random items per player
		for (PlayerSessionData data : s.getParty().values()) {
			ArrayList<EquipmentMetadata> eligible = data.aggregateEquipment((meta) -> {
				Equipment eq = meta.getEquipment();
				return eq.getType() != EquipmentType.CONSUMABLE && !eq.isCursed();
			});

			if (eligible.isEmpty()) continue;

			EquipmentMetadata chosen = eligible.get(NeoRogue.gen.nextInt(eligible.size()));
			String chosenSerialized = chosen.getEquipment().serialize();

			Equipment reward = Equipment.getDrop(s.getBaseDropValue() + 1, data.getPlayerClass(), EquipmentClass.CLASSLESS);
			reward = s.rollUpgrade(reward, 0);
			String rewardSerialized = reward.serialize();

			inst.setEventData(data.getUniqueId() + ":chosen", chosenSerialized);
			inst.setEventData(data.getUniqueId() + ":reward", rewardSerialized);
		}
		// Mark as initialized so deserialization path skips generation
		inst.setEventData(INIT_ID, "1");
	}
	
	private List<TextComponent> getTradeDescription(ChanceInstance inst, PlayerSessionData data) {
		String chosen = inst.getEventData(data.getUniqueId() + ":chosen");
		String reward = inst.getEventData(data.getUniqueId() + ":reward");
		if (chosen == null || reward == null) {
			return SharedUtil.addLineBreaks((TextComponent) Component.text("The faerie has nothing to offer you.", NamedTextColor.GRAY), 250);
		}
		Equipment chosenEq = Equipment.deserialize(chosen);
		Equipment rewardEq = Equipment.deserialize(reward);
		ArrayList<TextComponent> lore = new ArrayList<>();
		lore.addAll(SharedUtil.addLineBreaks(Component.text("Give your ", NamedTextColor.GRAY)
				.append(chosenEq.getHoverable())
				.append(Component.text(" to receive ", NamedTextColor.GRAY))
				.append(rewardEq.getHoverable())
				.append(Component.text(".", NamedTextColor.GRAY)), 250));
		return lore;
	}
	
	private List<TextComponent> getSacrificeDescription(ChanceInstance inst, PlayerSessionData data) {
		ArrayList<TextComponent> lore = new ArrayList<>();
		lore.addAll(SharedUtil.addLineBreaks(Component.text("Lose ", NamedTextColor.GRAY)
				.append(Component.text("15", NamedTextColor.RED))
				.append(Component.text(" health. If you have less, lose all but 1 instead.", NamedTextColor.GRAY)), 250));
		return lore;
	}
}
