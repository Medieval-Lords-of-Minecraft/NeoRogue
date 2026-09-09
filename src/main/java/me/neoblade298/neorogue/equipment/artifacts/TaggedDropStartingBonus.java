package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.event.RewardBuildEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public abstract class TaggedDropStartingBonus extends Artifact {
	public static final double DROP_RATE_INCREASE = 0.3;
	private static final int DEFAULT_TAGGED_STARTING_BONUS_WEIGHT = 5;

	private final Material material;
	private final GlossaryTag[] tags;

	protected static int splitStartingBonusWeight(int artifactCount) {
		if (artifactCount < 1 || DEFAULT_STARTING_BONUS_WEIGHT % artifactCount != 0) {
			throw new IllegalArgumentException("Artifact count must evenly divide the normal starting bonus weight");
		}
		return DEFAULT_STARTING_BONUS_WEIGHT / artifactCount;
	}

	protected TaggedDropStartingBonus(String id, String display, EquipmentClass equipmentClass, Material material,
			GlossaryTag... tags) {
		this(id, display, new EquipmentClass[] { equipmentClass }, DEFAULT_TAGGED_STARTING_BONUS_WEIGHT, material, tags);
	}

	protected TaggedDropStartingBonus(String id, String display, EquipmentClass[] equipmentClasses, Material material,
			GlossaryTag... tags) {
		this(id, display, equipmentClasses, DEFAULT_TAGGED_STARTING_BONUS_WEIGHT, material, tags);
	}

	protected TaggedDropStartingBonus(String id, String display, EquipmentClass equipmentClass, int startingBonusWeight,
			Material material, GlossaryTag... tags) {
		this(id, display, new EquipmentClass[] { equipmentClass }, startingBonusWeight, material, tags);
	}

	private TaggedDropStartingBonus(String id, String display, EquipmentClass[] equipmentClasses,
			int startingBonusWeight, Material material, GlossaryTag... tags) {
		super(id, display, Rarity.COMMON, equipmentClasses);
		this.material = material;
		this.tags = tags;
		markAsStartingBonus();
		setStartingBonusWeight(startingBonusWeight);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		data.addTrigger(id, SessionTrigger.REWARD_BUILD, (pdata, input) -> {
			RewardBuildEvent event = (RewardBuildEvent) input;
			event.multiplyEquipmentTagWeight(1 + DROP_RATE_INCREASE, tags);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		String tagList = "";
		for (int i = 0; i < tags.length; i++) {
			if (i > 0) tagList += i == tags.length - 1 ? " and " : ", ";
			tagList += tags[i].tag(this);
		}
		item = createItem(material, "Increases the drop rate of equipment relating to " + tagList + " by "
				+ DescUtil.val((int) (DROP_RATE_INCREASE * 100) + "%") + ".");
	}
}