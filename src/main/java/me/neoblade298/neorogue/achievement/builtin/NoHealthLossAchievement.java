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

public class NoHealthLossAchievement extends ObjectiveAchievement {
	private static final String ID = "untouchable";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Untouchable", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.GOLDEN_APPLE;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Beat fights without losing health.", NamedTextColor.GRAY));
	}

	@Override
	public int getSortPriority() {
		return 80;
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
	public List<String> getObjectiveIds() {
		return List.of("fight", "miniboss", "boss");
	}

	@Override
	public String getObjectiveDisplay(String id) {
		return switch (id) {
			case "fight" -> "Beat a fight without losing health";
			case "miniboss" -> "Beat a miniboss without losing health";
			case "boss" -> "Beat a boss without losing health";
			default -> id;
		};
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		final boolean[] tookDamage = { false };

		data.addTrigger(ID, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
			tookDamage[0] = true;
			return TriggerResult.keep();
		});

		data.addTrigger(ID + "_win", Trigger.WIN_FIGHT, (pdata, in) -> {
			if (tookDamage[0]) return TriggerResult.keep();

			FightInstance inst = pdata.getInstance();
			String objective;
			if (inst instanceof BossFightInstance) {
				objective = "boss";
			} else if (inst instanceof MinibossFightInstance) {
				objective = "miniboss";
			} else {
				objective = "fight";
			}

			if (!isObjectiveComplete(progress, objective)) {
				if (completeObjective(progress, objective)) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
				}
			}
			return TriggerResult.keep();
		});
	}
}
