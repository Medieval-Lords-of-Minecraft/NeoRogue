package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MindGrowth2 extends Equipment implements Power {
	private static final String ID = "MindGrowth2";
	private double regen;
	private int maxMana;
	private static final int THRES = 3;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);

	public MindGrowth2(boolean isUpgraded) {
		super(ID, "Mind Growth II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
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
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta am = new ActionMeta();
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.CAST_USABLE, (pdata2, in2) -> {
					am.addCount(1);
					if (am.getCount() >= THRES) {
						am.addCount(-THRES);
						pdata2.addManaRegen(regen);
						pdata2.addMaxMana(maxMana);
						Player p2 = data.getPlayer();
						pc.play(p2, p2);
						Sounds.enchant.play(p2, p2);
					}
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.LIGHT_BLUE_DYE,
				GlossaryTag.POWER.tag(this) + ". Activates after casting an ability. Every " + DescUtil.white(THRES) + " ability casts, increase your mana regen by "
						+ DescUtil.yellow(regen) + " and your max mana by " + DescUtil.yellow(maxMana) + ".");
	}
}
