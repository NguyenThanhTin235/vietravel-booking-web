package com.vietravel.booking.web.dto.tour;

import com.vietravel.booking.domain.entity.tour.DestinationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestinationUpsertRequest{
    private String name;
    private String slug;
    private DestinationType type;
    private Long categoryId;
    private Integer sortOrder;
    private Boolean isActive;
}
