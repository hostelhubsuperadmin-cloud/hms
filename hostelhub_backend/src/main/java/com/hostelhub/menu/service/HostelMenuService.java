package com.hostelhub.menu.service;

import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.entity.HostelStatus;
import com.hostelhub.hostel.repository.HostelRepository;
import com.hostelhub.hosteladmin.repository.HostelAdminAssignmentRepository;
import com.hostelhub.menu.dto.HostelMenuItemRequest;
import com.hostelhub.menu.dto.HostelMenuItemResponse;
import com.hostelhub.menu.entity.HostelMenuItem;
import com.hostelhub.menu.repository.HostelMenuItemRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;

@Service
public class HostelMenuService {

    private static final List<String> MEAL_TYPES =
            List.of("BREAKFAST", "LUNCH", "DINNER");

    private final HostelMenuItemRepository menuRepository;
    private final HostelRepository hostelRepository;
    private final HostelAdminAssignmentRepository assignmentRepository;

    public HostelMenuService(
            HostelMenuItemRepository menuRepository,
            HostelRepository hostelRepository,
            HostelAdminAssignmentRepository assignmentRepository
    ) {
        this.menuRepository = menuRepository;
        this.hostelRepository = hostelRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<HostelMenuItemResponse> getPublicMenu(Long hostelId) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .filter(h -> h.getStatus() == HostelStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Public hostel not found"));

        return menuRepository
                .findAllByHostel_IdAndAvailableTrueOrderByDayOfWeekAscMealTypeAscNameAsc(
                        hostel.getId())
                .stream()
                .map(HostelMenuItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HostelMenuItemResponse> getAdminMenu(
            String email, Long hostelId
    ) {
        requireAssignment(email, hostelId);

        return menuRepository
                .findAllByHostel_IdOrderByDayOfWeekAscMealTypeAscNameAsc(hostelId)
                .stream()
                .map(HostelMenuItemResponse::from)
                .toList();
    }

    @Transactional
    public HostelMenuItemResponse create(
            String email,
            Long hostelId,
            HostelMenuItemRequest request
    ) {
        requireAssignment(email, hostelId);

        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Hostel not found"));

        Validated value = validate(request);

        HostelMenuItem item = new HostelMenuItem(
                hostel,
                value.dayOfWeek(),
                value.mealType(),
                value.name(),
                value.description(),
                value.price(),
                value.available()
        );

        return HostelMenuItemResponse.from(menuRepository.save(item));
    }

    @Transactional
    public HostelMenuItemResponse update(
            String email,
            Long hostelId,
            Long itemId,
            HostelMenuItemRequest request
    ) {
        requireAssignment(email, hostelId);

        HostelMenuItem item = menuRepository
                .findByIdAndHostel_Id(itemId, hostelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Menu item not found for this hostel"));

        Validated value = validate(request);

        item.update(
                value.dayOfWeek(),
                value.mealType(),
                value.name(),
                value.description(),
                value.price(),
                value.available()
        );

        return HostelMenuItemResponse.from(menuRepository.save(item));
    }

    @Transactional
    public void delete(String email, Long hostelId, Long itemId) {
        requireAssignment(email, hostelId);

        HostelMenuItem item = menuRepository
                .findByIdAndHostel_Id(itemId, hostelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Menu item not found for this hostel"));

        menuRepository.delete(item);
    }

    private void requireAssignment(String email, Long hostelId) {
        boolean assigned =
                assignmentRepository
                        .existsByUser_EmailIgnoreCaseAndHostel_Id(
                                email, hostelId);

        if (!assigned) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not assigned to manage this hostel");
        }
    }

    private Validated validate(HostelMenuItemRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Menu data is required");
        }

        DayOfWeek day;

        try {
            day = DayOfWeek.valueOf(
                    request.dayOfWeek().trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Select a valid day from Monday to Sunday");
        }

        if (request.mealType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Meal type is required");
        }

        String mealType =
                request.mealType().trim().toUpperCase(Locale.ROOT);

        if (!MEAL_TYPES.contains(mealType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Meal type must be BREAKFAST, LUNCH, or DINNER");
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Food item name is required");
        }

        String name = request.name().trim();

        if (name.length() > 120) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Food item name must be 120 characters or fewer");
        }

        BigDecimal price = request.price() == null
                ? BigDecimal.ZERO
                : request.price();

        if (price.compareTo(BigDecimal.ZERO) < 0
                || price.scale() > 2
                || price.precision() > 10) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Price must be non-negative with at most two decimal places");
        }

        String description = request.description() == null
                ? ""
                : request.description().trim();

        if (description.length() > 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Description must be 1000 characters or fewer");
        }

        return new Validated(
                day,
                mealType,
                name,
                description,
                price,
                request.available() == null || request.available()
        );
    }

    private record Validated(
            DayOfWeek dayOfWeek,
            String mealType,
            String name,
            String description,
            BigDecimal price,
            boolean available
    ) {}
}

