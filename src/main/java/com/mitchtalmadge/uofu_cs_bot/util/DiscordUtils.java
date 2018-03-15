package com.mitchtalmadge.uofu_cs_bot.util;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.restaction.order.OrderAction;

import java.util.List;

/**
 * Utility methods for working with the Discord API.
 */
public class DiscordUtils {

    /**
     * Orders the entities, with the provided order action, to their order in the provided list.
     * Submits the order to Discord.
     *
     * @param orderAction The order action to use.
     * @param order       The desired order of the entities.
     * @param <E>         The entity type.
     * @param <O>         The OrderAction type.
     */
    @SuppressWarnings("unchecked")
    public static <E, O extends OrderAction<? extends E, ? extends O>> void orderEntities(O orderAction, List<E> order) {
        // Order the channels.
        for (int i = 0; i < order.size(); i++) {
            // Find and select the current item being sorted.
            orderAction.selectPosition(orderAction.getCurrentOrder().indexOf(order.get(i)));

            // Move the item to its new location.
            orderAction.moveTo(i);
        }

        // Submit the changes to order.
        orderAction.complete();
    }

    /**
     * Determines if the right member has equal or higher roles to the left member.
     *
     * @param left  The left member.
     * @param right The right member.
     * @return True if the right member has equal or higher roles to the left member.
     */
    public static boolean hasEqualOrHigherRole(Member left, Member right) {
        // Owner always has higher power.
        if(left.isOwner())
            return false;
        if(right.isOwner())
            return true;

        // Get highest left role position.
        int highLeft = left.getRoles().stream().map(Role::getPosition).reduce(0, Integer::max);

        // Get highest right role position.
        int highRight = right.getRoles().stream().map(Role::getPosition).reduce(0, Integer::max);

        // Compare.
        return highRight >= highLeft;
    }

}
