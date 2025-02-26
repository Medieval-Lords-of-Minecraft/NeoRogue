package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class CrackedCrystal extends Artifact {
	private static final String ID = "crackedCrystal";
	private static final int buff = 20;

	public CrackedCrystal() {
		super(ID, "Cracked Crystal", Rarity.UNCOMMON, EquipmentClass.MAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			DamageType prev = (DamageType) am.getObject();
			DamageType curr = ev.getMeta().getPrimarySlice().getType();
			if (curr.getCategories().contains(DamageCategory.MAGICAL) && curr != prev) {
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.multiplier(data, buff * 0.01, StatTracker.damageBuffAlly(this)));
			}
			am.setObject(curr);
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
		item = createItem(Material.GLISTERING_MELON_SLICE, 
				"Dealing " + GlossaryTag.MAGICAL.tag(this) + " damage that is a different type than your last damage increases it by " + DescUtil.white(buff + "%") + ".");
	}
}
