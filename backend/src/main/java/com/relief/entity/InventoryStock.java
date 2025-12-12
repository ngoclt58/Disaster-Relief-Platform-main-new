package com.relief.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_stock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private InventoryHub hub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ItemCatalog item;

    // Expose raw item_id so frontend can still perform actions even if the
    // ItemCatalog relationship is not serialized correctly
    @Column(name = "item_id", insertable = false, updatable = false)
    private UUID itemId;

    @Column(name = "qty_available")
    @Builder.Default
    private Integer qtyAvailable = 0;

    @Column(name = "qty_reserved")
    @Builder.Default
    private Integer qtyReserved = 0;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Explicit getters and setters for Lombok compatibility
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public InventoryHub getHub() { return hub; }
    public void setHub(InventoryHub hub) { this.hub = hub; }

    public ItemCatalog getItem() { return item; }
    public void setItem(ItemCatalog item) { this.item = item; }

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public Integer getQtyAvailable() { return qtyAvailable; }
    public void setQtyAvailable(Integer qtyAvailable) { this.qtyAvailable = qtyAvailable; }

    public Integer getQtyReserved() { return qtyReserved; }
    public void setQtyReserved(Integer qtyReserved) { this.qtyReserved = qtyReserved; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder pattern for Lombok compatibility
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private InventoryHub hub;
        private ItemCatalog item;
        private Integer qtyAvailable = 0;
        private Integer qtyReserved = 0;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder hub(InventoryHub hub) {
            this.hub = hub;
            return this;
        }

        public Builder item(ItemCatalog item) {
            this.item = item;
            return this;
        }

        public Builder qtyAvailable(Integer qtyAvailable) {
            this.qtyAvailable = qtyAvailable;
            return this;
        }

        public Builder qtyReserved(Integer qtyReserved) {
            this.qtyReserved = qtyReserved;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public InventoryStock build() {
            InventoryStock stock = new InventoryStock();
            stock.setId(id);
            stock.setHub(hub);
            stock.setItem(item);
            stock.setQtyAvailable(qtyAvailable);
            stock.setQtyReserved(qtyReserved);
            stock.setUpdatedAt(updatedAt);
            return stock;
        }
    }
}



