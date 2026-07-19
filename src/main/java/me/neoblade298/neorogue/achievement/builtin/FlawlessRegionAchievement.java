package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementScope;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.achievement.ObjectiveAchievement;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FlawlessRegionAchievement extends ObjectiveAchievement {
	private static final String ID = "flawless";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Flawless", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.DIAMOND_CHESTPLATE;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Beat regions without losing health.", NamedTextColor.GRAY));
	}

	@Override
	public AchievementScope getScope() {
		return AchievementScope.BOTH;
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.SESSION);
	}

	@Override
	public List<String> getObjectiveIds() {
		return List.of("ld", "hf", "fw");
	}

	@Override
	public String getObjectiveDisplay(String id) {
		return switch (id) {
			case "ld" -> "Beat Low District without losing health";
			case "hf" -> "Beat Harvest Fields without losing health";
			case "fw" -> "Beat Frozen Wastes without losing health";
			default -> id;
		};
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(ID, SessionTrigger.WIN_BOSS, (pdata, in) -> {
			double currentDamage = pdata.getSessionStats().getDamageTakenHealth();
			double baseline = pdata.getSessionStats().getDamageTakenHealthAtRegionStart();
			if (currentDamage <= baseline) {
				// No health lost this region
				RegionType region = pdata.getSession().getRegion().getType();
				String objective = regionToObjective(region);
				if (objective != null && !isObjectiveComplete(progress, objective)) {
					if (completeObjective(progress, objective)) {
						AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
					}
				}
			}
			return TriggerResult.keep();
		});
	}

	private String regionToObjective(RegionType region) {
		return switch (region) {
			case LOW_DISTRICT, LOW_DISTRICT_DEBUG -> "ld";
			case HARVEST_FIELDS, HARVEST_FIELDS_DEBUG -> "hf";
			case FROZEN_WASTES, FROZEN_WASTES_DEBUG -> "fw";
			default -> null;
		};
	}
}
