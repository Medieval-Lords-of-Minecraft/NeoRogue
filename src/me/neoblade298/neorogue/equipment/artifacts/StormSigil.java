package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
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

public class StormSigil extends Artifact {
	private static final String ID = "stormSigil";
	private static final int damage = 15, secs = 5;

	public StormSigil() {
		super(ID, "Storm Sigil", Rarity.UNCOMMON, EquipmentClass.ARCHER);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.CHANGE_AMMUNITION, (pdata, in) -> {
			data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
				Buff.increase(data, damage, StatTracker.damageBuffAlly(this)), secs * 20);
			return TriggerResult.keep();
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
		item = createItem(Material.DISC_FRAGMENT_5, 
				"Changing to a different ammunition buffs your damage by " + DescUtil.yellow(damage) + " " + DescUtil.duration(secs, false) + ".");
	}
}
