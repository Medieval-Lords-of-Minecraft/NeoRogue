package me.neoblade298.neorogue.achievement.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementScope;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class RapidCombatAchievement implements Achievement {
	private static final long WINDOW_MILLIS = 10000;

	private final String id;
	private final Component displayName;
	private final Material material;
	private final EquipmentClass requiredClass;
	private final Trigger trigger;
	private final boolean countProjectiles;
	private final String actionName;
	private final int[] thresholds;

	public RapidCombatAchievement(String id, String displayName, Material material, EquipmentClass requiredClass,
			Trigger trigger, boolean countProjectiles, String actionName, int... thresholds) {
		this.id = id;
		this.displayName = Component.text(displayName, NamedTextColor.GOLD);
		this.material = material;
		this.requiredClass = requiredClass;
		this.trigger = trigger;
		this.countProjectiles = countProjectiles;
		this.actionName = actionName;
		this.thresholds = thresholds;
	}

	@Override public String getId() { return id; }
	@Override public Component getDisplayName() { return displayName; }
	@Override public Material getMaterial() { return material; }
	@Override public int[] getMasteryThresholds() { return thresholds; }
	@Override public AchievementScope getScope() { return AchievementScope.CLASS; }
	@Override public EquipmentClass getRequiredClass() { return requiredClass; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.FIGHT); }

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < thresholds.length ? thresholds[mastery] : thresholds[thresholds.length - 1];
		return List.of(Component.text(actionName + " " + target + " times within 10 seconds.", NamedTextColor.GRAY));
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		Deque<Long> events = new ArrayDeque<>();
		data.addTrigger(id, trigger, (pdata, in) -> {
			long now = System.currentTimeMillis();
			while (!events.isEmpty() && now - events.peekFirst() > WINDOW_MILLIS) events.removeFirst();
			int amount = countProjectiles ? ((LaunchProjectileGroupEvent) in).getInstances().size() : 1;
			for (int i = 0; i < amount; i++) events.addLast(now);
			int count = events.size();
			if (count > progress.getProgress() && progress.addProgress(count - progress.getProgress())) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
