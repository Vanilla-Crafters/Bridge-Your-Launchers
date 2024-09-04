package net.narheur.templatemod;

import net.fabricmc.api.ClientModInitializer;

public class TemplateModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("Hello Fabric world! (Client)");
    }
}