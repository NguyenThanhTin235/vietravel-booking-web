package com.vietravel.booking.web.dto.tour;

import com.vietravel.booking.domain.entity.tour.DestinationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestinationResponse{
    private Long id;
    private String name;
    private String slug;
    private DestinationType type;
    private Boolean isActive;
    private Integer sortOrder;

    private Long categoryId;
    private String categoryName;
}
