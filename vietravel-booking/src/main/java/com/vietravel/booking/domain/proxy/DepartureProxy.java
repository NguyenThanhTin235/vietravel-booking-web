package com.vietravel.booking.domain.proxy;

import com.vietravel.booking.domain.entity.tour.Departure;

public interface DepartureProxy {
     boolean isAvailable(Departure departure, int totalRequest);

     String getUnavailableMessage();
}
