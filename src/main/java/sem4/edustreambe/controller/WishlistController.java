package sem4.edustreambe.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.wishlist.WishlistItemResponse;
import sem4.edustreambe.service.WishlistService;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
public class WishlistController {

    WishlistService wishlistService;

    @GetMapping
    public ApiResponse<List<WishlistItemResponse>> getWishlistItems() {
        return ApiResponse.<List<WishlistItemResponse>>builder()
                .result(wishlistService.getWishlistItems())
                .build();
    }

    @PostMapping("/{courseId}")
    public ApiResponse<WishlistItemResponse> addToWishlist(@PathVariable String courseId) {
        return ApiResponse.<WishlistItemResponse>builder()
                .result(wishlistService.addToWishlist(courseId))
                .build();
    }

    @DeleteMapping("/{courseId}")
    public ApiResponse<Void> removeFromWishlist(@PathVariable String courseId) {
        wishlistService.removeFromWishlist(courseId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/count")
    public ApiResponse<Integer> getWishlistCount() {
        return ApiResponse.<Integer>builder()
                .result(wishlistService.getWishlistCount())
                .build();
    }
}
