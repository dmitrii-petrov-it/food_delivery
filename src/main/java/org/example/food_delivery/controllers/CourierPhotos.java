package org.example.food_delivery.controllers;

import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.food_delivery.model.user.Courier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class CourierPhotos {
    private static final int SIZE = 36;
    private static final String[] EXTENSIONS = {"jpg", "jpeg", "png"};
    private static final Color[] PALETTE = {
            Color.web("#ff7a59"), Color.web("#5b8cff"),
            Color.web("#34c759"), Color.web("#af52de"),
            Color.web("#ff9f0a"), Color.web("#0a84ff"),
            Color.web("#ff3b30"), Color.web("#5ac8fa")
    };
    private static final Map<String, Image> CACHE = new HashMap<>();

    private CourierPhotos() {}

    public static Image imageFor(Courier courier) {
        if (courier == null) {
            return null;
        }
        String key = cacheKey(courier);
        Image cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        Image loaded = loadFromBytes(courier.getPhoto());
        if (loaded == null) {
            loaded = loadBundled(courier);
        }
        if (loaded == null) {
            loaded = generateInitialsAvatar(courier);
        }
        CACHE.put(key, loaded);
        return loaded;
    }

    public static void invalidate(Integer courierId) {
        if (courierId == null) {
            return;
        }
        CACHE.remove("id:" + courierId);
    }

    public static void invalidateAll() {
        CACHE.clear();
    }

    private static Image loadFromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return new Image(new ByteArrayInputStream(bytes), SIZE * 2.0, SIZE * 2.0, true, true);
        } catch (Exception ex) {
            return null;
        }
    }

    public static int sizePx() {
        return SIZE;
    }

    private static final int BUNDLED_COUNT = 12;

    private static Image loadBundled(Courier courier) {
        Image direct = tryLoad(courier.getId());
        if (direct != null) {
            return direct;
        }
        return tryLoad(fallbackPhotoIndex(courier));
    }

    private static Image tryLoad(Integer index) {
        if (index == null) {
            return null;
        }
        for (String ext : EXTENSIONS) {
            InputStream stream = CourierPhotos.class.getResourceAsStream(
                    "/images/couriers/" + index + "." + ext);
            if (stream != null) {
                return new Image(stream, SIZE * 2.0, SIZE * 2.0, true, true);
            }
        }
        return null;
    }

    private static int fallbackPhotoIndex(Courier courier) {
        return Math.floorMod(seedFor(courier), BUNDLED_COUNT) + 1;
    }

    private static Image generateInitialsAvatar(Courier courier) {
        String initials = initials(courier.getFullName());
        Color background = PALETTE[Math.floorMod(seedFor(courier), PALETTE.length)];

        Canvas canvas = new Canvas(SIZE, SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(background);
        gc.fillOval(0, 0, SIZE, SIZE);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(initials, SIZE / 2.0, SIZE / 2.0 + 1);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    private static int seedFor(Courier courier) {
        if (courier.getId() != null) {
            return courier.getId();
        }
        String name = courier.getFullName();
        return name == null ? 0 : name.hashCode();
    }

    private static String initials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder out = new StringBuilder();
        if (parts[0].length() > 0) {
            out.append(Character.toUpperCase(parts[0].charAt(0)));
        }
        if (parts.length > 1 && parts[parts.length - 1].length() > 0) {
            out.append(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        }
        return out.length() == 0 ? "?" : out.toString();
    }

    private static String cacheKey(Courier courier) {
        if (courier.getId() != null) {
            return "id:" + courier.getId();
        }
        return "name:" + (courier.getFullName() == null ? "" : courier.getFullName());
    }
}
