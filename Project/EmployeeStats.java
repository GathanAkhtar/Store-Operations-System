package Project;

public class EmployeeStats implements Comparable<EmployeeStats> {
    private String employeeName;
    private double totalSales;
    private int transactionCount;

    public EmployeeStats(String name) {
        this.employeeName = name;
        this.totalSales = 0.0;
        this.transactionCount = 0;
    }

    public void addSale(double amount) {
        this.totalSales += amount;
        this.transactionCount++;
    }

    public String getEmployeeName() { return employeeName; }
    public double getTotalSales() { return totalSales; }
    public int getTransactionCount() { return transactionCount; }

    // Sorting Descending (Terbesar ke Terkecil)
    @Override
    public int compareTo(EmployeeStats other) {
        return Double.compare(other.totalSales, this.totalSales);
    }
}