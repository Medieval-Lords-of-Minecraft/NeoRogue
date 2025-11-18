package me.neoblade298.neorogue.equipment.artifacts;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Anxiety extends Artifact {
	private static final String ID = "Anxiety";
	
	public Anxiety() {
		super(ID, "Anxiety", Rarity.COMMON, EquipmentClass.CLASSLESS);
		canDrop = false;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		String buffId = UUID.randomUUID().toString();
		data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.multiplier(data, -0.25, StatTracker.damageDebuffAlly(buffId, this)), 20 * 20);
		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			data.getSessionData().removeArtifact(this);
			return TriggerResult.remove();
		});
	}
	
	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.DEAD_FIRE_CORAL, "Decreases damage by <white>25%</white> for the first <white>20s</white>. Disappears after <white>1</white> fight.");
	}
}
