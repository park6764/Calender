package com.example.calendar;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalendarController {
    private LinkedList<Schedule> calendar = new LinkedList<>();

    @GetMapping("/")
    public String view() {
        return calendar.toString();
    }

    @GetMapping("/createSchedule")
    public String createSchedule(
        @RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startDate,
        @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endDate,
        @RequestParam(name = "name") String name
    ) {
        calendar.add(new Schedule(startDate, endDate, new LinkedList<>(), name, Optional.empty()));
        return "\"" + name + "\" 이(가) 추가되었습니다.";
    }

    // editMemo :: String -> Maybe String -> IO ()
    @GetMapping("/editMemo")
    public String editMemo(
        @RequestParam(name = "name") String name,
        @RequestParam(name = "memo", required = false) String memo
    ) {
        final var m = Optional.ofNullable(memo);

        final var newCal = filterAndReplace(s -> s.getName().equals(name), m, calendar);
        
        if(!newCal.isPresent()) return "\"" + name + "\"" + " 라는 이름의 일정이 존재하지 않습니다.";
        else {
            calendar = newCal.get();
            if(m.isPresent()) return "\"" + name + "\" 일정의 메모가 \"" + m.get() + "\" (으)로 수정되었습니다.";
            else return "\"" + name + "\" 일정의 메모가 \" 삭제되었습니다.";
        }
    }

    private Optional<LinkedList<Schedule>> filterAndReplace(Function<Schedule, Boolean> p, Optional<String> memo, LinkedList<Schedule> calendar) {
        if(calendar.isEmpty()) return Optional.of(calendar);
        else if(calendar.size() == 1 && !p.apply(calendar.get(0))) return Optional.empty();
        else {
            final LinkedList<Schedule> calendar_ = (LinkedList<Schedule>) calendar.clone();
            final var a = calendar_.removeFirst();
            
            if(p.apply(a)) {
                calendar_.addFirst(a.withMemo(memo)); // withMemo "memo"
                return Optional.of(calendar_);
            } else {
                final var ls = filterAndReplace(p, memo, calendar_);
                ls.ifPresent(l -> l.addFirst(a));
                return ls;
            }
        }
    }

    @GetMapping("/addNoti")
    public String addNoti(
        @RequestParam(name = "name") String name,
        @RequestParam(name = "days", required = false) Integer d,
        @RequestParam(name = "hours", required = false) Integer h,
        @RequestParam(name = "mins", required =  false) Integer m
    ) {
        final var d_ = Optional.ofNullable(d);
        final var h_ = Optional.ofNullable(h);
        final var m_ = Optional.ofNullable(m);

        if(d_.isEmpty() && h_.isEmpty() && m_.isEmpty()) throw new IllegalArgumentException("days, hours, mins 중 적어도 하나는 입력해야합니다.");
        else {
            final var days = Duration.ofDays(d_.orElse(0));
            final var hours = Duration.ofHours(h_.orElse(0));
            final var mins = Duration.ofMinutes(m_.orElse(0));

            final var result = days.plus(hours).plus(mins);

            final var c_ = findElem(s -> s.getName().equals(name), calendar);

            final var n = c_.orElseThrow(() -> new IllegalArgumentException("\"" + name + "\"" + "이름의 일정이 존재하지 않습니다."));
            calendar.remove(n);
            n.getNoties().add(n.getStartDate().minus(result));
            calendar.add(n);

            return "\"" + name + "\"" + "일정에 대해 " + n.getStartDate().minus(result) + "에 알림이 추가되었습니다.";
        }
    }

    private static <T> Optional<T> findElem(Function<T, Boolean> p, LinkedList<T> ls) {
        if(ls.isEmpty()) return Optional.empty();
        else {
            LinkedList<T> l = (LinkedList<T>) ls.clone();
            final var a = l.removeFirst();
            if(p.apply(a)) return Optional.of(a);
            else return findElem(p, l);
        }
    }

    private static <T> Optional<T> findElemLoop(Function<T, Boolean> p, LinkedList<T> ls) {
        Optional<T> result = Optional.empty();
        for (T t : ls) {
            if(p.apply(t)) return Optional.of(t);
            else continue;
        }
        return result;
    }

    @GetMapping("/delNoti")
    public String delNoti(
        @RequestParam(name = "name") String name,
        @RequestParam(name = "days", required = false) Integer d,
        @RequestParam(name = "hours", required = false) Integer h,
        @RequestParam(name = "mins", required =  false) Integer m
    ) {

        final var d_ = Optional.ofNullable(d);
        final var h_ = Optional.ofNullable(h);
        final var m_ = Optional.ofNullable(m);

        if(d_.isEmpty() && h_.isEmpty() && m_.isEmpty()) throw new IllegalArgumentException("days, hours, mins 중 적어도 하나는 입력해야합니다.");
        else {
            final var days = Duration.ofDays(d_.orElse(0));
            final var hours = Duration.ofHours(h_.orElse(0));
            final var mins = Duration.ofMinutes(m_.orElse(0));

            final var result = days.plus(hours).plus(mins);

            final var c_ = findElem(s -> s.getName().equals(name), calendar);

            final var n = c_.orElseThrow(() -> new IllegalArgumentException("\"" + name + "\"" + "이름의 일정이 존재하지 않습니다."));
            calendar.remove(n);
            n.getNoties().remove(n.getStartDate().minus(result));
            calendar.add(n);

            return "\"" + name + "\"" + "일정에 대해 " + n.getStartDate().minus(result) + "에 있던 알림이 삭제되었습니다.";
        }
    }

    @GetMapping("/editSchedule")
    public String editSchedule( // 수정
        @RequestParam(name = "startDateTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startDateTo,
        @RequestParam(name = "endDateTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endDateTo,
        @RequestParam(name = "name") String name,
        @RequestParam(name = "toName", required = false) String toName
    ) {
        final var n = Optional.ofNullable(toName);
        final var sd = Optional.ofNullable(startDateTo);
        final var ed = Optional.ofNullable(endDateTo);

        if(n.isEmpty() && sd.isEmpty() && ed.isEmpty()) throw new IllegalArgumentException("toName, startDateTo, endDateTo 중 적어도 하나는 입력해야합니다.");
        else {
            final Optional<Schedule> s = findElem(s_ -> s_.getName().equals(name), calendar);
            final var ss = s.orElseThrow(() -> new IllegalArgumentException("\"" + name + "\"" + "이름의 일정이 존재하지 않습니다."));

            final Schedule newSchedule;
            final int c = 4 * (n.isPresent() ? 1 : 0) + 2 * (sd.isPresent() ? 1 : 0) + (ed.isPresent() ? 1 : 0);

            switch (c) { //스케줄 객체 생성만,
                case 0b001:
                newSchedule = new Schedule(ss.getStartDate(), ed.get(), ss.getNoties(), name, ss.getMemo());
                break;
                case 0b010: 
                newSchedule = new Schedule(sd.get(), ss.getEndDate(), ss.getNoties(), name, ss.getMemo());
                break;
                case 0b011: 
                newSchedule = new Schedule(sd.get(), ed.get(), ss.getNoties(), name, ss.getMemo());
                break;
                case 0b100: 
                newSchedule = new Schedule(ss.getStartDate(), ss.getEndDate(), ss.getNoties(), n.get(), ss.getMemo());
                break;
                case 0b101: 
                newSchedule = new Schedule(ss.getStartDate(), ed.get(), ss.getNoties(), n.get(), ss.getMemo());
                break;
                case 0b110: 
                newSchedule = new Schedule(sd.get(), ss.getEndDate(), ss.getNoties(), n.get(), ss.getMemo());
                break;
                case 0b111: 
                newSchedule = new Schedule(sd.get(), ed.get(), ss.getNoties(), n.get(), ss.getMemo());
                break;
                default:
                    break;
            }
            // 삭제하고 값 추가하기!!!
            return "값이 있는 녀석을 반환하면서" + " 스케줄이 수정되었습니다.";
        }
    }

    @GetMapping("/deleteSchedule")
    public String deleteSchedule(
        @RequestParam(name = "name") String name
    ) {
        final var s = findElem(s_ -> s_.getName().equals(name), calendar);
        final var ss = s.orElseThrow(() -> new IllegalArgumentException("\"" + name + "\"" + " 이름의 스케줄이 존재하지 않습니다."));
        calendar.remove(ss);
        return "\"" + name + "\"" + " (이)가 삭제되었습니다.";
    }
}