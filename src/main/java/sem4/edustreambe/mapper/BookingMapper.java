package sem4.edustreambe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sem4.edustreambe.dto.booking.response.BookingResponse;
import sem4.edustreambe.dto.booking.response.EnrollmentResponse;
import sem4.edustreambe.entity.Booking;
import sem4.edustreambe.entity.Enrollment;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BookingMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "courseThumbnail", source = "course.thumbnailUrl")
    BookingResponse toBookingResponse(Booking booking);

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "courseThumbnail", source = "course.thumbnailUrl")
    EnrollmentResponse toEnrollmentResponse(Enrollment enrollment);
}
