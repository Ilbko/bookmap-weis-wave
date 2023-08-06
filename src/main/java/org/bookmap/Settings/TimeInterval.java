package org.bookmap.Settings;

public record TimeInterval(String name, long seconds) {
    public TimeInterval(String name, long seconds) {
        this.name = name;
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        return name;
    }
}
