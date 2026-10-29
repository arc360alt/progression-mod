package com.progressionmod.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Opens the quest book screen. Every player is given one the first time they join. */
public class QuestBookItem extends Item {

    /** Opens the screen on the client; set by the client initializer. */
    public static Runnable openScreen = () -> {};

    public QuestBookItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) openScreen.run();
        return InteractionResult.SUCCESS;
    }
}
