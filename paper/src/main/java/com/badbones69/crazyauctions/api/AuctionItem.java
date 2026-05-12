package com.badbones69.crazyauctions.api;

import org.bukkit.inventory.ItemStack;

public class AuctionItem {

    private final String key;
    private final ItemStack item;
    private final int storeId;
    private final boolean biddable;
    private final String sellerUuid;
    private final String sellerName;
    private long price;
    private final long expireTime;
    private final long fullExpireTime;
    private String topBidder;
    private String topBidderName;

    public AuctionItem(String key, ItemStack item, int storeId, boolean biddable, String sellerUuid, String sellerName, long price, long expireTime, long fullExpireTime, String topBidder, String topBidderName) {
        this.key = key;
        this.item = item;
        this.storeId = storeId;
        this.biddable = biddable;
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        this.price = price;
        this.expireTime = expireTime;
        this.fullExpireTime = fullExpireTime;
        this.topBidder = topBidder;
        this.topBidderName = topBidderName;
    }

    public String getKey() {
        return key;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getStoreId() {
        return storeId;
    }

    public boolean isBiddable() {
        return biddable;
    }

    public String getSellerUuid() {
        return sellerUuid;
    }

    public String getSellerName() {
        return sellerName;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public long getFullExpireTime() {
        return fullExpireTime;
    }

    public String getTopBidder() {
        return topBidder;
    }

    public void setTopBidder(String topBidder) {
        this.topBidder = topBidder;
    }

    public String getTopBidderName() {
        return topBidderName;
    }

    public void setTopBidderName(String topBidderName) {
        this.topBidderName = topBidderName;
    }
}
