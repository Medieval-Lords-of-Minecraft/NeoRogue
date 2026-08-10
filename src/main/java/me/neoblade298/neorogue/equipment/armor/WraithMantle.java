package me.neoblade298.neorogue.equipment.armor;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class WraithMantle extends Equipment {
	private static final String ID = "WraithMantle";
	private static final int INSANITY_THRESHOLD = 150, CLEAR_RADIUS = 4;
	private static final ParticleContainer CLEAR_PARTICLE = new ParticleContainer(Particle.SOUL)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleAnimation CLEAR_SPHERE;
	private final int damageReduction;

	static {
		CLEAR_SPHERE = new ParticleAnimation(CLEAR_PARTICLE, (loc, tick) -> {
			LinkedList<Location> locations = new LinkedList<Location>();
			double latitude = -Math.PI / 2 + Math.PI * tick / 6;
			double ringRadius = CLEAR_RADIUS * Math.cos(latitude);
			double y = CLEAR_RADIUS * Math.sin(latitude);
			int points = Math.max(1, (int) Math.round(20 * Math.cos(latitude)));
			for (int point = 0; point < points; point++) {
				double angle = Math.PI * 2 * point / points + tick * 0.25;
				locations.add(loc.clone().add(Math.cos(angle) * ringRadius, y,
						Math.sin(angle) * ringRadius));
			}
			return locations;
		}, 7);
	}

	public WraithMantle(boolean isUpgraded) {
		super(ID, "Wraith Mantle", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ARMOR);
		damageReduction = isUpgraded ? 6 : 4;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, damageReduction,
				StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
		ActionMeta insanityApplied = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INSANITY) || ev.getStacks() <= 0) return TriggerResult.keep();
			if (insanityApplied.getCount() >= INSANITY_THRESHOLD) {
				Player player = data.getPlayer();
				ProjectileInstance.cancelWithin(data.getInstance(), player.getLocation(), CLEAR_RADIUS);
				playProjectileClearFx(data, player);
			}
			insanityApplied.addCount(ev.getStacks());
			return TriggerResult.keep();
		});
	}

	private void playProjectileClearFx(PlayerFightData data, Player player) {
		data.runAnimation(id + "-clear", player, CLEAR_SPHERE, player.getLocation());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_CHESTPLATE, "Reduce " + GlossaryTag.MAGICAL.tag(this) + " damage by "
				+ DescUtil.yellow(damageReduction) + ". After applying " + GlossaryTag.INSANITY.tag(this, INSANITY_THRESHOLD)
				+ ", each subsequent application clears nearby projectiles within " + DescUtil.white(CLEAR_RADIUS) + " blocks.");
	}
}