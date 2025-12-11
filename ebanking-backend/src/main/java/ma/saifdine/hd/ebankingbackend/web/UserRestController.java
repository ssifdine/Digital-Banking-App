package ma.saifdine.hd.ebankingbackend.web;

import lombok.AllArgsConstructor;
import ma.saifdine.hd.ebankingbackend.dtos.ChangePasswordRequestDTO;
import ma.saifdine.hd.ebankingbackend.dtos.ResetPasswordAdminDTO;
import ma.saifdine.hd.ebankingbackend.dtos.UserRequestDTO;
import ma.saifdine.hd.ebankingbackend.dtos.UserResponseDTO;
import ma.saifdine.hd.ebankingbackend.dtos.response.ApiResponse;
import ma.saifdine.hd.ebankingbackend.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserRestController {

    private final UserService userService;

    @PostMapping("/createUser")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@RequestBody UserRequestDTO dto) {
        UserResponseDTO createdUser = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("User created successfully", createdUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/change-password")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequestDTO request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        userService.changePassword(username, request);
        return ResponseEntity.ok("Password change successfully");
    }

    @PatchMapping("/admin/{id}/reset-password")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<String> adminResetPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordAdminDTO request) {

        userService.adminResetPassword(id, request);
        return ResponseEntity.ok("Password reset by admin successfully");
    }


}

