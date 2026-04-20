package com.vietravel.booking.domain.entity.tour;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tours")
public class Tour {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(nullable = false, unique = true, length = 30)
     private String code;

     @Column(nullable = false, length = 300)
     private String title;

     @Column(nullable = false, unique = true, length = 350)
     private String slug;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "tour_line_id", nullable = false)
     private TourLine tourLine;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "transport_mode_id", nullable = false)
     private TransportMode transportMode;

     @Column(name = "duration_days", nullable = false)
     private Integer durationDays;

     @Column(name = "duration_nights", nullable = false)
     private Integer durationNights;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "start_location_id")
     private Destination startLocation;

     @Column(name = "base_price", precision = 12, scale = 2)
     private BigDecimal basePrice;

     @Column(columnDefinition = "TEXT")
     private String summary;

     @Lob
     @Column(name = "overview_html")
     private String overviewHtml;

     @Lob
     @Column(name = "additional_info_html")
     private String additionalInfoHtml;

     @Lob
     @Column(name = "notes_html")
     private String notesHtml;

     @Column(name = "is_active", nullable = false)
     private Boolean isActive = true;

     @Column(name = "created_at", insertable = false, updatable = false)
     private LocalDateTime createdAt;

     @Column(name = "updated_at", insertable = false, updatable = false)
     private LocalDateTime updatedAt;

     @ManyToMany
     @JoinTable(name = "tour_category_map", joinColumns = @JoinColumn(name = "tour_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
     private Set<TourCategory> categories = new HashSet<>();

     @ManyToMany
     @JoinTable(name = "tour_destinations", joinColumns = @JoinColumn(name = "tour_id"), inverseJoinColumns = @JoinColumn(name = "destination_id"))
     private Set<Destination> destinations = new HashSet<>();

     @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
     @OrderBy("sortOrder ASC, id ASC")
     private List<TourImage> images = new ArrayList<>();

     @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
     @OrderBy("sortOrder ASC, dayNo ASC")
     private List<ItineraryDay> itineraryDays = new ArrayList<>();

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public String getCode() {
          return code;
     }

     public void setCode(String code) {
          this.code = code;
     }

     public String getTitle() {
          return title;
     }

     public void setTitle(String title) {
          this.title = title;
     }

     public String getSlug() {
          return slug;
     }

     public void setSlug(String slug) {
          this.slug = slug;
     }

     public TourLine getTourLine() {
          return tourLine;
     }

     public void setTourLine(TourLine tourLine) {
          this.tourLine = tourLine;
     }

     public TransportMode getTransportMode() {
          return transportMode;
     }

     public void setTransportMode(TransportMode transportMode) {
          this.transportMode = transportMode;
     }

     public Integer getDurationDays() {
          return durationDays;
     }

     public void setDurationDays(Integer durationDays) {
          this.durationDays = durationDays;
     }

     public Integer getDurationNights() {
          return durationNights;
     }

     public void setDurationNights(Integer durationNights) {
          this.durationNights = durationNights;
     }

     public Destination getStartLocation() {
          return startLocation;
     }

     public void setStartLocation(Destination startLocation) {
          this.startLocation = startLocation;
     }

     public BigDecimal getBasePrice() {
          return basePrice;
     }

     public void setBasePrice(BigDecimal basePrice) {
          this.basePrice = basePrice;
     }

     public String getSummary() {
          return summary;
     }

     public void setSummary(String summary) {
          this.summary = summary;
     }

     public String getOverviewHtml() {
          return overviewHtml;
     }

     public void setOverviewHtml(String overviewHtml) {
          this.overviewHtml = overviewHtml;
     }

     public String getAdditionalInfoHtml() {
          return additionalInfoHtml;
     }

     public void setAdditionalInfoHtml(String additionalInfoHtml) {
          this.additionalInfoHtml = additionalInfoHtml;
     }

     public String getNotesHtml() {
          return notesHtml;
     }

     public void setNotesHtml(String notesHtml) {
          this.notesHtml = notesHtml;
     }

     public Boolean getIsActive() {
          return isActive;
     }

     public void setIsActive(Boolean isActive) {
          this.isActive = isActive;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public LocalDateTime getUpdatedAt() {
          return updatedAt;
     }

     public Set<TourCategory> getCategories() {
          return categories;
     }

     public void setCategories(Set<TourCategory> categories) {
          this.categories = categories;
     }

     public Set<Destination> getDestinations() {
          return destinations;
     }

     public void setDestinations(Set<Destination> destinations) {
          this.destinations = destinations;
     }

     public List<TourImage> getImages() {
          return images;
     }

     public void setImages(List<TourImage> images) {
          this.images = images;
     }

     public List<ItineraryDay> getItineraryDays() {
          return itineraryDays;
     }

     public void setItineraryDays(List<ItineraryDay> itineraryDays) {
          this.itineraryDays = itineraryDays;
     }
}
