package de.melanx.datatrader.util;

import java.util.List;

public class ListEntryGetter<T> {

    private long ticksPassed = 0;
    private int currentIndex = 0;
    private T currentEntry;
    private final int duration;
    private final List<T> entries;

    public ListEntryGetter(List<T> list, int duration) {
        this.duration = duration;
        this.entries = list;
        if (!list.isEmpty()) {
            this.currentEntry = this.entries.get(this.currentIndex);
        }
    }

    public T getEntry() {
        return this.currentEntry;
    }

    public void tick() {
        if (this.entries.isEmpty()) {
            return;
        }

        this.ticksPassed++;
        if (this.currentIndex >= this.entries.size()) {
            this.currentIndex = 0;
        }

        if (this.ticksPassed >= this.duration) {
            this.currentEntry = this.entries.get(this.currentIndex);
            this.currentIndex++;
            this.ticksPassed = 0;
        }
    }
}
