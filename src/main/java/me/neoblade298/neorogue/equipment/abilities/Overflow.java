package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Overflow extends Equipment {
	private static final String ID = "Overflow";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.BLUE, 1));
	private double reduc = isUpgraded ? 0.1 : 0.3;

	public Overflow(boolean isUpgraded) {
		super(ID, "Overflow", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {

	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "On cast, set your mana to your max mana and decrease your mana regen by "
				+ DescUtil.yellow(reduc) + ". Can only be used once per fight.");
		PotionMeta pm = (PotionMeta) item.getItemMeta();
		pm.setColor(Color.BLUE);
		item.setItemMeta(pm);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.infect.play(p, p);
			pc.play(p, p);
			data.setMana(data.getMaxMana());
			data.addManaRegen(-reduc);
			p.getInventory().setItem(slot, null);
			return TriggerResult.remove();
		}));
	}
}
