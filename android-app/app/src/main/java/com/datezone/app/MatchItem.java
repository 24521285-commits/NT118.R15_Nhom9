package com.datezone.app;

public class MatchItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_CARD = 1;

    private final int type;
    private String headerTitle;

    private String name;
    private String age;
    private String details;
    private String distance;
    private int photoResId;
    private int[] photos;

    // Header constructor
    public MatchItem(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    // Card constructor
    public MatchItem(String name, String age, String details, String distance, int photoResId, int[] photos) {
        this.type = TYPE_CARD;
        this.name = name;
        this.age = age;
        this.details = details;
        this.distance = distance;
        this.photoResId = photoResId;
        this.photos = photos;
    }

    public int getType() {
        return type;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getDetails() {
        return details;
    }

    public String getDistance() {
        return distance;
    }

    public int getPhotoResId() {
        return photoResId;
    }

    public int[] getPhotos() {
        return photos;
    }
}
