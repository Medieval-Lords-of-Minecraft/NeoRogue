package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;

public class EmbersOfGlory extends Artifact {
	private static final String ID = "EmbersOfGlory";
	private static final double DAMAGE = 0.1;

	public EmbersOfGlory() {
		super(ID, "Embers of Glory", Rarity.COMMON, EquipmentClass.CLASSLESS);
		markAsStartingBonus();
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT),
				Buff.multiplier(data, DAMAGE, StatTracker.damageBuffAlly(id, this)));
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER, "Increase " + GlossaryTag.DIRECT.tag(this) + " damage by "
				+ DescUtil.val((int) (DAMAGE * 100) + "%") + ".");
	}
}