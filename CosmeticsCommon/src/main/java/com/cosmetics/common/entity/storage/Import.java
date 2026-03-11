package com.cosmetics.common.entity.storage;

import com.cosmetics.common.entity.IdBasedEntity;
import com.cosmetics.common.entity.User;
import com.cosmetics.common.entity.product.ProductVariant;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "import")
public class Import extends IdBasedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "transaction_time", nullable = false, updatable = false)
    private Date transactionTime;

    @OneToMany(mappedBy = "importField", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImportDetail> details = new ArrayList<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public List<ImportDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ImportDetail> details) {
        this.details = details;
    }

    public void addDetail(ProductVariant variant, Integer quantity, Float cost){
        this.details.add(new ImportDetail(variant, quantity, cost,this));
    }

    @Formula("(SELECT COALESCE(SUM(d.cost * d.quantity), 0) " +
            "FROM import_details d " +
            "WHERE d.import_id = {alias}.id)")
    private Float sumCost;

    public Float getSumCost() {
        return sumCost != null ? sumCost : 0F;
    }

    public Import() {
    }

    @Override
    public String toString() {
        return "Import{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", transactionTime=" + transactionTime +
                "}";
    }
}
