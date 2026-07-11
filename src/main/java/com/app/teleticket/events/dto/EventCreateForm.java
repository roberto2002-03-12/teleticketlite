package com.app.teleticket.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.time.LocalDateTime;
import java.util.List;

public class EventCreateForm {

    @RestForm("title")
    @NotBlank
    @Size(max = 45)
    private String title;

    @RestForm("description")
    @NotBlank
    private String description;

    @RestForm("maxPeople")
    @NotNull
    @Positive
    private Integer maxPeople;

    @RestForm("address")
    @NotBlank
    @Size(max = 65)
    private String address;

    @RestForm("available")
    private boolean available;

    @RestForm("finished")
    private boolean finished;

    @RestForm("startDate")
    @NotNull
    private LocalDateTime startDate;

    @RestForm("finishDate")
    @NotNull
    private LocalDateTime finishDate;

    @RestForm("categoryId")
    @NotNull
    @Positive
    private Integer categoryId;

    @RestForm("photos")
    private List<FileUpload> photos;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxPeople() {
        return maxPeople;
    }

    public void setMaxPeople(Integer maxPeople) {
        this.maxPeople = maxPeople;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(LocalDateTime finishDate) {
        this.finishDate = finishDate;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public List<FileUpload> getPhotos() {
        return photos;
    }

    public void setPhotos(List<FileUpload> photos) {
        this.photos = photos;
    }
}