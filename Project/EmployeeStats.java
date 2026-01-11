package Project;

public class EmployeeStats {
    private String employeeName;
    private int transactionCount;
    private double totalSales;

    public EmployeeStats(String name) {
        this.employeeName = name;
        this.transactionCount = 0;
        this.totalSales = 0.0;
    }

    public void addTransaction(double amount) {
        this.transactionCount++;
        this.totalSales += amount;
    }

    public String getEmployeeName() { return employeeName; }
    public int getTransactionCount() { return transactionCount; }
    public double getTotalSales() { return totalSales; }
}