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

public class SpendCoinsAchievement implements Achievement {
	private static final int[] THRESHOLDS = { 100, 1000, 10000, 100000 };

	@Override
	public String getId() {
		return "spend_coins";
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Big Spender", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.GOLD_NUGGET;
	}

    @Override
    public int getSortPriority() {
        return 22;
    }

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Spend coins at shops.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.SESSION);
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger("spend_coins", SessionTrigger.SPEND_COINS, (pdata, in) -> {
			int amount = (int) in;
			if (progress.addProgress(amount)) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
		});
	}
}
