package com.mitchtalmadge.uofu_cs_bot.util;

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
            // Get the index of the current channel in the order list.
            int currentOrderPosition = orderAction.getCurrentOrder().indexOf(order.get(i));

            // Swap the current channel with the channel at the desired position.
            orderAction.selectPosition(currentOrderPosition).swapPosition(i);
        }

        // Submit the changes to order.
        orderAction.queue();
    }

}
