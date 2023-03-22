package com.example.calendar;


import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;

public class Schedule {
    private final LocalDateTime startDate;
    private final LocalDateTime endDate; 
    private final LinkedList<LocalDateTime> noties;
    private final String name;
    private final Optional<String> memo;

    public Schedule(LocalDateTime startDate, LocalDateTime endDate, LinkedList<LocalDateTime> noties, String name,
            Optional<String> memo) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.noties = noties;
        this.name = name;
        this.memo = memo;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public LinkedList<LocalDateTime> getNoties() {
        return noties;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getMemo() {
        return memo;
    }

    public Schedule withMemo(Optional<String> memo) {
        return new Schedule(startDate, endDate, noties, name, memo);
    }

    @Override
    public String toString() {
        return "Schedule { startDate=" + startDate + ", endDate=" + endDate + ", noties=" + noties + ", name=" + name
                + ", memo=" + memo + " }";
    }
}
