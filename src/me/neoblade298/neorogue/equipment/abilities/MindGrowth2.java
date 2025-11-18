package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MindGrowth2 extends Equipment {
	private static final String ID = "MindGrowth2";
	private double regen;
	private int maxMana;
	private static final int THRES = 3;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);

	public MindGrowth2(boolean isUpgraded) {
		super(ID, "Mind Growth II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.none());
		regen = isUpgraded ? 0.5 : 0.4;
		maxMana = isUpgraded ? 10 : 5;
	}

	@Override
	public void setupReforges() {

	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() >= THRES) {
				am.addCount(-THRES);
				pdata.addManaRegen(regen);
				pdata.addMaxMana(maxMana);
				pc.play(p, p);
				Sounds.enchant.play(p, p);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHT_BLUE_DYE,
				"Passive. Every " + DescUtil.white(THRES) + " ability casts, increase your mana regen by "
						+ DescUtil.yellow(regen) + " and your max mana by " + DescUtil.yellow(maxMana) + ".");
	}
}
