package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EnGarde extends Equipment {
	private static final String ID = "EnGarde";
	private static final int DURATION_SECONDS = 5;
	private static final int DURATION_TICKS = DURATION_SECONDS * 20;
	private static final Circle STANCE_RING = new Circle(0.9);
	private static final ParticleContainer STANCE_PARTICLE = new ParticleContainer(Particle.DUST).count(1)
			.spread(0, 0).speed(0).dustOptions(new DustOptions(Color.fromRGB(185, 195, 205), 1F));
	private static final SoundContainer STANCE_SOUND = new SoundContainer(Sound.ITEM_SHIELD_BLOCK, 0.4F, 1.25F);
	private final int strength;
	private final int shields;

	public EnGarde(boolean isUpgraded) {
		super(ID, "En Garde", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		strength = isUpgraded ? 3 : 2;
		shields = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String taskId = id + slot;
		data.addTrigger(id, Trigger.TOGGLE_CROUCH, (pdata, in) -> {
			PlayerToggleSneakEvent event = (PlayerToggleSneakEvent) in;
			if (!event.isSneaking()) {
				data.removeAndCancelTask(taskId);
				return TriggerResult.keep();
			}

			data.addTask(taskId, new BukkitRunnable() {
				@Override
				public void run() {
					Player player = data.getPlayer();
					STANCE_RING.play(STANCE_PARTICLE, player.getLocation().add(0, 0.1, 0), LocalAxes.xz(), null);
					STANCE_SOUND.play(player, player);
					data.applyStatus(StatusType.STRENGTH, data, strength, DURATION_TICKS, EnGarde.this);
					data.addSimpleShield(player.getUniqueId(), shields, DURATION_TICKS, EnGarde.this);
				}
			}.runTaskTimer(NeoRogue.inst(), 40L, 40L));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_BARS,
				GlossaryTag.PASSIVE.tag(this) + ". While crouching, every 2 seconds gain "
						+ GlossaryTag.STRENGTH.tag(this, strength) + " " + DescUtil.duration(DURATION_SECONDS)
						+ " and " + GlossaryTag.SHIELDS.tag(this, shields) + " "
						+ DescUtil.duration(DURATION_SECONDS) + ".");
	}
}