package com.progressionmod.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Entry point for Progression Overhaul's Jade integration.
 *
 * Registered via the "jade" entrypoint in fabric.mod.json.
 * Jade will call registerClient() only on the client side — safe for
 * client-only tooltip logic.
 */
@WailaPlugin
public class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        // No server-side data providers needed — all tier logic is block-state based
        // and can be evaluated on the client without a server round-trip.
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Register our provider for every block class (Object.class = universal).
        registration.registerBlockComponent(ProgressionToolProvider.INSTANCE, net.minecraft.block.Block.class);
    }
}
