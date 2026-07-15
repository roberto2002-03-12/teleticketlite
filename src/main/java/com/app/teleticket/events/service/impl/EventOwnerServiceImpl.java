package com.app.teleticket.events.service.impl;

import com.app.teleticket.events.dto.EventCreateDTO;
import com.app.teleticket.events.dto.EventImageInput;
import com.app.teleticket.events.dto.EventResponseDTO;
import com.app.teleticket.events.dto.EventUpdateDTO;
import com.app.teleticket.events.entity.EventEntity;
import com.app.teleticket.events.entity.EventImageEntity;
import com.app.teleticket.events.exception.EventException;
import com.app.teleticket.events.repository.EventCategoryRepository;
import com.app.teleticket.events.repository.EventImageRepository;
import com.app.teleticket.events.repository.EventRepository;
import com.app.teleticket.events.service.EventImageStorageService;
import com.app.teleticket.events.service.EventOwnerService;
import com.app.teleticket.events.utils.EventMapper;
import com.app.teleticket.users.entity.EventOwnerEntity;
import com.app.teleticket.users.entity.UserEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.EventOwnerRepository;
import com.app.teleticket.users.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ApplicationScoped
public class EventOwnerServiceImpl implements EventOwnerService {

    private static final int MAX_IMAGES = 8;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    @Inject
    EventRepository eventRepository;

    @Inject
    EventImageRepository eventImageRepository;

    @Inject
    EventCategoryRepository eventCategoryRepository;

    @Inject
    EventOwnerRepository eventOwnerRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    EventImageStorageService imageStorage;

    @Inject
    EventMapper mapper;

    @Override
    @Transactional
    public EventResponseDTO create(String currentEmail, EventCreateDTO dto) {
        Integer ownerId = resolveEventOwnerId(currentEmail);

        if (dto.getPhotos() != null && dto.getPhotos().size() > MAX_IMAGES) {
            throw new EventException(400, "An event can have at most " + MAX_IMAGES + " images");
        }

        if (eventCategoryRepository.findById(dto.getCategoryId()).isEmpty()) {
            throw new EventException(404, "Category not found");
        }

        EventEntity event = new EventEntity();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setMaxPeople(dto.getMaxPeople());
        event.setAddress(dto.getAddress());
        event.setAvailable(dto.isAvailable());
        event.setFinished(dto.isFinished());
        event.setStartDate(dto.getStartDate());
        event.setFinishDate(dto.getFinishDate());
        event.setCategoryId(dto.getCategoryId());
        event.setOwnerId(ownerId);

        eventRepository.persist(event);
        eventRepository.flush();

        List<EventImageEntity> imageRows = uploadAndPersistImages(event, dto.getPhotos());
        return mapper.toResponse(event, imageRows);
    }

    @Override
    @Transactional
    public EventResponseDTO update(String currentEmail, Integer eventId, EventUpdateDTO dto) {
        Integer ownerId = resolveEventOwnerId(currentEmail);
        EventEntity event = mustFindOwned(eventId, ownerId);

        if (eventCategoryRepository.findById(dto.getCategoryId()).isEmpty()) {
            throw new EventException(404, "Category not found");
        }

        mapper.applyUpdate(event, dto);
        List<EventImageEntity> images = eventImageRepository.findByEventId(eventId);
        return mapper.toResponse(event, images);
    }

    @Override
    @Transactional
    public EventResponseDTO cancel(String currentEmail, Integer eventId) {
        Integer ownerId = resolveEventOwnerId(currentEmail);
        EventEntity event = mustFindOwned(eventId, ownerId);

        event.setAvailable(false);
        List<EventImageEntity> images = eventImageRepository.findByEventId(eventId);
        return mapper.toResponse(event, images);
    }

    @Override
    public List<EventResponseDTO> listOwn(String currentEmail) {
        Integer ownerId = resolveEventOwnerId(currentEmail);
        List<EventEntity> events = eventRepository.findByOwnerId(ownerId);
        return events.stream()
                .map(e -> mapper.toResponse(e, eventImageRepository.findByEventId(e.getId())))
                .toList();
    }

    @Override
    @Transactional
    public EventResponseDTO replaceImages(String currentEmail, Integer eventId, List<EventImageInput> photos) {
        Integer ownerId = resolveEventOwnerId(currentEmail);
        EventEntity event = mustFindOwned(eventId, ownerId);
        if (Objects.isNull(photos) || photos.isEmpty()) {
            throw new EventException(404, "No photos found");
        }

        if (photos.size() > MAX_IMAGES) {
            throw new EventException(400, "An event can have at most " + MAX_IMAGES + " images");
        }

        List<EventImageEntity> newRows = uploadNewImages(eventId, photos);
        List<String> newKeys = newRows.stream().map(EventImageEntity::getKeyName).toList();

        try {
            List<EventImageEntity> oldImages = eventImageRepository.findByEventId(eventId);
            eventImageRepository.deleteByEventId(eventId);

            for (EventImageEntity row : newRows) {
                eventImageRepository.persist(row);
                eventImageRepository.flush();
            }

            imageStorage.deleteAll(oldImages.stream().map(EventImageEntity::getKeyName).toList());
            return mapper.toResponse(event, newRows);
        } catch (RuntimeException e) {
            imageStorage.deleteAll(newKeys);
            throw e;
        }
    }

