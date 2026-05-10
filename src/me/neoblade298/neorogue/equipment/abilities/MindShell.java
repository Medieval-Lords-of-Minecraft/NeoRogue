package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MindShell extends Equipment {
	private static final String ID = "MindShell";
	private double regen;
	private int shell;
	private static final int THRES = 3;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);

	public MindShell(boolean isUpgraded) {
		super(ID, "Mind Shell", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(25, 5, 0, 0));
		regen = 0.3;
		shell = isUpgraded ? 2 : 1;
	}

	@Override
	public void setupReforges() {

	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			ActionMeta am = new ActionMeta();
			data.addTrigger(id, Trigger.CAST_USABLE, (pdata2, in2) -> {
				am.addCount(1);
				if (am.getCount() >= THRES) {
					am.addCount(-THRES);
					pdata2.addManaRegen(regen);
					data.applyStatus(StatusType.SHELL, data, shell, -1);
					Player p = data.getPlayer();
					pc.play(p, p);
					Sounds.enchant.play(p, p);
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHT_BLUE_DYE,
				GlossaryTag.POWER.tag(this) + ". Every " + DescUtil.white(THRES) + " ability casts, increase your mana regen by "
						+ DescUtil.white(regen) + " and gain " + GlossaryTag.SHELL.tag(this, shell, true) + ".");
	}
}
