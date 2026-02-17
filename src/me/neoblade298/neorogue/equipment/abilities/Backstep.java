package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Backstep extends Equipment {
	private static final String ID = "Backstep";
	private int shields;
	
	public Backstep(boolean isUpgraded) {
		super(ID, "Backstep", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 1 : 5, 8, 0));
		properties.addUpgrades(PropertyType.STAMINA_COST);
		shields = isUpgraded ? 6 : 3;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				"On cast, jump backwards, gain " + DescUtil.potion("Speed", 0, 3) + ", and gain " +
				GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>].");
	}

	public void setupReforges() {
		addReforge(AgilityTraining.get(), RecklessApproach.get(), Backstep2.get(), BlastStep.get());
		addReforge(KeenSenses.get(), Surprise.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Vector v = p.getEyeLocation().getDirection();
			if (p.isOnGround()) {
				p.teleport(p.getLocation().add(0, 0.2, 0));
			}
			p.setVelocity(v.setY(0).setX(-v.getX()).setZ(-v.getZ()).normalize().multiply(0.7).setY(0.3));
			Sounds.jump.play(p, p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			return TriggerResult.keep();
		}));
	}
}
