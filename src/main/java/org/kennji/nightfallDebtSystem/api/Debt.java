package org.kennji.nightfallDebtSystem.api;

import java.util.UUID;

public class Debt {
    private int debtID;
    private UUID borrowerUUID;
    private UUID lenderUUID;
    private double amount;
    private double remainingAmount;
    private long dueDate;
    private double interestRate;
    private boolean isPaid;

    public Debt() {}

    // getters and setters
    public int getDebtID() { return debtID; }
    public void setDebtID(int debtID) { this.debtID = debtID; }
    public UUID getBorrowerUUID() { return borrowerUUID; }
    public void setBorrowerUUID(UUID borrowerUUID) { this.borrowerUUID = borrowerUUID; }
    public UUID getLenderUUID() { return lenderUUID; }
    public void setLenderUUID(UUID lenderUUID) { this.lenderUUID = lenderUUID; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(double remainingAmount) { this.remainingAmount = remainingAmount; }
    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
}

