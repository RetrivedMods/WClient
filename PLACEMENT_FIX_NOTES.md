# Placement fix

This revision aligns PistonCrystal/Surround placement with the decompiled latest WClient:
- cache a real outgoing ITEM_USE placement transaction as a template;
- clone that transaction for module placements so newer protocol fields are preserved;
- use the protocol BlockDefinition from the held ItemData directly;
- sanitize ItemData net-id before sending;
- rebuild server-authoritative inventory actions;
- keep local block state updated immediately.

The decompiled latest WClient Scaffold (`com.retrivedmods.wclient.game.module.misc.a1`)
was used as the behavioral reference.
