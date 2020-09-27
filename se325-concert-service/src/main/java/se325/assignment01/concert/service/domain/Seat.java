package se325.assignment01.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Seat {



	@Id
	@GeneratedValue
	private Long id;
	@Column(name = "LABEL")
	private String label;
	@Column(name = "PRICE")
	private BigDecimal price;
	@Column(name = "IS_BOOKED")
	private boolean isBooked;
	@Column(name = "DATE")
	private LocalDateTime date;

	// versioning for optimistic locking
	@Version
	private int version;

	public Seat() {}

	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal price) {
		this.label = label;
		this.isBooked = isBooked;
		this.date = date;
		this.price = price;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public boolean getIsBooked() { return isBooked; }

	public void setIsBooked(boolean isBooked) { this.isBooked = isBooked; }



}
