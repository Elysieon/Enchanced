package net.collective.enchanced.client.render.entity.thrown_spear;

import net.collective.enchanced.common.entity.ThrownSpearEntityHitbox;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class ThrownSpearEntityHitboxRenderer extends EntityRenderer<ThrownSpearEntityHitbox, ThrownSpearEntityHitboxState> {
    public ThrownSpearEntityHitboxRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public ThrownSpearEntityHitboxState createRenderState() {
        return new ThrownSpearEntityHitboxState();
    }
}
