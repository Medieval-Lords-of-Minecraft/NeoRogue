package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SorcerersTome extends Equipment {
	private static final String ID = "SorcerersTome";
	private static final int CHANNEL_TICKS = 20, STATUS_DURATION = 3, CHANNEL_BYPASS = 12;
	private static final Circle CHANNEL_RING = new Circle(1.1);
	private static final ParticleContainer CHANNEL_PARTICLE = new ParticleContainer(Particle.ENCHANT)
			.count(8).spread(0.1, 0.1).speed(0).offsetY(0.8);
	private static final ParticleContainer BUFF_RING = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(115, 205, 255), 1.05F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer BUFF_BURST = new ParticleContainer(Particle.FIREWORK)
			.count(12).spread(0.1, 0.1).speed(0.01).offsetY(1);
	private int intellect, shields;

	public SorcerersTome(boolean isUpgraded) {
		super(ID, "Sorcerer's Tome", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(35, 0, 15, 0));
		intellect = isUpgraded ? 6 : 4;
		shields = isUpgraded ? 5 : 3;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.RIGHT_CLICK, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			int currentIntellect = data.hasStatus(StatusType.INTELLECT) ? data.getStatus(StatusType.INTELLECT).getStacks() : 0;
			Runnable grant = () -> {
				Player player = data.getPlayer();
				data.applyStatus(StatusType.INTELLECT, data, intellect, STATUS_DURATION * 20, this);
				data.addSimpleShield(player.getUniqueId(), shields, STATUS_DURATION * 20, this);
				CHANNEL_RING.play(BUFF_RING, player.getLocation().add(0, 0.2, 0), LocalAxes.xz(), null);
				BUFF_BURST.play(player, player);
				Sounds.enchant.play(player, player);
			};
			if (currentIntellect >= CHANNEL_BYPASS) grant.run();
			else {
				Sounds.turnPage.play(data.getPlayer(), data.getPlayer());
				data.addTask(new BukkitRunnable() {
					private int ticks;

					@Override
					public void run() {
						Player player = data.getPlayer();
						CHANNEL_PARTICLE.play(player, player);
						if (++ticks >= 5) cancel();
					}
				}.runTaskTimer(NeoRogue.inst(), 0L, 4L));
				data.channel(CHANNEL_TICKS).then(grant);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK, "On use, " + GlossaryTag.CHANNEL.tag(this) + " for "
				+ DescUtil.white("1s") + " to gain " + GlossaryTag.INTELLECT.tag(this, intellect) + " and "
				+ GlossaryTag.SHIELDS.tag(this, shields) + " " + DescUtil.duration(STATUS_DURATION)
				+ ". Skip the channel at or above " + GlossaryTag.INTELLECT.tag(this, CHANNEL_BYPASS) + ".");
	}
}