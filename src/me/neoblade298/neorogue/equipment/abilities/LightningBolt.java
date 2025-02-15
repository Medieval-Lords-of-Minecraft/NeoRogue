package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class LightningBolt extends Equipment {
	private static final String ID = "lightningBolt";
	private static final ParticleContainer tick = new ParticleContainer(Particle.FIREWORK).count(3).spread(0.3, 0.3);

	private int damage, burn;
	
	public LightningBolt(boolean isUpgraded) {
		super(
				ID , "Lightning Bolt", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 0, 12, 10));
		damage = isUpgraded ? 210 : 140;
	}

	@Override
	public void setupReforges() {
		addReforge(Manabending.get(), Fireball2.get(), Torch.get(), Fireblast.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata ,in) -> {
			data.channel(20).then(new Runnable() {
				public void run() {
					Location start = p.getLocation().add(0, 1, 0);
					Vector dir = p.getEyeLocation().getDirection();
					Location end = start.clone().add(dir.clone().multiply(properties.get(PropertyType.RANGE)));
					ParticleUtil.drawLine(p, tick, start, end, 0.3);
				}
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.YELLOW_DYE,
			GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before launching a fireball that deals " + GlossaryTag.FIRE.tag(this, damage, true) + " damage but apply " +
			GlossaryTag.BURN.tag(this, burn, false) + " to yourself.");
	}
}
