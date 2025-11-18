package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.PotionProjectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class MinorFirePotion extends Consumable {
	private static final String ID = "MinorFirePotion";
	private int damage;
	
	public MinorFirePotion(boolean isUpgraded) {
		super(ID, "Minor Fire Potion", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS);
		damage = isUpgraded ? 500 : 300;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void runConsumableEffects(Player p, PlayerFightData data) {
		PotionProjectile pot = new PotionProjectile((loc, hit) -> {
			for (LivingEntity ent : hit) {
				if (ent instanceof Player || !(ent instanceof LivingEntity)) continue;
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE, DamageStatTracker.of(id, this)), ent);
			}
			Sounds.explode.play(p, loc);
		});
		ProjectileGroup grp = new ProjectileGroup(pot);
		
		Sounds.threw.play(p, p);
		grp.start(data);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Throw a potion that deals " + GlossaryTag.FIRE.tag(this, damage, true) + " to all nearby enemies. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 0, 0));
		item.setItemMeta(meta);
	}
}
