package vine.vine.service;

import java.time.LocalDateTime;

public interface ChargesService {
  //  Page<ChargesResponse> getAllCharges(Pageable pageable);
   // String getPrisonerCharges();
//    String getPrisonerChargesByBookingId(Long bookId);
    String processBookings(LocalDateTime lastRunTime);
}
