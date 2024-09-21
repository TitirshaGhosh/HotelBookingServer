package com.dailycodebuffer.lakesidehotel.controller;

import com.dailycodebuffer.lakesidehotel.exception.InvalidBookingRequestException;
import com.dailycodebuffer.lakesidehotel.exception.ResourceNotFoundException;
import com.dailycodebuffer.lakesidehotel.model.BookedRoom;
import com.dailycodebuffer.lakesidehotel.model.Room;
import com.dailycodebuffer.lakesidehotel.response.BookingResponse;
import com.dailycodebuffer.lakesidehotel.response.RoomResponse;
import com.dailycodebuffer.lakesidehotel.service.IBookingService;
import com.dailycodebuffer.lakesidehotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

//@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final IBookingService bookingService;
    private final IRoomService roomService;
//    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/all-bookings")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings(){
        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (BookedRoom booking : bookings){
            BookingResponse bookingResponse = getBookingResponse(booking);
            bookingResponses.add(bookingResponse);
        }
        return ResponseEntity.ok(bookingResponses);
    }
//
//@CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/room/{roomId}/booking")
    public ResponseEntity<?> saveBooking(@PathVariable Long roomId,
                                         @RequestBody BookedRoom bookingRequest){
        try{
            String confirmationCode = bookingService.saveBooking(roomId, bookingRequest);
            return ResponseEntity.ok(
                    "Room booked successfully, Your booking confirmation code is :"+confirmationCode);

        }catch (InvalidBookingRequestException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
//
//@CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable String confirmationCode){
        try{
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingResponse bookingResponse = getBookingResponse(booking);
            return ResponseEntity.ok(bookingResponse);
        }catch (ResourceNotFoundException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }
//    @CrossOrigin(origins = "http://localhost:5173")
@GetMapping("/user/{email}/bookings")
public ResponseEntity<List<BookingResponse>> getBookingsByUserEmail(@PathVariable String email) {
    List<BookedRoom> bookings = bookingService.getBookingsByUserEmail(email);
    List<BookingResponse> bookingResponses = new ArrayList<>();
    for (BookedRoom booking : bookings) {
        BookingResponse bookingResponse = getBookingResponse(booking);
        bookingResponses.add(bookingResponse);
    }
    return ResponseEntity.ok(bookingResponses);
}

//@CrossOrigin(origins = "http://localhost:5173")
    @DeleteMapping("/booking/{bookingId}/delete")
    public void cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
    }
//    @CrossOrigin(origins = "http://localhost:5173")
    private BookingResponse getBookingResponse(BookedRoom booking) {
        Room theRoom = roomService.getRoomById(booking.getRoom().getId()).get();
        RoomResponse room = new RoomResponse(
                theRoom.getId(),
                theRoom.getRoomType(),
                theRoom.getRoomPrice());
        return new BookingResponse(
                booking.getBookingId(), booking.getCheckInDate(),
                booking.getCheckOutDate(),booking.getGuestFullName(),
                booking.getGuestEmail(), booking.getNumOfAdults(),
                booking.getNumOfChildren(), booking.getTotalNumOfGuest(),
                booking.getBookingConfirmationCode(), room);
    }
}