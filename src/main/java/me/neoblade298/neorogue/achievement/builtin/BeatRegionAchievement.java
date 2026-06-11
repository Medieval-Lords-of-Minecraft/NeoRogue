package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BeatRegionAchievement implements Achievement {
	private static final int[] THRESHOLDS = { 1, 5, 20, 100 };

	private final String id;
	private final Component displayName;
	private final Material material;
	private final RegionType region;
	private final RegionType debugRegion;

	public BeatRegionAchievement(String id, Component displayName, Material material, RegionType region) {
		this.id = id;
		this.displayName = displayName;
		this.material = material;
		this.region = region;
		this.debugRegion = RegionType.getDebugRegion(region);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Component getDisplayName() {
		return displayName;
	}

	@Override
	public Material getMaterial() {
		return material;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Beat " + region.getDisplay() + ".", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.SESSION);
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(id, SessionTrigger.WIN_BOSS, (pdata, in) -> {
			RegionType bossRegion = pdata.getSession().getRegion().getType();
			if (bossRegion == region || bossRegion == debugRegion) {
				if (progress.addProgress(1)) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
				}
			}
		});
	}
}
