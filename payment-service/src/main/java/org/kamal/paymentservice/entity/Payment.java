package org.kamal.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long bookingId;

    @Column(nullable=false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private PaymentStatus paymentStatus;

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal amount;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist()
    {
        this.createdAt = LocalDateTime.now();
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }
}
