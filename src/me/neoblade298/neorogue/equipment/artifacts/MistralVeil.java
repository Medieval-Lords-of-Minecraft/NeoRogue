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
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class MistralVeil extends Artifact {
	private static final String ID = "mistralVeil";
	private static final int range = 7, reduc = 6, rangeSq = range * range;

	public MistralVeil() {
		super(ID, "Mistral Veil", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			ReceivedDamageEvent ev = (ReceivedDamageEvent) in;
			if (p.getLocation().distanceSquared(ev.getDamager().getEntity().getLocation()) > rangeSq) {
				ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, reduc, 0, StatTracker.defenseBuffAlly(this)));
			}
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
		item = createItem(Material.CHERRY_SAPLING, 
				"Damage received from an enemy over " + DescUtil.white(range) + " blocks away is reduced by " + DescUtil.white(reduc) + ".");
	}
}
