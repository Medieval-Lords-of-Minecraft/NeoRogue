package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MechanicDisable implements ITargetedEntitySkill {
	private final double seconds;

	@Override
	public ThreadSafetyLevel getThreadSafetyLevel() {
		return ThreadSafetyLevel.SYNC_ONLY;
	}

	public MechanicDisable(MythicLineConfig config) {
		this.seconds = config.getDouble(new String[] { "s", "seconds" }, 5);
	}

	@Override
	public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			if (!(target.getBukkitEntity() instanceof Player)) return SkillResult.INVALID_TARGET;
			Player p = (Player) target.getBukkitEntity();
			PlayerFightData pdata = FightInstance.getUserData(p.getUniqueId());
			if (pdata == null) return SkillResult.INVALID_TARGET;

			// Collect candidates: castable abilities + weapons
			ArrayList<DisableCandidate> candidates = new ArrayList<>();
			HashSet<PriorityAction> castableActions = new HashSet<>();

			// Castable abilities from getActiveEquipment
			for (Map.Entry<String, EquipmentInstance> entry : pdata.getActiveEquipment().entrySet()) {
				EquipmentInstance ei = entry.getValue();
				EquipSlot es = ei.getEquipSlot();
				if (es != EquipSlot.HOTBAR && es != EquipSlot.KEYBIND) continue;

				Trigger triggerKey;
				if (es == EquipSlot.HOTBAR) {
					triggerKey = Trigger.getFromHotbarSlot(ei.getSlot());
				} else {
					triggerKey = KeyBind.getBindFromData(ei.getSlot()).getTrigger();
				}
				if (triggerKey == null) continue;

				ArrayList<PriorityAction> list = pdata.getTriggers().get(triggerKey);
				if (list == null || !list.contains(ei)) continue;

				int invSlot = EquipSlot.convertSlot(es, ei.getSlot());
				DisableCandidate c = new DisableCandidate(invSlot);
				c.add(ei, list);
				candidates.add(c);
				castableActions.add(ei);
			}

			// Weapons from slotBasedTriggers. All actions bound to a single slot belong to the same
			// weapon (e.g. a basic attack on LEFT_CLICK_HIT and a dash on RIGHT_CLICK), so they must be
			// disabled together — otherwise disabling one action leaves the weapon's other action usable.
			for (Map.Entry<Integer, HashMap<Trigger, ArrayList<PriorityAction>>> slotEntry : pdata.getSlotBasedTriggers().entrySet()) {
				int slot = slotEntry.getKey();
				DisableCandidate c = new DisableCandidate(slot);
				for (Map.Entry<Trigger, ArrayList<PriorityAction>> trigEntry : slotEntry.getValue().entrySet()) {
					for (PriorityAction pa : trigEntry.getValue()) {
						// Skip if already added as a castable
						if (castableActions.contains(pa)) continue;
						c.add(pa, trigEntry.getValue());
					}
				}
				if (!c.isEmpty()) candidates.add(c);
			}

			if (candidates.isEmpty()) return SkillResult.CONDITION_FAILED;

			// Pick a random candidate
			DisableCandidate chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));

			// Remove every PriorityAction in the candidate from its list
			for (int i = 0; i < chosen.actions.size(); i++) {
				chosen.lists.get(i).remove(chosen.actions.get(i));
				// Suppress icon updates so a finishing cooldown can't override the barrier placeholder
				if (chosen.actions.get(i) instanceof EquipmentInstance) {
					((EquipmentInstance) chosen.actions.get(i)).setDisabled(true);
				}
			}
			// Prevent the player from switching to the disabled slot
			pdata.setSlotDisabled(chosen.invSlot, true);

			// Save original item and set barrier
			ItemStack original = p.getInventory().getItem(chosen.invSlot);
			p.getInventory().setItem(chosen.invSlot, new ItemStack(Material.BARRIER));
			p.setCooldown(Material.BARRIER, (int) (seconds * 20));

			// Send disable message
			Component name = chosen.getDisplayName();
			p.sendMessage(Component.text("").append(name).append(Component.text(" was disabled for " + (int) seconds + "s!", NamedTextColor.RED)));

			// Schedule revert
			int ticks = (int) (seconds * 20);
			DisableCandidate finalChosen = chosen;
			pdata.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					boolean hasEquipmentInstance = false;
					for (int i = 0; i < finalChosen.actions.size(); i++) {
						finalChosen.lists.get(i).add(finalChosen.actions.get(i));
						if (finalChosen.actions.get(i) instanceof EquipmentInstance) {
							EquipmentInstance ei = (EquipmentInstance) finalChosen.actions.get(i);
							ei.setDisabled(false);
							ei.updateIcon();
							hasEquipmentInstance = true;
						}
					}
					pdata.setSlotDisabled(finalChosen.invSlot, false);
					// EquipmentInstances restore their own icon via updateIcon(); a plain (lambda) weapon
					// slot has no managed icon, so put the saved item back manually.
					if (!hasEquipmentInstance) {
						pdata.getPlayer().getInventory().setItem(finalChosen.invSlot, original);
					}
				}
			}.runTaskLater(NeoRogue.inst(), ticks));

			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
	}

	// Represents one disable target. A castable ability holds a single action; a weapon holds every
	// slot-based action bound to its inventory slot so they're all disabled and restored together.
	private static class DisableCandidate {
		final ArrayList<PriorityAction> actions = new ArrayList<>();
		final ArrayList<ArrayList<PriorityAction>> lists = new ArrayList<>();
		final int invSlot;

		DisableCandidate(int invSlot) {
			this.invSlot = invSlot;
		}

		void add(PriorityAction action, ArrayList<PriorityAction> list) {
			actions.add(action);
			lists.add(list);
		}

		boolean isEmpty() {
			return actions.isEmpty();
		}

		// Prefer an EquipmentInstance's display name; fall back to the first action's equipment/id.
		Component getDisplayName() {
			for (PriorityAction pa : actions) {
				if (pa instanceof EquipmentInstance) {
					return ((EquipmentInstance) pa).getEquipment().getHoverable();
				}
			}
			PriorityAction first = actions.get(0);
			Equipment eq = Equipment.get(first.getId(), false);
			return eq != null ? eq.getHoverable() : Component.text(first.getId(), NamedTextColor.RED);
		}
	}
}
