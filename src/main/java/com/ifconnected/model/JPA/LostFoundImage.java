package com.ifconnected.model.JPA;

import jakarta.persistence.*;

@Entity
@Table(name="lost_found_images")
public class LostFoundImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String url;

    @OneToOne
    @JoinColumn(name="item_id", nullable=false, unique=true)
    private LostFoundItem item;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LostFoundItem getItem() {
        return item;
    }

    public void setItem(LostFoundItem item) {
        this.item = item;
    }
}
