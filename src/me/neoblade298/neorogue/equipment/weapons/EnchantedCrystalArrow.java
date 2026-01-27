package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class EnchantedCrystalArrow extends Ammunition {
	private static final String ID = "EnchantedCrystalArrow";
	private static final int BONUS_DAMAGE = 20;
	private int frost;
	
	public EnchantedCrystalArrow(boolean isUpgraded) {
		super(ID, "Enchanted Crystal Arrow", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(10, 0.1, DamageType.ICE));
		frost = isUpgraded ? 40 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity trg) {
		FightData fd = FightInstance.getFightData(trg);
		
		// Deal bonus damage if target has Frost
		if (fd.hasStatus(StatusType.FROST)) {
			meta.addDamageSlice(new DamageSlice(inst.getOwner(), BONUS_DAMAGE, DamageType.ICE, DamageStatTracker.of(id, this)));
		}
		
		// Apply additional frost
		fd.applyStatus(StatusType.FROST, inst.getOwner(), frost, -1);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TIPPED_ARROW,
				"Deals <white>" + BONUS_DAMAGE + "</white> more damage to enemies with " + GlossaryTag.FROST.tag(this) + 
				". Applies " + GlossaryTag.FROST.tag(this, frost, true) + " on hit.");
		PotionMeta pm = (PotionMeta) item.getItemMeta();
		pm.setColor(Color.AQUA);
		item.setItemMeta(pm);
	}
}
