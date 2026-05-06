package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class MirrorPotion extends Consumable {
	private static final String ID = "MirrorPotion";
	private static final long DELAY = 20L;

	public MirrorPotion(boolean isUpgraded) {
		super(ID, "Mirror Potion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		int[] remaining = { isUpgraded ? 2 : 1 };
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent ev = (CastUsableEvent) in;
			EquipmentInstance inst = ev.getInstance();
			CastType type = ev.getType();
			if (inst.getEquipment().getType() != EquipmentType.ABILITY) {
				return TriggerResult.keep();
			}
			if (type != CastType.STANDARD && type != CastType.POST_TRIGGER) {
				return TriggerResult.keep();
			}

			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					Player p2 = data.getPlayer();
					Sounds.success.play(p2, p2);
					inst.trigger(data, ev.getInputs());
					inst.updateIcon();
				}
			}.runTaskLater(NeoRogue.inst(), DELAY));
			return --remaining[0] <= 0 ? TriggerResult.remove() : TriggerResult.keep();
		});
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, isUpgraded
				? "Your next [<white>2</white>] ability casts are each cast again for free after [<white>1s</white>]. Consumed on first use."
				: "Your next ability cast is cast again for free after [<white>1s</white>]. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(186, 85, 211));
		item.setItemMeta(meta);
	}
}