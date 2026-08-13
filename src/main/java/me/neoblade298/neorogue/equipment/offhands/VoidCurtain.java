package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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

public class VoidCurtain extends Equipment {
	private static final String ID = "VoidCurtain";
	private static final Circle PROTECT_RING = new Circle(1.05), SHELL_RING = new Circle(1.35);
	private static final ParticleContainer PROTECT_PARTICLE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(245, 205, 95), 1.1F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer SHELL_PARTICLE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(105, 190, 255), 1.1F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer VOID_BURST = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(10).spread(0.1, 0.1).speed(0.01).offsetY(1);
	private int uses;

	public VoidCurtain(boolean isUpgraded) {
		super(ID, "Void Curtain", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(60, 10, 15, 0));
		uses = isUpgraded ? 2 : 1;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta used = new ActionMeta();
		EquipmentInstance instance = new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player player = data.getPlayer();
			int protect = data.hasStatus(StatusType.PROTECT) ? data.getStatus(StatusType.PROTECT).getStacks() : 0;
			int shell = data.hasStatus(StatusType.SHELL) ? data.getStatus(StatusType.SHELL).getStacks() : 0;
			if (protect > 0) {
				data.applyStatus(StatusType.PROTECT, data, protect, -1, this);
				PROTECT_RING.play(PROTECT_PARTICLE, player.getLocation().add(0, 0.35, 0), LocalAxes.xz(), null);
			}
			if (shell > 0) {
				data.applyStatus(StatusType.SHELL, data, shell, -1, this);
				SHELL_RING.play(SHELL_PARTICLE, player.getLocation().add(0, 0.65, 0), LocalAxes.xz(), null);
			}
			used.addCount(1);
			VOID_BURST.play(player, player);
			Sounds.block.play(player, player);
			Sounds.enchant.play(player, player);
			return TriggerResult.keep();
		}, (player, pdata, in) -> used.getCount() < uses);
		data.addTrigger(id, Trigger.RIGHT_CLICK, instance);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "On use, gain permanent " + GlossaryTag.PROTECT.tag(this)
				+ " and " + GlossaryTag.SHELL.tag(this) + " equal to your current respective stacks. Usable "
				+ DescUtil.val(uses) + (uses == 1 ? " time" : " times") + " per fight.");
	}
}