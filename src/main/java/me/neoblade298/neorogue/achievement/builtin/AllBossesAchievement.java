package me.neoblade298.neorogue.achievement.builtin;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementScope;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.achievement.ObjectiveAchievement;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AllBossesAchievement extends ObjectiveAchievement {
	private final String id;
	private final Component displayName;
	private final Material material;
	private final RegionType region;

	public AllBossesAchievement(String id, Component displayName, Material material, RegionType region) {
		this.id = id;
		this.displayName = displayName;
		this.material = material;
		this.region = region;
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
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Beat all " + region.getDisplay() + " bosses.", NamedTextColor.GRAY));
	}

    @Override
    public int getSortPriority() {
        return 60 + region.getDifficulty();
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
		ArrayList<MapPiece> pieces = Map.getBossPieces().get(region);
		if (pieces == null) return List.of();
		List<String> ids = new ArrayList<>();
		for (MapPiece piece : pieces) {
			ids.add(piece.getId());
		}
		return ids;
	}

	@Override
	public String getObjectiveDisplay(String id) {
		ArrayList<MapPiece> pieces = Map.getBossPieces().get(region);
		if (pieces != null) {
			for (MapPiece piece : pieces) {
				if (piece.getId().equals(id)) {
					return piece.getDisplay() != null ? piece.getDisplay() : piece.getId();
				}
			}
		}
		return id;
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(id, SessionTrigger.WIN_BOSS, (pdata, in) -> {
			String bossId = (String) in;
			if (!getObjectiveIds().contains(bossId)) return;
			if (completeObjective(progress, bossId)) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
		});
	}
}
