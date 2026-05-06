package sem4.edustreambe.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.cart.CartItemResponse;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
public class CartController {

    CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemResponse>> getCartItems() {
        return ApiResponse.<List<CartItemResponse>>builder()
                .result(cartService.getCartItems())
                .build();
    }

    @PostMapping("/{courseId}")
    public ApiResponse<CartItemResponse> addToCart(@PathVariable String courseId) {
        return ApiResponse.<CartItemResponse>builder()
                .result(cartService.addToCart(courseId))
                .build();
    }

    @DeleteMapping("/{courseId}")
    public ApiResponse<Void> removeFromCart(@PathVariable String courseId) {
        cartService.removeFromCart(courseId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/count")
    public ApiResponse<Integer> getCartCount() {
        return ApiResponse.<Integer>builder()
                .result(cartService.getCartCount())
                .build();
    }
}
