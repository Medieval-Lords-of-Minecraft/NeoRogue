package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.ShieldHolder;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class GoldenAegis extends Equipment {
	private static final String ID = "GoldenAegis";
	private int uses;
	
	public GoldenAegis(boolean isUpgraded) {
		super(ID, "Golden Aegis", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(0, 0, 15, 0));
		uses = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// Use right click for non-archers, left click for archers
		Trigger tr = data.getSessionData().getPlayerClass() == EquipmentClass.ARCHER ? Trigger.LEFT_CLICK : Trigger.RIGHT_CLICK;
		
		ItemStack icon = item.clone();
		icon.setAmount(uses);
		ActionMeta am = new ActionMeta();
		am.setCount(uses);
		
		data.addTrigger(id, tr, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			ShieldHolder shieldHolder = data.getShields();
			
			// Get current shield amount
			double currentShields = shieldHolder.getAmount();
			
			// Only proceed if there are shields to convert
			if (currentShields > 0) {
				// Remove all current shields by clearing the amount
				// We need to manually clear shields since there's no public method
				// Get all shields and remove them one by one
				while (!shieldHolder.isEmpty()) {
					// Remove shields by setting amount to 0
					shieldHolder.useShields(shieldHolder.getAmount());
				}
				
				// Add permanent shields
				data.addPermanentShield(p.getUniqueId(), currentShields);
				
				// Sound effect
				Sounds.levelup.play(p, p);
				
				// Swing offhand for archer
				if (tr == Trigger.LEFT_CLICK) {
					p.swingOffHand();
				}
				
				// Decrement use count
				am.addCount(-1);
				if (am.getCount() <= 0) {
					Sounds.breaks.play(p, p);
					p.getInventory().setItemInOffHand(null);
					return TriggerResult.remove();
				}
				else {
					icon.setAmount(am.getCount());
					p.getInventory().setItemInOffHand(icon);
					return TriggerResult.keep();
				}
			}
			
			return TriggerResult.keep();
		}));

		// The icon has to be after new EquipmentInstance because the constructor updates the icon
		data.getPlayer().getInventory().setItemInOffHand(icon);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_APPLE,
				"On right click (left click for <gold>Archer</gold>), convert all current " + 
				GlossaryTag.SHIELDS.tag(this) + " to permanent " + GlossaryTag.SHIELDS.tag(this) + ". Usable " +
				DescUtil.yellow((isUpgraded ? "twice" : "once")) + " per fight.");
	}
}
