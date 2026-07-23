package me.neoblade298.neorogue.equipment.abilities;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Dissonance extends Equipment implements Power {
	private static final String ID = "Dissonance";
	private static final int ACTIVATION_THRES = 3;
	private int mana, shields;
	
	public Dissonance(boolean isUpgraded) {
		super(ID, "Dissonance", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		mana = isUpgraded ? 8 : 5;
		shields = isUpgraded ? 4 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.KNOWLEDGE_BOOK,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after dealing " + DescUtil.val(ACTIVATION_THRES) +
				" different damage types. Anytime you deal a damage type that is different from your previous damage type, " +
				"gain " + DescUtil.val(mana) + " mana and " + GlossaryTag.SHIELDS.tag(this, shields) +
				" [" + DescUtil.val("5s") + "].");
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		HashSet<DamageType> seenTypes = new HashSet<>();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().getSlices().isEmpty()) return TriggerResult.keep();
			DamageSlice primarySlice = ev.getMeta().getSlices().getFirst();
			DamageType currentType = primarySlice.getPostBuffType();
			seenTypes.add(currentType);
			if (seenTypes.size() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta lastDamageType = new ActionMeta();
		data.addTrigger(id + "-active", Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			Player p = data.getPlayer();
			
			if (ev.getMeta().getSlices().isEmpty()) return TriggerResult.keep();
			DamageSlice primarySlice = ev.getMeta().getSlices().getFirst();
			DamageType currentType = primarySlice.getPostBuffType();
			
			DamageType lastType = (DamageType) lastDamageType.getObject();
			if (lastType == null || currentType != lastType) {
				data.addMana(mana);
				data.addSimpleShield(p.getUniqueId(), shields, 100, this);
				lastDamageType.setObject(currentType);
			}
			
			return TriggerResult.keep();
		});
	}
}
