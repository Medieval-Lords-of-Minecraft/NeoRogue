package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BeatMinibossesAchievement implements Achievement {
	private static final String ID = "beat_minibosses";
	private static final int[] THRESHOLDS = { 1, 5, 20, 100 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Miniboss Slayer", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.IRON_SWORD;
	}

    @Override
    public int getSortPriority() {
        return 5;
    }

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		String desc = target == 1 ? "Defeat a miniboss." : "Defeat " + target + " minibosses.";
		return List.of(Component.text(desc, NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.SESSION);
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(ID, SessionTrigger.WIN_MINIBOSS, (pdata, in) -> {
			if (progress.addProgress(1)) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
		});
	}
}
