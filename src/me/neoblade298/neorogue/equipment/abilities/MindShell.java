package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MindShell extends Equipment {
	private static final String ID = "MindShell";
	private double regen;
	private int shell;
	private static final int THRES = 3;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);

	public MindShell(boolean isUpgraded) {
		super(ID, "Mind Shell", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
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
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			ActionMeta am = new ActionMeta();
			data.addTask(new BukkitRunnable() {
				public void run() {
					data.addTrigger(id + "-active", Trigger.CAST_USABLE, (pdata2, in2) -> {
						am.addCount(1);
						if (am.getCount() >= THRES) {
							am.addCount(-THRES);
							pdata2.addManaRegen(regen);
							data.applyStatus(StatusType.SHELL, data, shell, -1);
							Player p2 = data.getPlayer();
							pc.play(p2, p2);
							Sounds.enchant.play(p2, p2);
						}
						return TriggerResult.keep();
					});
				}
			}.runTask(NeoRogue.inst()));

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHT_BLUE_DYE,
				GlossaryTag.POWER.tag(this) + ". Activates after casting an ability. Every " + DescUtil.white(THRES) + " ability casts, increase your mana regen by "
						+ DescUtil.white(regen) + " and gain " + GlossaryTag.SHELL.tag(this, shell, true) + ".");
	}
}
