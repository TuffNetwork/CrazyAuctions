package com.badbones69.crazyauctions;

import com.badbones69.crazyauctions.api.enums.Files;
import com.badbones69.crazyauctions.api.enums.Messages;
import com.badbones69.crazyauctions.api.enums.Reasons;
import com.badbones69.crazyauctions.api.events.AuctionCancelledEvent;
import com.badbones69.crazyauctions.api.events.AuctionExpireEvent;
import com.badbones69.crazyauctions.api.events.AuctionWinBidEvent;
import com.badbones69.crazyauctions.api.CrazyManager;
import com.badbones69.crazyauctions.api.AuctionItem;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Methods {

    private static final CrazyAuctions plugin = CrazyAuctions.get();
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"
    );


    public static String color(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of(matcher.group()).toString());
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static String getPrefix() {
        return color(Files.config.getConfiguration().getString("Settings.Prefix", ""));
    }

    public static String getPrefix(String msg) {
        return color(Files.config.getConfiguration().getString("Settings.Prefix", "") + msg);
    }


    public static ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    public static void setItemInHand(Player player, ItemStack item) {
        player.getInventory().setItemInMainHand(item);
    }

    public static boolean isInvFull(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }


    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isUUID(String uuid) {
        return uuid != null && UUID_PATTERN.matcher(uuid).matches();
    }



    public static @Nullable Player getPlayer(String uuid) {
        if (!isUUID(uuid)) return null;
        return plugin.getServer().getPlayer(UUID.fromString(uuid));
    }

    public static boolean isOnline(String uuid) {
        if (!isUUID(uuid)) return false;
        return plugin.getServer().getPlayer(UUID.fromString(uuid)) != null;
    }

    public static boolean isOnline(String name, CommandSender sender) {
        boolean online = plugin.getServer().getPlayerExact(name) != null;
        if (!online) sender.sendMessage(Messages.NOT_ONLINE.getMessage(sender));
        return online;
    }

    public static OfflinePlayer getOfflinePlayer(String uuid) {
        if (isUUID(uuid)) {
            return plugin.getServer().getOfflinePlayer(UUID.fromString(uuid));
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);
        return player;
    }


    public static boolean hasPermission(Player player, String perm) {
        if (!player.hasPermission("crazyauctions." + perm)) {
            player.sendMessage(Messages.NO_PERMISSION.getMessage(player));
            return false;
        }
        return true;
    }

    public static boolean hasPermission(CommandSender sender, String perm) {
        if (sender instanceof Player player) {
            return hasPermission(player, perm);
        }
        return true;
    }


    public static String toBase64(final ItemStack itemStack) {
        return Base64.getEncoder().encodeToString(itemStack.serializeAsBytes());
    }

    public static @NotNull ItemStack fromBase64(final String base64) {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(base64));
    }


    private static final int PAGE_SIZE = 45;

    public static List<ItemStack> getPage(List<ItemStack> list, int page) {
        if (page <= 0) page = 1;
        page = Math.min(page, getMaxPage(list)); // clamp to last valid page
        int from = Math.min((page - 1) * PAGE_SIZE, list.size());
        int to   = Math.min(from + PAGE_SIZE, list.size());
        return new ArrayList<>(list.subList(from, to));
    }

    public static List<Integer> getPageInts(List<Integer> list, int page) {
        if (page <= 0) page = 1;
        int maxPage = Math.max(1, (int) Math.ceil((double) list.size() / PAGE_SIZE));
        page = Math.min(page, maxPage);
        int from = Math.min((page - 1) * PAGE_SIZE, list.size());
        int to   = Math.min(from + PAGE_SIZE, list.size());
        return new ArrayList<>(list.subList(from, to));
    }

    public static int getMaxPage(List<ItemStack> list) {
        return Math.max(1, (int) Math.ceil((double) list.size() / PAGE_SIZE));
    }



    public static String convertToTime(long time) {
        long totalSeconds = (time - System.currentTimeMillis()) / 1000;
        if (totalSeconds < 0) totalSeconds = 0;

        long d = totalSeconds / 86400;
        long h = (totalSeconds % 86400) / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;

        return d + "d " + h + "h " + m + "m " + s + "s ";
    }

    public static long convertToMill(String time) {
        Calendar cal = Calendar.getInstance();
        for (String part : time.split(" ")) {
            String digits = part.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) continue;
            int value = Integer.parseInt(digits);
            String lower = part.toLowerCase(Locale.ROOT);
            if (lower.contains("d")) cal.add(Calendar.DATE,   value);
            else if (lower.contains("h")) cal.add(Calendar.HOUR,   value);
            else if (lower.contains("m")) cal.add(Calendar.MINUTE, value);
            else if (lower.contains("s")) cal.add(Calendar.SECOND, value);
        }
        return cal.getTimeInMillis();
    }


    public static void updateAuction() {
        CrazyManager crazyManager = plugin.getCrazyManager();
        FileConfiguration config = Files.config.getConfiguration();
        FileConfiguration data   = Files.data.getConfiguration();

        long now = System.currentTimeMillis();
        boolean shouldSave = false;

        List<AuctionItem> toRemove = new ArrayList<>();
        for (AuctionItem item : crazyManager.getAuctionItems()) {
            if (now > item.getExpireTime()) toRemove.add(item);
        }

        for (AuctionItem item : toRemove) {
            String i = item.getKey();
            long fullExpireTime = item.getFullExpireTime();

            int num = 1;
            while (data.contains("OutOfTime/Cancelled." + num)) num++;

            boolean bidWon = item.isBiddable()
                && !item.getTopBidder().equalsIgnoreCase("None")
                && plugin.getSupport().getMoney(getOfflinePlayer(item.getTopBidder())) >= item.getPrice();

            if (bidWon) {
                String winner = item.getTopBidder();
                String seller = item.getSellerUuid();
                long price    = item.getPrice();
                double taxRate = config.getDouble("Settings.Percent-Tax", 0);
                long taxAmount = (long) (price * taxRate / 100);
                long taxedPrice = Math.max(price - taxAmount, 0);

                OfflinePlayer sellerPlayer = getOfflinePlayer(seller);
                OfflinePlayer winnerPlayer = getOfflinePlayer(winner);

                plugin.getSupport().addMoney(sellerPlayer, taxedPrice);
                plugin.getSupport().removeMoney(winnerPlayer, price);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%price%",       String.valueOf(price));
                placeholders.put("%tax%",         String.valueOf(taxAmount));
                placeholders.put("%taxed_price%", String.valueOf(taxedPrice));
                placeholders.put("%player%",      winnerPlayer.getName());
                placeholders.put("%seller%",      sellerPlayer.getName());

                Player winnerOnline = getPlayer(winner);
                if (winnerOnline != null) {
                    new AuctionWinBidEvent(winnerOnline, item.getItem(), price).callEvent();
                    winnerOnline.sendMessage(Messages.WIN_BIDDING.getMessage(winnerOnline, placeholders));
                }

                Player sellerOnline = getPlayer(seller);
                if (sellerOnline != null) {
                    sellerOnline.sendMessage(Messages.SOMEONE_WON_PLAYERS_BID.getMessage(sellerOnline, placeholders));
                }

                data.set("OutOfTime/Cancelled." + num + ".Seller", winner);
            } else {
                String seller = item.getSellerUuid();
                Player sellerOnline = getPlayer(seller);

                if (sellerOnline != null) {
                    sellerOnline.sendMessage(Messages.ITEM_HAS_EXPIRED.getMessage(sellerOnline));
                }

                new AuctionExpireEvent(sellerOnline, item.getItem()).callEvent();

                data.set("OutOfTime/Cancelled." + num + ".Seller", seller);
            }

            data.set("OutOfTime/Cancelled." + num + ".Full-Time", fullExpireTime);
            data.set("OutOfTime/Cancelled." + num + ".StoreID",   item.getStoreId());
            data.set("OutOfTime/Cancelled." + num + ".Item",      toBase64(item.getItem()));
            data.set("Items." + i, null);
            crazyManager.removeAuctionItem(i);
            shouldSave = true;
        }

        if (data.contains("OutOfTime/Cancelled")) {
            for (String i : data.getConfigurationSection("OutOfTime/Cancelled").getKeys(false)) {
                if (now > data.getLong("OutOfTime/Cancelled." + i + ".Full-Time")) {
                    data.set("OutOfTime/Cancelled." + i, null);
                    shouldSave = true;
                }
            }
        }

        if (shouldSave) Files.data.saveAsync();
    }


    public static String getPrice(String ID, boolean expired) {
        FileConfiguration configuration = Files.data.getConfiguration();
        String path = expired
            ? "OutOfTime/Cancelled." + ID + ".Price"
            : "Items." + ID + ".Price";
        return String.valueOf(configuration.getLong(path, 0L));
    }

    public static int expireItem(int num, OfflinePlayer seller, String i, FileConfiguration data, Reasons reasons) {
        while (data.contains("OutOfTime/Cancelled." + num)) num++;

        new AuctionCancelledEvent(seller, fromBase64(data.getString("Items." + i + ".Item")), reasons).callEvent();

        data.set("OutOfTime/Cancelled." + num + ".Seller",    data.getString("Items." + i + ".Seller"));
        data.set("OutOfTime/Cancelled." + num + ".Full-Time", data.getLong("Items." + i + ".Full-Time"));
        data.set("OutOfTime/Cancelled." + num + ".StoreID",   data.getInt("Items." + i + ".StoreID"));
        data.set("OutOfTime/Cancelled." + num + ".Item",      data.getString("Items." + i + ".Item"));

        data.set("Items." + i, null);
        plugin.getCrazyManager().removeAuctionItem(i);

        return num;
    }
}

// ts could be fucked for eagler uuids
