package com.Acrobot.ChestShop.Listeners.Block;

import com.Acrobot.Breeze.Utils.BlockUtil;
import com.Acrobot.Breeze.Utils.StringUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.PreShopCreationEvent;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * @author Acrobot
 */
public class SignCreate implements Listener {

    @EventHandler(ignoreCancelled = true)
    public static void onSignChange(SignChangeEvent event) {
        Block signBlock = event.getBlock();
        String[] line = StringUtil.stripColourCodes(event.getLines());

        if (!BlockUtil.isSign(signBlock)) {
            return;
        }

        if (!ChestShopSign.isValidPreparedSign(line)) {
            return;
        }

        PreShopCreationEvent preEvent = new PreShopCreationEvent(event.getPlayer(), (Sign) signBlock.getState(), line);
        ChestShop.callEvent(preEvent);

        preEvent.setSignLine((byte)0, "§1" + preEvent.getSignLine((byte)0));
        preEvent.setSignLine((byte)2, preEvent.getSignLine((byte)2).replace("B", "§aB").replace("S", "§4S").replace(":", "§0:"));
        preEvent.setSignLine((byte)3, "§9" + preEvent.getSignLine((byte)3));

        for (byte i = 0; i < event.getLines().length; ++i) {
            event.setLine(i, preEvent.getSignLine(i));
        }

        if (preEvent.isCancelled()) {
            return;
        }

        ShopCreatedEvent postEvent = new ShopCreatedEvent(preEvent.getPlayer(), preEvent.getSign(), uBlock.findConnectedContainer(preEvent.getSign()), preEvent.getSignLines());
        ChestShop.callEvent(postEvent);
    }
}
