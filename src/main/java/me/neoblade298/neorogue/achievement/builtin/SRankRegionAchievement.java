package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.FightScore;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SRankRegionAchievement implements Achievement {
	private static final int[] THRESHOLDS = { 1, 5, 20, 100 };

	private final String id;
	private final Component displayName;
	private final Material material;
	private final RegionType region;
	private final RegionType debugRegion;

	public SRankRegionAchievement(String id, Component displayName, Material material, RegionType region) {
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
    public int getSortPriority() {
        return 60 + region.getDifficulty();
    }

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Beat a " + region.getDisplay() + " fight with S rank.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.FIGHT);
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			RegionType fightRegion = pdata.getSessionData().getSession().getRegion().getType();
			if (fightRegion != region && fightRegion != debugRegion) return TriggerResult.keep();
			if (pdata.getInstance().getFightScore() == FightScore.S) {
				if (progress.addProgress(1)) {
					Player p = pdata.getPlayer();
					AchievementManager.notifyMastery(p, this, progress);
				}
			}
			return TriggerResult.keep();
		});
	}
}
