package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class GlacialHammer extends Artifact {
	private static final String ID = "GlacialHammer";

	public GlacialHammer() {
		super(ID, "Glacial Hammer", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (data.getMana() <= data.getMaxMana() * 0.5) {
				return TriggerResult.keep();
			}
			if (am.addCount(1) < 5) {
				return TriggerResult.keep();
			}
			am.setCount(0);
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getTarget().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0));
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
		item = createItem(Material.GOLDEN_SHOVEL, 
				"Every " + DescUtil.val(5) + " basic attacks while you're above " + DescUtil.val("50%") + " mana gives the enemy slowness " + DescUtil.val(1)
				+ " [<white>1s</white>].");
	}
}
