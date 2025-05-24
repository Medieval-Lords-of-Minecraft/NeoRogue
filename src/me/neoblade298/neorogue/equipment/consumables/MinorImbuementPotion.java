package me.neoblade298.neorogue.equipment.consumables;

import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.UseConsumableEvent;

public class MinorImbuementPotion extends Consumable {
	private static final String ID = "minorImbuementPotion";
	private int damage;
	
	public MinorImbuementPotion(boolean isUpgraded) {
		super(ID, "Minor Imbuement Potion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		this.damage = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ImbuementPotionMeta meta = new ImbuementPotionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.RIGHT_CLICK, (pdata, in) -> {
			drink.play(p, p);
			data.getSessionData().removeEquipment(es, slot);
			p.getInventory().setItem(slot, null);
			data.runActions(data, Trigger.USE_CONSUMABLE, new UseConsumableEvent(this));
			meta.use();
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in2) -> {
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in2;
				ev.getMeta().addDamageSlice(new DamageSlice(data, damage, meta.getElement()));
				return TriggerResult.keep();
			});
			return TriggerResult.remove();
		});

		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			if (meta.isUsed()) {
				return TriggerResult.remove();
			}
			meta.toggleElement();
			Util.msg(p, "Element changed to " + meta.getElement().getDisplay());
			return TriggerResult.keep();
		});
	}

	private static class ImbuementPotionMeta {
		private static HashMap<DamageType, DamageType> elements = new HashMap<DamageType, DamageType>();
		private DamageType curr = DamageType.FIRE;
		private boolean used = false;
		static {
			elements.put(DamageType.FIRE, DamageType.ICE);
			elements.put(DamageType.ICE, DamageType.LIGHTNING);
			elements.put(DamageType.LIGHTNING, DamageType.EARTHEN);
			elements.put(DamageType.EARTHEN, DamageType.LIGHT);
			elements.put(DamageType.LIGHT, DamageType.DARK);
			elements.put(DamageType.DARK, DamageType.FIRE);
		}
		public void toggleElement() {
			curr = elements.get(curr);
		}

		public DamageType getElement() {
			return curr;
		}

		public void use() {
			used = true;
		}

		public boolean isUsed() {
			return used;
		}
	}
	
	@Override
	public void runConsumableEffects(Player p, PlayerFightData data) {
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Right click with this in hand to consume, granting " + DescUtil.yellow(damage) + " bonus elemental damage on your basic attacks."
		+ " Left click to change the element. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 0, 0));
		item.setItemMeta(meta);
	}
}
