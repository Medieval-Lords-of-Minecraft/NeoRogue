package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.ArrayList;
import java.util.HashMap;
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
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

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
				candidates.add(new DisableCandidate(ei, list, invSlot, false));
			}

			// Weapons from slotBasedTriggers
			for (Map.Entry<Integer, HashMap<Trigger, ArrayList<PriorityAction>>> slotEntry : pdata.getSlotBasedTriggers().entrySet()) {
				int slot = slotEntry.getKey();
				for (Map.Entry<Trigger, ArrayList<PriorityAction>> trigEntry : slotEntry.getValue().entrySet()) {
					for (PriorityAction pa : trigEntry.getValue()) {
						if (pa instanceof EquipmentInstance) {
							// Skip if already added as a castable
							boolean alreadyAdded = false;
							for (DisableCandidate c : candidates) {
								if (c.action == pa) {
									alreadyAdded = true;
									break;
								}
							}
							if (alreadyAdded) continue;
						}
						candidates.add(new DisableCandidate(pa, trigEntry.getValue(), slot, true));
					}
				}
			}

			if (candidates.isEmpty()) return SkillResult.CONDITION_FAILED;

			// Pick a random candidate
			DisableCandidate chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));

			// Remove the specific PriorityAction from its list
			chosen.list.remove(chosen.action);

			// Save original item and set barrier
			ItemStack original = p.getInventory().getItem(chosen.invSlot);
			p.getInventory().setItem(chosen.invSlot, new ItemStack(Material.BARRIER));

			// Schedule revert
			long ticks = (long) (seconds * 20);
			DisableCandidate finalChosen = chosen;
			pdata.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					finalChosen.list.add(finalChosen.action);
					Player current = pdata.getPlayer();
					if (finalChosen.action instanceof EquipmentInstance) {
						((EquipmentInstance) finalChosen.action).updateIcon();
					} else {
						current.getInventory().setItem(finalChosen.invSlot, original);
					}
				}
			}.runTaskLater(NeoRogue.inst(), ticks));

			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
	}

	private static class DisableCandidate {
		final PriorityAction action;
		final ArrayList<PriorityAction> list;
		final int invSlot;
		final boolean isSlotBased;

		DisableCandidate(PriorityAction action, ArrayList<PriorityAction> list, int invSlot, boolean isSlotBased) {
			this.action = action;
			this.list = list;
			this.invSlot = invSlot;
			this.isSlotBased = isSlotBased;
		}
	}
}
