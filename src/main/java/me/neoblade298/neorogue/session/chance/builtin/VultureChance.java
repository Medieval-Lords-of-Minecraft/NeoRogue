package me.neoblade298.neorogue.session.chance.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.MinibossFightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.settings.NotorietySetting;
import net.kyori.adventure.text.TextComponent;

public class VultureChance extends ChanceSet {
	private static final String ED_ARTIFACT = "foundArtifact", ED_HEAL = "foundHeal", ED_EQUIPMENT = "foundEquipment";

	private static final ChanceChoice leave = new ChanceChoice(Material.FLINT, "Leave while you can",
			"No need to risk anything else.", (s, inst, unused) -> {
		s.broadcast("This is a disaster waiting to happen. The party sneaks away.");
		return null;
	});

	public VultureChance() {
		super(RegionType.LOW_DISTRICT, Material.GRAVEL, "Vulture");

		// Stage: miniboss fight
		ChanceStage miniboss = new ChanceStage(this, "miniboss", "You're cornered by a powerful foe.");
		miniboss.addChoice(new ChanceChoice(Material.IRON_SWORD, "<red>You know what time it is!"));

		// Stage: init
		ChanceStage init = new ChanceStage(this, INIT_ID,
				"You come across a dead adventurer lying against a wall. "
						+ "Something dangerous was nearby and it may not be a good idea to stay for long, but the adventurer "
						+ "looks to have had some useful items on them.");
		init.addChoice(new ChanceChoice(Material.FLINT, "Steal from the dead adventurer",
				VultureChance::stealDescription, VultureChance::stealAction));
		init.addChoice(leave);

		// Stages: loot (returned to after each successful steal). Split by how many items have already
		// been found so each escalating risk tier (50% after 1, 75% after 2) is a distinct stageId in
		// pickrate analytics rather than being conflated under one "loot" stage.
		ChanceStage loot1 = new ChanceStage(this, "loot1",
				"You successfully loot an item, but each theft draws the enemy closer.");
		loot1.addChoice(new ChanceChoice(Material.FLINT, "Steal more",
				VultureChance::stealDescription, VultureChance::stealAction));
		loot1.addChoice(leave);

		ChanceStage loot2 = new ChanceStage(this, "loot2",
				"You've grabbed most of the valuables. One more theft would be extremely risky.");
		loot2.addChoice(new ChanceChoice(Material.FLINT, "Steal more",
				VultureChance::stealDescription, VultureChance::stealAction));
		loot2.addChoice(leave);

		// Stage: caught (all 3 items found, enemy closing in)
		ChanceStage caught = new ChanceStage(this, "caught",
				"You've taken everything of value. The enemy is closing in.");
		caught.addChoice(new ChanceChoice(Material.IRON_SWORD, "<red>Fight the miniboss",
				"Face the enemy dealing <white>20%</white> reduced damage.", (s, inst, unused) -> {
			s.broadcast("<red>The enemy has found you! Prepare for battle!");
			setupMinibossFight(s, inst);
			return "miniboss";
		}));
		caught.addChoice(leave);
	}

	private static int getFoundCount(ChanceInstance inst) {
		int count = 0;
		if (inst.getEventData(ED_ARTIFACT) != null) count++;
		if (inst.getEventData(ED_HEAL) != null) count++;
		if (inst.getEventData(ED_EQUIPMENT) != null) count++;
		return count;
	}

	private static List<TextComponent> stealDescription(Session s, ChanceInstance inst, PlayerSessionData data) {
		int failPercent = 25 + (getFoundCount(inst) * 25);
		return SharedUtil.addLineBreaks((TextComponent) NeoCore.miniMessage().deserialize(
				"<yellow>" + failPercent + "%</yellow> chance you will fail and be forced to fight a Miniboss dealing <white>20%</white> reduced damage."), 250);
	}

	private static String stealAction(Session s, ChanceInstance inst, PlayerSessionData unused) {
		int foundCount = getFoundCount(inst);
		int failPercent = 25 + (foundCount * 25);
		if (NeoRogue.gen.nextInt(100) < failPercent) {
			s.broadcast("<red>As you loot the body, the enemy returns!");
			setupMinibossFight(s, inst);
			return "miniboss";
		}
		else {
			ItemFound nextFind = chooseNextFind(inst);
			s.broadcast(nextFind.desc);
			nextFind.action.run(s);
			nextFind.markFound(inst);
			int found = getFoundCount(inst);
			return found >= 3 ? "caught" : (found == 1 ? "loot1" : "loot2");
		}
	}

	private static void setupMinibossFight(Session s, ChanceInstance inst) {
		inst.setNextInstance(new MinibossFightInstance(s, s.getParty().keySet(), s.getRegion().getType()));
		((FightInstance) inst.getNextInstance()).addInitialTask((fi, fdata) -> {
			for (PlayerFightData pfdata : fdata) {
				pfdata.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(pfdata, 0, -0.2, BuffStatTracker.ignored("vultureChance")));
			}
		});
	}

	private static ItemFound chooseNextFind(ChanceInstance inst) {
		ArrayList<ItemFound> finds = new ArrayList<ItemFound>(3);
		if (inst.getEventData(ED_HEAL) == null) finds.add(ItemFound.HEAL);
		if (inst.getEventData(ED_ARTIFACT) == null) finds.add(ItemFound.ARTIFACT);
		if (inst.getEventData(ED_EQUIPMENT) == null) finds.add(ItemFound.EQUIPMENT);
		return finds.get(NeoRogue.gen.nextInt(finds.size()));
	}

	private enum ItemFound {
		ARTIFACT(ED_ARTIFACT, "You find an artifact for your troubles!", (s) -> {
			for (PlayerSessionData data : s.getParty().values()) {
				Artifact drop = Equipment.getArtifact(data.getArtifactDroptable(), s.getBaseDropValue() + 1, 1, data.getPlayerClass(), EquipmentClass.CLASSLESS).get(0);
				data.giveEquipment(drop);
			}
		}),
		HEAL(ED_HEAL, "You get healed for <yellow>25%</yellow> of your max health!", (s) -> {
			for (PlayerSessionData data : s.getParty().values()) {
				data.healPercent(0.25);
			}
		}),
		EQUIPMENT(ED_EQUIPMENT, "You find some equipment for your troubles!", (s) -> {
			for (PlayerSessionData data : s.getParty().values()) {
				SessionEquipment se = new SessionEquipment(Equipment.getDrop(data.getData().getEquipmentDroptable(), s.getBaseDropValue() + 1, 1, data.getPlayerClass(), EquipmentClass.CLASSLESS).get(0));
				se = s.rollUpgrade(se, 0);
				NotorietySetting.rollBreakable(s, se);
				data.giveEquipment(se);
			}
		});
		private String eventDataKey;
		private String desc;
		private RunnableFind action;

		private ItemFound(String eventDataKey, String desc, RunnableFind action) {
			this.eventDataKey = eventDataKey;
			this.desc = desc;
			this.action = action;
		}

		public void markFound(ChanceInstance inst) {
			inst.setEventData(eventDataKey, "true");
		}
	}

	private interface RunnableFind {
		public void run(Session s);
	}
}
