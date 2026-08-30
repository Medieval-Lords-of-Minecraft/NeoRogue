package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementScope;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SoleSurvivorAchievement implements Achievement {
	private static final String ID = "sole_survivor";
	private static final int[] THRESHOLDS = { 1 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Sole Survivor", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.TOTEM_OF_UNDYING;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public AchievementScope getScope() {
		return AchievementScope.GLOBAL;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Win a fight as the last living member of a party.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.FIGHT);
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		data.addTrigger(ID, Trigger.WIN_FIGHT, (pdata, in) -> {
			if (fight.getPlayers().size() < 2) return TriggerResult.keep();

			long livingPlayers = fight.getPlayers().stream().filter(playerData -> !playerData.isDead()).count();
			if (livingPlayers == 1 && !pdata.isDead() && progress.addProgress(1)) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}