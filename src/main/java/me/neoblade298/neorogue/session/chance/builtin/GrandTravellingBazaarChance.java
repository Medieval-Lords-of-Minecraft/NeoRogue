package me.neoblade298.neorogue.session.chance.builtin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.DropTableSet;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.PlayerSessionData.EquipmentMetadata;
import me.neoblade298.neorogue.player.inventory.EquipmentGlossaryInventory;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.chance.ChanceInventory;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class GrandTravellingBazaarChance extends ChanceSet {
	private static final int GOLD_COST = 150;

	public GrandTravellingBazaarChance() {
		super(new RegionType[] { RegionType.HARVEST_FIELDS, RegionType.FROZEN_WASTES },
				Material.EMERALD, "GrandTravellingBazaar", "Grand Travelling Bazaar", true);

		ChanceStage stage = new ChanceStage(this, INIT_ID,
				"A sleazy merchant with a wide grin steps out from behind a cart overflowing with mysterious items. "
				+ "\"Finest goods in the land, guaranteed!\" they declare. \"I'll take anything valuable off your "
				+ "hands in exchange for something useful. Only one trade per customer — choose wisely!\"");

		// Choice 1: Trade a specific artifact the player has for a reward artifact
		ChanceChoice tradeArtifact = new ChanceChoice(Material.ENDER_EYE, "Trade an artifact",
				GrandTravellingBazaarChance::desc1,
				"You have no artifacts to offer!",
				(s, inst, data) -> getValue(data, "c1") != null,
				(s, inst, data) -> {
					String c1 = getValue(data, "c1");
					String r1 = getValue(data, "r1");
					Artifact cost = (Artifact) Equipment.get(c1, false);
					Artifact reward = (Artifact) Equipment.get(r1, false);
					Player p = data.getPlayer();
					data.removeArtifact(cost);
					Util.msgRaw(p, Component.text("You hand over your ", NamedTextColor.GRAY)
							.append(cost.getDisplay())
							.append(Component.text(". The merchant eyes it greedily before sliding over a new artifact.", NamedTextColor.GRAY)));
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> traded an artifact with the merchant.", p);
					data.giveArtifact(reward, 1);
					return null;
				});
		setRewardGlossary(tradeArtifact, "r1");
		stage.addChoice(tradeArtifact);

		// Choice 2: Trade 150 gold for a reward artifact
		ChanceChoice buyWithGold = new ChanceChoice(Material.GOLD_INGOT, "Buy with " + GOLD_COST + " gold",
				GrandTravellingBazaarChance::desc2,
				"You don't have " + GOLD_COST + " coins!",
				(s, inst, data) -> data.hasCoins(GOLD_COST),
				(s, inst, data) -> {
					String r2 = getValue(data, "r2");
					Artifact reward = (Artifact) Equipment.get(r2, false);
					Player p = data.getPlayer();
					data.addCoins(-GOLD_COST);
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> bought an artifact from the merchant.", p);
					data.giveArtifact(reward, 1);
					return null;
				});
		setRewardGlossary(buyWithGold, "r2");
		stage.addChoice(buyWithGold);

		// Choice 3: Trade a specific non-storage equipment for a reward artifact
		ChanceChoice tradeEquipment = new ChanceChoice(Material.IRON_SWORD, "Trade equipment",
				GrandTravellingBazaarChance::desc3,
				"You have no equipped or hotbar items to offer!",
				(s, inst, data) -> getValue(data, "c3") != null,
				(s, inst, data) -> {
					String c3 = getValue(data, "c3");
					String r3 = getValue(data, "r3");
					String[] parts = c3.split(":");
					String eqId = parts[0];
					boolean isUpgraded = Boolean.parseBoolean(parts[1]);
					Artifact reward = (Artifact) Equipment.get(r3, false);
					Player p = data.getPlayer();

					ArrayList<EquipmentMetadata> candidates = data.aggregateEquipment(meta -> {
						Equipment eq = meta.getEquipment();
						return meta.getEquipSlot() != EquipSlot.STORAGE
								&& eq.getId().equals(eqId)
								&& eq.isUpgraded() == isUpgraded;
					});

					if (candidates.isEmpty()) {
						Util.msgRaw(p, "<red>The merchant can't find that item on you anymore!");
						return null;
					}

					EquipmentMetadata target = candidates.get(0);
					Equipment traded = data.removeEquipment(target.getEquipSlot(), target.getSlot()).getEquipment();
					data.setupInventory();
					Util.msgRaw(p, Component.text("You hand over your ", NamedTextColor.GRAY)
							.append(traded.getDisplay())
							.append(Component.text(". The merchant inspects it with a grin and shoves an artifact in your pocket.", NamedTextColor.GRAY)));
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> traded equipment with the merchant.", p);
					data.giveArtifact(reward, 1);
					return null;
				});
		setRewardGlossary(tradeEquipment, "r3");
		stage.addChoice(tradeEquipment);

	}

	// Right-clicking a choice opens the glossary page of the artifact it offers
	private static void setRewardGlossary(ChanceChoice choice, String rewardKey) {
		choice.setOnRightClick((p, prev) -> {
			ChanceInventory ci = (ChanceInventory) prev;
			String rewardId = getValue(ci.getData(), rewardKey);
			if (rewardId == null) return;
			Equipment reward = Equipment.get(rewardId, false);
			if (reward == null) return;
			new EquipmentGlossaryInventory(p, reward, prev);
		});
	}

	@Override
	public void initialize(Session s, ChanceInstance inst) {
		for (PlayerSessionData data : s.getParty().values()) {
			HashMap<String, String> values = parseData(data);

			// Roll 3 independent reward artifacts (one per choice, duplicates allowed)
			DropTableSet<Artifact> pool1 = Equipment.copyArtifactsDropSet(data.getPlayerClass(), EquipmentClass.CLASSLESS);
			DropTableSet<Artifact> pool2 = Equipment.copyArtifactsDropSet(data.getPlayerClass(), EquipmentClass.CLASSLESS);
			DropTableSet<Artifact> pool3 = Equipment.copyArtifactsDropSet(data.getPlayerClass(), EquipmentClass.CLASSLESS);
			values.put("r1", Equipment.getArtifact(pool1, s.getBaseDropValue(), 1, data.getPlayerClass(), EquipmentClass.CLASSLESS).get(0).getId());
			values.put("r2", Equipment.getArtifact(pool2, s.getBaseDropValue(), 1, data.getPlayerClass(), EquipmentClass.CLASSLESS).get(0).getId());
			values.put("r3", Equipment.getArtifact(pool3, s.getBaseDropValue(), 1, data.getPlayerClass(), EquipmentClass.CLASSLESS).get(0).getId());

			// Choice 1 cost: pick a random artifact the player currently has
			if (!data.getArtifacts().isEmpty()) {
				ArtifactInstance[] owned = data.getArtifacts().values().toArray(new ArtifactInstance[0]);
				ArtifactInstance pick = owned[NeoRogue.gen.nextInt(owned.length)];
				values.put("c1", pick.getArtifact().getId());
			}
			else {
				values.remove("c1");
			}

			// Choice 3 cost: pick a random non-storage, non-artifact, non-consumable equipment
			ArrayList<EquipmentMetadata> candidates = data.aggregateEquipment(meta -> {
				EquipmentType type = meta.getEquipment().getType();
				return meta.getEquipSlot() != EquipSlot.STORAGE
						&& type != EquipmentType.ARTIFACT
						&& type != EquipmentType.CONSUMABLE;
			});
			if (!candidates.isEmpty()) {
				EquipmentMetadata pick = candidates.get(NeoRogue.gen.nextInt(candidates.size()));
				Equipment eq = pick.getEquipment();
				values.put("c3", eq.getId() + ":" + eq.isUpgraded());
			}
			else {
				values.remove("c3");
			}

			data.setInstanceData(INIT_ID + "::" + serializeData(values));
		}
	}

	private static List<TextComponent> desc1(Session s, ChanceInstance inst, PlayerSessionData data) {
		String c1Id = getValue(data, "c1");
		String r1Id = getValue(data, "r1");
		if (c1Id == null || r1Id == null) {
			return List.of((TextComponent) Component.text("You have no artifacts to offer.", NamedTextColor.GRAY));
		}
		Equipment cost = Equipment.get(c1Id, false);
		Equipment reward = Equipment.get(r1Id, false);
		return List.of((TextComponent) Component.text("Trade your ", NamedTextColor.GRAY)
				.append(cost.getDisplay())
				.append(Component.text(" for ", NamedTextColor.GRAY))
				.append(reward.getDisplay())
				.append(Component.text(".", NamedTextColor.GRAY)));
	}

	private static List<TextComponent> desc2(Session s, ChanceInstance inst, PlayerSessionData data) {
		String r2Id = getValue(data, "r2");
		if (r2Id == null) {
			return List.of((TextComponent) Component.text("No offer available.", NamedTextColor.GRAY));
		}
		Equipment reward = Equipment.get(r2Id, false);
		return List.of((TextComponent) Component.text("Spend ", NamedTextColor.GRAY)
				.append(Component.text(GOLD_COST + " coins", NamedTextColor.YELLOW))
				.append(Component.text(" for ", NamedTextColor.GRAY))
				.append(reward.getDisplay())
				.append(Component.text(".", NamedTextColor.GRAY)));
	}

	private static List<TextComponent> desc3(Session s, ChanceInstance inst, PlayerSessionData data) {
		String c3Key = getValue(data, "c3");
		String r3Id = getValue(data, "r3");
		if (c3Key == null || r3Id == null) {
			return List.of((TextComponent) Component.text("You have no eligible equipment to offer.", NamedTextColor.GRAY));
		}
		String[] parts = c3Key.split(":");
		Equipment cost = Equipment.get(parts[0], Boolean.parseBoolean(parts[1]));
		Equipment reward = Equipment.get(r3Id, false);
		if (cost == null || reward == null) {
			return List.of((TextComponent) Component.text("No offer available.", NamedTextColor.GRAY));
		}
		return List.of((TextComponent) Component.text("Trade your ", NamedTextColor.GRAY)
				.append(cost.getDisplay())
				.append(Component.text(" for ", NamedTextColor.GRAY))
				.append(reward.getDisplay())
				.append(Component.text(".", NamedTextColor.GRAY)));
	}

	private static HashMap<String, String> parseData(PlayerSessionData data) {
		HashMap<String, String> out = new HashMap<String, String>();
		String payload = ChanceInstance.getInstanceDataPayload(data);
		if (payload == null || payload.isEmpty()) return out;
		for (String pair : payload.split(";")) {
			String[] kv = pair.split("=", 2);
			if (kv.length == 2 && !kv[0].isEmpty()) {
				out.put(kv[0], kv[1]);
			}
		}
		return out;
	}

	private static String serializeData(HashMap<String, String> data) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String key : new String[] { "r1", "r2", "r3", "c1", "c3" }) {
			if (!data.containsKey(key)) continue;
			if (!first) sb.append(';');
			first = false;
			sb.append(key).append('=').append(data.get(key));
		}
		return sb.toString();
	}

	private static String getValue(PlayerSessionData data, String key) {
		return parseData(data).get(key);
	}
}
