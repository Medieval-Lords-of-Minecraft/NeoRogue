package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WinFightsAchievement implements Achievement {
	private static final String ID = "victorious";
	private static final int[] THRESHOLDS = { 10, 50, 200, 500 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Victorious", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.GOLDEN_SWORD;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Win " + target + " fights.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.FIGHT);
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		data.addTrigger(ID, Trigger.WIN_FIGHT, (pdata, in) -> {
			if (progress.addProgress(1)) {
				Player p = pdata.getPlayer();
				AchievementManager.notifyMastery(p, this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
