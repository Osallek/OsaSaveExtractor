package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.country.Loan;

import java.time.LocalDate;

public class LoanDTO {

    private final LocalDate expiryDate;

    private final int amount;

    private final double interest;

    public LoanDTO(Loan loan) {
        this.expiryDate = loan.getExpiryDate();
        this.amount = loan.getAmount();
        this.interest = loan.getInterest();
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public int getAmount() {
        return amount;
    }

    public double getInterest() {
        return interest;
    }
}
