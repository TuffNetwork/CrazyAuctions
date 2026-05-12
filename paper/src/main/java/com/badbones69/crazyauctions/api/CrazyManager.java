package com.badbones69.crazyauctions.api;

import com.badbones69.crazyauctions.Methods;
import com.badbones69.crazyauctions.api.enums.Files;
import com.badbones69.crazyauctions.api.enums.ShopType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;

public class CrazyManager {

    private boolean sellingEnabled;
    private boolean biddingEnabled;

    private final java.util.Map<String, AuctionItem> auctionItems = new java.util.HashMap<>();
    private final java.util.Map<Integer, String> storeIdToKey = new java.util.HashMap<>();

    public void load() {
        this.sellingEnabled = Files.config.getConfiguration().getBoolean("Settings.Feature-Toggle.Selling", true);
        this.biddingEnabled = Files.config.getConfiguration().getBoolean("Settings.Feature-Toggle.Bidding", true);

        loadAuctionItems();
    }

    public void loadAuctionItems() {
        this.auctionItems.clear();
        this.storeIdToKey.clear();

        FileConfiguration data = Files.data.getConfiguration();
        if (data.contains("Items")) {
            for (String key : data.getConfigurationSection("Items").getKeys(false)) {
                try {
                    String itemBase64 = data.getString("Items." + key + ".Item");
                    if (itemBase64 == null) continue;

                    ItemStack item = Methods.fromBase64(itemBase64);
                    int storeId = data.getInt("Items." + key + ".StoreID");
                    boolean biddable = data.getBoolean("Items." + key + ".Biddable");
                    String sellerUuid = data.getString("Items." + key + ".Seller");
                    String sellerName = data.getString("Items." + key + ".SellerName");
                    long price = data.getLong("Items." + key + ".Price");
                    long expireTime = data.getLong("Items." + key + ".Time-Till-Expire");
                    long fullExpireTime = data.getLong("Items." + key + ".Full-Time");
                    String topBidder = data.getString("Items." + key + ".TopBidder");
                    String topBidderName = data.getString("Items." + key + ".TopBidderName");

                    AuctionItem auctionItem = new AuctionItem(key, item, storeId, biddable, sellerUuid, sellerName, price, expireTime, fullExpireTime, topBidder, topBidderName);
                    this.auctionItems.put(key, auctionItem);
                    this.storeIdToKey.put(storeId, key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addAuctionItem(AuctionItem item) {
        this.auctionItems.put(item.getKey(), item);
        this.storeIdToKey.put(item.getStoreId(), item.getKey());
    }

    public void removeAuctionItem(String key) {
        AuctionItem item = this.auctionItems.remove(key);
        if (item != null) {
            this.storeIdToKey.remove(item.getStoreId());
        }
    }

    public AuctionItem getAuctionItem(String key) {
        return this.auctionItems.get(key);
    }

    public AuctionItem getAuctionItemByStoreId(int storeId) {
        String key = this.storeIdToKey.get(storeId);
        return key != null ? this.auctionItems.get(key) : null;
    }

    public java.util.Collection<AuctionItem> getAuctionItems() {
        return this.auctionItems.values();
    }

    public void unload() {
        Files.data.save();
    }
    
    public boolean isSellingEnabled() {
        return this.sellingEnabled;
    }
    
    public boolean isBiddingEnabled() {
        return this.biddingEnabled;
    }
    
    public ArrayList<ItemStack> getItems(Player player, ShopType type) {
        ArrayList<ItemStack> items = new ArrayList<>();
        String playerUuid = player.getUniqueId().toString();

        for (AuctionItem auctionItem : this.auctionItems.values()) {
            if (auctionItem.getSellerUuid().equalsIgnoreCase(playerUuid)) {
                if (auctionItem.isBiddable()) {
                    if (type == ShopType.BID) {
                        items.add(auctionItem.getItem());
                    }
                } else {
                    if (type == ShopType.SELL) {
                        items.add(auctionItem.getItem());
                    }
                }
            }
        }

        return items;
    }
}