package sem4.edustreambe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sem4.edustreambe.dto.booking.response.BookingItemResponse;
import sem4.edustreambe.dto.booking.response.BookingResponse;
import sem4.edustreambe.dto.booking.response.EnrollmentResponse;
import sem4.edustreambe.entity.Booking;
import sem4.edustreambe.entity.BookingItem;
import sem4.edustreambe.entity.Enrollment;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingResponse toBookingResponse(Booking booking);

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "courseThumbnail", source = "course.thumbnailUrl")
    BookingItemResponse toBookingItemResponse(BookingItem item);

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "courseThumbnail", source = "course.thumbnailUrl")
    EnrollmentResponse toEnrollmentResponse(Enrollment enrollment);
}
