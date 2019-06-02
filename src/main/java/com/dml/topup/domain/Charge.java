package com.dml.topup.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Ismael Sadeghi
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "charge")
public class Charge extends DomainEntity implements Comparable<Charge> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "subscriberNo")
    private Long subscriberNo;
    @Column(name = "amount")
    private int amount;
    @Column(name = "orderType")
    private int orderType;
    @Column(name = "method")
    private int method;
    @Column(name = "resNum")
    private int resNo;
    @Column(name = "responseCodeOrder")
    private int resCodeOrder;
    @Column(name = "responseCodeSubmit")
    private int resCodeSubmit;
    @Column(name = "messageId")
    private String messageId;
    @Column(name = "errorCode")
    private String errorCode;
    @Column(name = "postageDate")
    private Date postageDate;
    @Column(name = "requestDateTimeTopup")
    private Long requestDateTimeTopup;
    @Column(name = "requestDateTopup")
    private Long requestDateTopup;

    @Override
    public int compareTo(Charge charge) {
        Long code = charge.getId();
        return this.id.compareTo(code);
    }
}