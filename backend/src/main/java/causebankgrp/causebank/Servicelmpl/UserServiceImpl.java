package causebankgrp.causebank.Servicelmpl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import causebankgrp.causebank.Dto.OrganizationDTO.Response.ApiResponse;
import causebankgrp.causebank.Entity.User;
import causebankgrp.causebank.Enums.UserRole;
import causebankgrp.causebank.Exception.Auth_Authorize.ResourceNotFoundException;
import causebankgrp.causebank.Models.UserModel;
import causebankgrp.causebank.Repository.UserRepository;
import causebankgrp.causebank.Services.UserService;
import causebankgrp.causebank.Utils.Auth_Authorize.AuthenticationUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AuthenticationUtils authenticationUtils;

    @Override
    public ApiResponse<UserModel> getUser(UUID id) {
        try {
            return userRepository.findUserById(id)
                    .map(user -> new ApiResponse<>(
                            true,
                            "User found successfully",
                            mapToUserModel(user)))
                    .orElse(new ApiResponse<>(
                            false,
                            "User not found",
                            null));
        } catch (Exception e) {
            return new ApiResponse<>(
                    false,
                    "An error occurred: " + e.getMessage(),
                    null);
        }
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public ApiResponse<List<UserModel>> getAllUsers(int page, int size, String sortBy, String sortDir) {
        try {
            if (!authenticationUtils.isCurrentUserAdmin()) {
                throw new ResourceNotFoundException("only admin can get all users");
            }
            Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<User> userPage = userRepository.findAll(pageable);

            List<UserModel> userModels = userPage.getContent()
                    .stream()
                    .map(this::mapToUserModel)
                    .collect(Collectors.toList());

            return new ApiResponse<>(
                    true,
                    String.format("Retrieved %d users (Page %d of %d)",
                            userPage.getNumberOfElements(),
                            userPage.getNumber() + 1,
                            userPage.getTotalPages()),
                    userModels);
        } catch (Exception e) {
            return new ApiResponse<>(
                    false,
                    "An error occurred: " + e.getMessage(),
                    null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UserModel> updateUserById(UUID id, UserModel userModel) {
        try {
            if (!authenticationUtils.getCurrentAuthenticatedUserId().equals(id)) {
                throw new ResourceNotFoundException("You are not authorized to update this user");
            }
            return userRepository.findById(id)
                    .map(existingUser -> {
                        // Update fields
                        existingUser.setEmail(userModel.getEmail());
                        existingUser.setFirstName(userModel.getFirstName());
                        existingUser.setLastName(userModel.getLastName());
                        existingUser.setAvatarUrl(userModel.getAvatarUrl());
                        existingUser.setPhone(userModel.getPhone());
                        existingUser.setUpdatedAt(ZonedDateTime.now());

                        User updatedUser = userRepository.save(existingUser);
                        return new ApiResponse<>(
                                true,
                                "User updated successfully",
                                mapToUserModel(updatedUser));
                    })
                    .orElse(new ApiResponse<>(
                            false,
                            "User not found",
                            null));
        } catch (Exception e) {
            return new ApiResponse<>(
                    false,
                    "An error occurred: " + e.getMessage(),
                    null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(UUID id) {
        try {
            if (!authenticationUtils.getCurrentAuthenticatedUserId().equals(id)) {
                throw new ResourceNotFoundException("You are not authorized to delete this user");
            }
            return userRepository.findUserById(id)
                    .map(user -> {
                        userRepository.deleteById(id);
                        return new ApiResponse<Void>(
                                true,
                                "User deleted successfully",
                                null);
                    })
                    .orElse(new ApiResponse<Void>(
                            false,
                            "User not found",
                            null));
        } catch (Exception e) {
            return new ApiResponse<>(
                    false,
                    "An error occurred: " + e.getMessage(),
                    null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UserModel> updateUserRole(UUID id, String role) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            if (!authenticationUtils.getCurrentAuthenticatedUserId().equals(id)) {
                throw new ResourceNotFoundException("You are not authorized to update this user's role");
            }
            return userRepository.findUserById(id)
                    .map(user -> {
                        userRepository.updateUserRole(id, userRole);
                        user.setRole(userRole);
                        return new ApiResponse<>(
                                true,
                                "User role updated successfully",
                                mapToUserModel(user));
                    })
                    .orElse(new ApiResponse<>(
                            false,
                            "User not found",
                            null));
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(
                    false,
                    "Invalid role provided",
                    null);
        } catch (Exception e) {
            return new ApiResponse<>(
                    false,
                    "An error occurred: " + e.getMessage(),
                    null);
        }
    }

    @Override
    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    // Helper method to map User entity to UserModel
    private UserModel mapToUserModel(User user) {
        UserModel model = new UserModel();
        model.setId(user.getId());
        model.setEmail(user.getEmail());
        model.setFirstName(user.getFirstName());
        model.setLastName(user.getLastName());
        model.setRole(user.getRole());
        model.setAvatarUrl(user.getAvatarUrl());
        model.setPhone(user.getPhone());
        model.setPasswordHash(user.getPasswordHash());
        model.setIsEmailVerified(user.getIsEmailVerified());
        model.setIsPhoneVerified(user.getIsPhoneVerified());
        model.setIsActive(user.getIsActive());
        model.setLastLoginAt(user.getLastLoginAt());
        model.setCreatedAt(user.getCreatedAt());
        model.setUpdatedAt(user.getUpdatedAt());
        return model;
    }
}
