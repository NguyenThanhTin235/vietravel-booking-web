package com.vietravel.booking.domain.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "tours")
public class TourDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String code;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String summary;

    @Field(type = FieldType.Double)
    private BigDecimal basePrice;

    @Field(type = FieldType.Integer)
    private Integer durationDays;

    @Field(type = FieldType.Integer)
    private Integer durationNights;

    @Field(type = FieldType.Keyword)
    private String tourLineName;

    @Field(type = FieldType.Keyword)
    private String startLocationName;

    @Field(type = FieldType.Keyword)
    private String transportModeName;

    @Field(type = FieldType.Keyword)
    private String thumbnailUrl;

    @Field(type = FieldType.Long)
    private Long tourLineId;

    @Field(type = FieldType.Long)
    private Long transportModeId;

    @Field(type = FieldType.Long)
    private Long startLocationId;

    @Field(type = FieldType.Long)
    private List<Long> categoryIds;

    @Field(type = FieldType.Long)
    private List<Long> destinationIds;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    @Field(type = FieldType.Text, analyzer = "standard")
    private List<String> categoryNames;

    @Field(type = FieldType.Text, analyzer = "standard")
    private List<String> destinationNames;
}
