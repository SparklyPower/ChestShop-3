package com.Acrobot.ChestShop.Listeners.Block;

import com.Acrobot.Breeze.Utils.BlockUtil;
import com.Acrobot.Breeze.Utils.StringUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.PreShopCreationEvent;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.UUIDs.NameManager;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import static com.Acrobot.ChestShop.Permission.OTHER_NAME_DESTROY;
import static com.Acrobot.ChestShop.Signs.ChestShopSign.NAME_LINE;

/**
 * @author Acrobot
 */
public class SignCreate implements Listener {

    @EventHandler(ignoreCancelled = true)
    public static void onSignChange(SignChangeEvent event) {
        Block signBlock = event.getBlock();

        if (!BlockUtil.isSign(signBlock)) {
            return;
        }

        Sign sign = (Sign) signBlock.getState();

        // Required due to checks below that we want to strip the color codes
        String[] strippedColorsLines = event.getLines();
        for (byte i = 0; i < strippedColorsLines.length && i < 4; ++i) {
            // Convert to color and then remove
            strippedColorsLines[i] = StringUtil.stripColourCodes(ChatColor.translateAlternateColorCodes('&', strippedColorsLines[i]));
        }

        if (ChestShopSign.isValid(strippedColorsLines) && !NameManager.canUseName(event.getPlayer(), OTHER_NAME_DESTROY, strippedColorsLines[NAME_LINE])) {
            event.setCancelled(true);
            sign.update();
            return;
        }

        String[] lines = StringUtil.stripColourCodes(strippedColorsLines);

        if (!ChestShopSign.isValidPreparedSign(lines)) {
            return;
        }

        PreShopCreationEvent preEvent = new PreShopCreationEvent(event.getPlayer(), sign, lines);
        ChestShop.callEvent(preEvent);

        if (preEvent.getOutcome().shouldBreakSign()) {
            event.setCancelled(true);
            signBlock.breakNaturally();
            return;
        }

        for (byte i = 0; i < preEvent.getSignLines().length && i < 4; ++i) {
            String line = preEvent.getSignLine(i);

            if (i == (byte) 0) {
                line = "§1" + line;
            }
            if (i == (byte) 2) {
                line = line.replace("B", "§aB").replace("S", "§4S").replace(":", "§0:");
            }
            if (i == (byte) 3) {
                line = "§9" + line;
            }

            event.setLine(i, line);
        }

        if (preEvent.isCancelled()) {
            return;
        }

        ShopCreatedEvent postEvent = new ShopCreatedEvent(preEvent.getPlayer(), preEvent.getSign(), uBlock.findConnectedContainer(preEvent.getSign()), preEvent.getSignLines(), preEvent.getOwnerAccount());
        ChestShop.callEvent(postEvent);
    }
}
