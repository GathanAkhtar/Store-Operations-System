package Project;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.HashMap;

public class AttendanceLog {
    private HashMap<String, AttendanceRecord> records;
    private DateTimeFormatter dateFormatter;
    
    public AttendanceLog() {
        records = new HashMap<>();
        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }
    
    public String clockIn(Employee employee) {
        String today = getTodayDate();
        String key = employee.getUserID() + "_" + today;
        
        if (records.containsKey(key)) {
            return "ERROR: You have already clocked in today!\nPlease clock out first.";
        }
        
        AttendanceRecord newRecord = new AttendanceRecord(employee.getUserID(), employee.getName(), employee.getOutletId());
        records.put(key, newRecord);
        return newRecord.getClockInMessage();
    }
    
    public String clockOut(Employee employee) {
        String today = getTodayDate();
        String key = employee.getUserID() + "_" + today;
        
        if (!records.containsKey(key)) return "ERROR: You haven't clocked in yet today!";
        
        AttendanceRecord record = records.get(key);
        if (record.hasClockOut()) return "ERROR: You have already clocked out today!";
        
        record.clockOut();
        return record.getClockOutMessage();
    }
    
    public String checkStatus(Employee employee) {
        String today = getTodayDate();
        String key = employee.getUserID() + "_" + today;
        
        if (!records.containsKey(key)) return "Status: Not clocked in yet";
        AttendanceRecord record = records.get(key);
        return record.hasClockOut() ? "Status: Already clocked out" : "Status: Currently Clocked In";
    }
    
    private String getTodayDate() { return LocalDateTime.now().format(dateFormatter); }
    
    private class AttendanceRecord {
        private String employeeID, employeeName, outlet;
        private LocalDateTime clockInTime, clockOutTime;
        
        public AttendanceRecord(String id, String name, String outlet) {
            this.employeeID = id; this.employeeName = name; this.outlet = outlet;
            this.clockInTime = LocalDateTime.now();
        }
        
        public void clockOut() { this.clockOutTime = LocalDateTime.now(); }
        public boolean hasClockOut() { return clockOutTime != null; }
        
        public String getClockInMessage() {
            return "Clock In Successful for " + employeeName + " at " + clockInTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        }
        
        public String getClockOutMessage() {
            long minutes = Duration.between(clockInTime, clockOutTime).toMinutes();
            return "Clock Out Successful! Worked: " + (minutes/60.0) + " hours.";
        }
    }
}