package org.sifacai.vlcjellyfin.Bean;

import java.util.List;

public class Items {
    private List<Item> Items;
    private int StartIndex;
    private int TotalRecordCount;

    public List<Item> getItems() {
        return Items;
    }

    public void setItems(List<Item> items) {
        Items = items;
    }

    public void AddItems(List<Item> items) {
        Items.addAll(items);
    }

    public int getStartIndex() {
        return StartIndex;
    }

    public void setStartIndex(int startIndex) {
        StartIndex = startIndex;
    }

    public int getTotalRecordCount() {
        return TotalRecordCount;
    }

    public void setTotalRecordCount(int totalRecordCount) {
        TotalRecordCount = totalRecordCount;
    }
}
