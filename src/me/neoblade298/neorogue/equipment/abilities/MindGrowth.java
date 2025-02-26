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

public class MindGrowth extends Equipment {
	private static final String ID = "mindGrowth";
	private double regen;
	private static final int THRES = 5;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	
	public MindGrowth(boolean isUpgraded) {
		super(ID, "Mind Growth", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		regen = isUpgraded ? 0.3 : 0.2;
	}
	
	@Override
	public void setupReforges() {
		addReforge(Manabending.get(), MindBlast.get());
		addReforge(CalculatingGaze.get(), MindShell.get());
		addReforge(Intuition.get(), MindGrowth2.get());
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
				pc.play(p, p);
				Sounds.enchant.play(p, p);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHT_BLUE_DYE,
				"Passive. Every " + DescUtil.white(THRES) + " ability casts, increase your mana regen by " + DescUtil.yellow(regen) + ".");
	}
}
