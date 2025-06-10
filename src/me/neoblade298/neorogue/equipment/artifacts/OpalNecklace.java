package me.neoblade298.neorogue.equipment.artifacts;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class OpalNecklace extends Artifact {
	private static final String ID = "opalNecklace";
	private double inc = 0.3;
	private int displayInc = (int) (inc * 100);

	public OpalNecklace() {
		super(ID, "Opal Necklace", Rarity.UNCOMMON, EquipmentClass.MAGE);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, new OpalNecklaceInstance(this, id));
	}

	private class OpalNecklaceInstance extends PriorityAction {
		private DamageType lastType;

		public OpalNecklaceInstance(Equipment eq, String id) {
			super(id);
			String buffId = UUID.randomUUID().toString();
			action = (pdata, in) -> {
				PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
				// Only trigger on magical damage
				if (!ev.getMeta().getPrimarySlice().getPostBuffType().getCategories().contains(DamageCategory.MAGICAL))
					return TriggerResult.keep();
				DamageType curr = ev.getMeta().getPrimarySlice().getPostBuffType();
				if (lastType != curr && lastType != null) {
					ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
							Buff.multiplier(pdata, inc, StatTracker.damageBuffAlly(buffId, eq)));
				}
				lastType = curr;
				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_NUGGET,
				"Whenever you deal " + GlossaryTag.MAGICAL.tag(this)
						+ " damage that is different from your previous damage's main type, increase its damage by "
						+ DescUtil.yellow(displayInc + "%") + ".");
	}
}