    @Override
    @Transactional
    public EventResponseDTO deleteImages(String currentEmail, Integer eventId, List<Integer> imagesId, boolean isAdmin) {
        EventEntity event = null;

        if (!isAdmin) {
            Integer ownerId = resolveEventOwnerId(currentEmail);
            event = mustFindOwned(eventId, ownerId);
        } else {
            event = eventRepository.findById(eventId).orElseThrow(() -> new EventException(404, "Event not found"));
        }

        if (imagesId == null || imagesId.isEmpty()) {
            throw new EventException(400, "imagesId list is required");
        }

        List<EventImageEntity> toDelete = eventImageRepository.findByEventIdAndIdIn(eventId, imagesId);
        if (toDelete.size() != imagesId.size()) {
            throw new EventException(404, "One or more images not found for this event");
        }

        List<String> keys = toDelete.stream().map(EventImageEntity::getKeyName).toList();
        eventImageRepository.deleteByEventIdAndIdIn(eventId, imagesId);
        imageStorage.deleteAll(keys);

        List<EventImageEntity> remaining = eventImageRepository.findByEventId(eventId);
        return mapper.toResponse(event, remaining);
    }

    private EventEntity mustFindOwned(Integer eventId, Integer ownerId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(404, "Event not found"));
        if (!event.getOwnerId().equals(ownerId)) {
            throw new EventException(403, "You can only manage events that you own");
        }
        return event;
    }

    private Integer resolveEventOwnerId(String currentEmail) {
        UserEntity user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserException(404, "User not found"));
        EventOwnerEntity owner = eventOwnerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EventException(403, "Current user is not an event owner"));
        return owner.getIdEventOwner();
    }

    private List<EventImageEntity> uploadAndPersistImages(EventEntity event, List<EventImageInput> photos) {
        if (photos == null || photos.isEmpty()) {
            return List.of();
        }
        List<EventImageEntity> rows = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();
        try {
            for (int i = 0; i < photos.size(); i++) {
                EventImageInput photo = photos.get(i);
                if (photo.bytes() == null || photo.bytes().length == 0) {
                    throw new EventException(400, "Empty image at index " + i);
                }
                if (photo.contentType() == null || !ALLOWED_CONTENT_TYPES.contains(photo.contentType())) {
                    throw new EventException(415, "Only jpg, jpeg and png images are allowed");
                }
                String url = imageStorage.upload(event.getId(), i, photo.contentType(), photo.bytes());
                String key = extractKey(url);

                EventImageEntity row = new EventImageEntity();
                row.setUrl(url);
                row.setKeyName(key);
                row.setIndex(i);
                row.setEventId(event.getId());
                eventImageRepository.persist(row);
                eventImageRepository.flush();
                rows.add(row);
                uploadedKeys.add(key);
            }
        } catch (RuntimeException e) {
            imageStorage.deleteAll(uploadedKeys);
            throw e;
        }
        return rows;
    }

    private List<EventImageEntity> uploadNewImages(Integer eventId, List<EventImageInput> photos) {
        if (photos == null || photos.isEmpty()) {
            return List.of();
        }
        List<EventImageEntity> rows = new ArrayList<>();
        for (int i = 0; i < photos.size(); i++) {
            EventImageInput photo = photos.get(i);
            if (photo.bytes() == null || photo.bytes().length == 0) {
                throw new EventException(400, "Empty image at index " + i);
            }
            if (photo.contentType() == null || !ALLOWED_CONTENT_TYPES.contains(photo.contentType())) {
                throw new EventException(415, "Only jpg, jpeg and png images are allowed");
            }
            String url = imageStorage.upload(eventId, i, photo.contentType(), photo.bytes());
            String key = extractKey(url);

            EventImageEntity row = new EventImageEntity();
            row.setUrl(url);
            row.setKeyName(key);
            row.setIndex(i);
            row.setEventId(eventId);
            rows.add(row);
        }
        return rows;
    }

    private String extractKey(String url) {
        int idx = url.indexOf(".amazonaws.com/");
        return idx < 0 ? url : url.substring(idx + ".amazonaws.com/".length());
    }
}