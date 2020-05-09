package com.borysov.dev.models;

import com.borysov.dev.models.base.Auditable;
import com.borysov.dev.properties.CommonProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "items")
@EqualsAndHashCode(callSuper = true, exclude = "prices")
@ToString(exclude = {"prices", "user"})
public class Item extends Auditable {

    private String url;

    private String name;

    @Column(length = 2048)
    private String picture;

    private LocalDateTime startDateTrack;

    private LocalDateTime lastDateUpdate;

    @OneToMany(mappedBy="item", cascade = CascadeType.REMOVE/*, fetch = FetchType.EAGER*/)
    private Set<Price> prices = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public String getStartPrices() {
     return getPrices().stream().sorted(Comparator.comparing(Price::getAbbreviation)).map( Price::getStartRateStr).collect(Collectors.joining("<br>\n"));
    }

    public String getCurrentPrices() {
     return getPrices().stream().sorted(Comparator.comparing(Price::getAbbreviation)).map( Price::getCurrentRateStr).collect(Collectors.joining("<br/>"));
    }

    public String getBitSkinsUrl() {
        return String.format(CommonProperties.BIT_SKINS_URL, URLEncoder.encode(getName()));
    }
}
