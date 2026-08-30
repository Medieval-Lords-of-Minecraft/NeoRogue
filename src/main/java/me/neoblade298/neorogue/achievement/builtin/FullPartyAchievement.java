package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementScope;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.achievement.ObjectiveAchievement;
import me.neoblade298.neorogue.session.fight.BossFightInstance;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.MinibossFightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FullPartyAchievement extends ObjectiveAchievement {
	private static final String ID = "strength_in_numbers";
	private static final List<String> OBJECTIVES = List.of("fight", "miniboss", "boss");

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Strength in Numbers", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.GOLDEN_HELMET;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Beat each fight type as a party of 4.", NamedTextColor.GRAY));
	}

	@Override public List<String> getObjectiveIds() { return OBJECTIVES; }
	@Override public String getObjectiveDisplay(String id) {
		return switch (id) {
			case "fight" -> "Beat a fight as a party of 4";
			case "miniboss" -> "Beat a miniboss as a party of 4";
			case "boss" -> "Beat a boss as a party of 4";
			default -> id;
		};
	}

	@Override
	public AchievementScope getScope() {
		return AchievementScope.BOTH;
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.FIGHT);
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		data.addTrigger(ID, Trigger.WIN_FIGHT, (pdata, in) -> {
			if (pdata.getInstance().getSession().getParty().size() < 4) return TriggerResult.keep();

			FightInstance inst = pdata.getInstance();
			String objective = inst instanceof BossFightInstance ? "boss"
					: inst instanceof MinibossFightInstance ? "miniboss" : "fight";
			if (completeObjective(progress, objective)) AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			return TriggerResult.keep();
		});
	}
}
