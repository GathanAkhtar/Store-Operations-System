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
    
    // Clock In
    public String clockIn(Employee employee) {
        String today = getTodayDate();
        String key = employee.getUserID() + "_" + today;
        
        if (records.containsKey(key)) {
            return "ERROR: You have already clocked in today!\nPlease clock out first.";
        }
        
        AttendanceRecord newRecord = new AttendanceRecord(
            employee.getUserID(),
            employee.getName(),
            employee.getOutletId()
        );
        
        records.put(key, newRecord);
        return newRecord.getClockInMessage();
    }
    
    // Clock Out
    public String clockOut(Employee employee) {
        String today = getTodayDate();
        String key = employee.getUserID() + "_" + today;
        
        if (!records.containsKey(key)) {
            return "ERROR: You haven't clocked in yet today!\nPlease clock in first.";
        }
        
        AttendanceRecord record = records.get(key);
        
        if (record.hasClockOut()) {
            return "ERROR: You have already clocked out today!";
        }
        
        record.clockOut();
        return record.getClockOutMessage();
    }
    
    // Check Status
    public String checkStatus(Employee employee) {
        String today = getTodayDate();
        String key = employee.getUserID() + "_" + today;
        
        if (!records.containsKey(key)) {
            return "Status: Not clocked in yet";
        }
        
        AttendanceRecord record = records.get(key);
        if (record.hasClockOut()) {
            return "Status: Already clocked out today";
        } else {
            return "Status: Clocked in, not yet clocked out";
        }
    }
    
    private String getTodayDate() {
        return LocalDateTime.now().format(dateFormatter);
    }
    
    // Inner Class: AttendanceRecord
    private class AttendanceRecord {
        private String employeeID;
        private String employeeName;
        private String outlet;
        private LocalDateTime clockInTime;
        private LocalDateTime clockOutTime;
        
        public AttendanceRecord(String employeeID, String employeeName, String outlet) {
            this.employeeID = employeeID;
            this.employeeName = employeeName;
            this.outlet = outlet;
            this.clockInTime = LocalDateTime.now();
            this.clockOutTime = null;
        }
        
        public void clockOut() {
            this.clockOutTime = LocalDateTime.now();
        }
        
        public boolean hasClockOut() {
            return clockOutTime != null;
        }
        
        public double calculateWorkingHours() {
            if (clockOutTime == null) {
                return 0.0;
            }
            
            Duration duration = Duration.between(clockInTime, clockOutTime);
            long minutes = duration.toMinutes();
            return minutes / 60.0;
        }
        
        public String getClockInMessage() {
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm a");
            
            String message = "=== Attendance Clock In ===\n\n";
            message += "Employee ID: " + employeeID + "\n\n";
            message += "Name: " + employeeName + "\n\n";
            message += "Outlet: " + outlet + "\n\n\n";
            message += "Clock In Successful!\n\n";
            message += "Date: " + clockInTime.format(dateFormat) + "\n\n";
            message += "Time: " + clockInTime.format(timeFormat);
            
            return message;
        }
        
        public String getClockOutMessage() {
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm a");
            
            String message = "=== Attendance Clock Out ===\n\n";
            message += "Employee ID: " + employeeID + "\n\n";
            message += "Name: " + employeeName + "\n\n";
            message += "Outlet: " + outlet + "\n\n\n";
            message += "Clock Out Successful!\n\n";
            message += "Date: " + clockOutTime.format(dateFormat) + "\n\n";
            message += "Time: " + clockOutTime.format(timeFormat) + "\n\n";
            message += "Total Hours Worked: " + String.format("%.1f", calculateWorkingHours()) + " hours";
            
            return message;
        }
    }
}