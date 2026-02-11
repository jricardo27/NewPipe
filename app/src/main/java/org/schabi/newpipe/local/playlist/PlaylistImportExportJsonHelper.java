package org.schabi.newpipe.local.playlist;

import androidx.annotation.Nullable;

import com.grack.nanojson.JsonAppendableWriter;
import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.BuildConfig;
import org.schabi.newpipe.database.stream.model.StreamEntity;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.local.subscription.services.ImportExportEventListener;

import java.time.OffsetDateTime;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public final class PlaylistImportExportJsonHelper {

    private static final String JSON_APP_VERSION_KEY = "app_version";
    private static final String JSON_APP_VERSION_INT_KEY = "app_version_int";

    private static final String JSON_PLAYLISTS_ARRAY_KEY = "playlists";
    private static final String JSON_PLAYLIST_NAME_KEY = "name";
    private static final String JSON_PLAYLIST_ITEMS_KEY = "items";

    // Stream Entity Keys
    private static final String JSON_STREAM_SERVICE_ID_KEY = "service_id";
    private static final String JSON_STREAM_URL_KEY = "url";
    private static final String JSON_STREAM_TITLE_KEY = "title";
    private static final String JSON_STREAM_TYPE_KEY = "stream_type";
    private static final String JSON_STREAM_DURATION_KEY = "duration";
    private static final String JSON_STREAM_UPLOADER_KEY = "uploader";
    private static final String JSON_STREAM_UPLOADER_URL_KEY = "uploader_url";
    private static final String JSON_STREAM_THUMBNAIL_URL_KEY = "thumbnail_url";
    private static final String JSON_STREAM_PROGRESS_MILLIS_KEY = "progress_millis";
    private static final String JSON_STREAM_TEXTUAL_UPLOAD_DATE_KEY = "textual_upload_date";
    private static final String JSON_STREAM_UPLOAD_DATE_KEY = "upload_date";
    private static final String JSON_STREAM_IS_UPLOAD_DATE_APPROXIMATION_KEY = "is_upload_date_approximation";

    private PlaylistImportExportJsonHelper() {
    }

    public static class StreamExportEntry {
        public StreamEntity streamEntity;
        public long progressMillis;

        public StreamExportEntry(StreamEntity streamEntity, long progressMillis) {
            this.streamEntity = streamEntity;
            this.progressMillis = progressMillis;
        }
    }

    public static class PlaylistWithStreams {
        public String name;
        public List<StreamExportEntry> streams;

        public PlaylistWithStreams(String name, List<StreamExportEntry> streams) {
            this.name = name;
            this.streams = streams;
        }
    }

    public static List<PlaylistWithStreams> readFrom(
            final InputStream in, @Nullable final ImportExportEventListener eventListener)
            throws Exception {
        if (in == null) {
            throw new IllegalArgumentException("input is null");
        }

        final List<PlaylistWithStreams> playlists = new ArrayList<>();

        final JsonObject parentObject = JsonParser.object().from(in);

        if (!parentObject.has(JSON_PLAYLISTS_ARRAY_KEY)) {
            throw new IllegalArgumentException("Playlists array is null");
        }

        final JsonArray playlistsArray = parentObject.getArray(JSON_PLAYLISTS_ARRAY_KEY);

        if (eventListener != null) {
            eventListener.onSizeReceived(playlistsArray.size());
        }

        for (final Object o : playlistsArray) {
            if (o instanceof JsonObject) {
                final JsonObject playlistObject = (JsonObject) o;
                final String name = playlistObject.getString(JSON_PLAYLIST_NAME_KEY);
                final JsonArray itemsArray = playlistObject.getArray(JSON_PLAYLIST_ITEMS_KEY);

                final List<StreamExportEntry> streams = new ArrayList<>();
                if (itemsArray != null) {
                    for (Object itemObj : itemsArray) {
                        if (itemObj instanceof JsonObject) {
                            JsonObject item = (JsonObject) itemObj;

                            String uploadDateStr = item.getString(JSON_STREAM_UPLOAD_DATE_KEY);
                            OffsetDateTime uploadDate = null;
                            if (uploadDateStr != null) {
                                try {
                                    uploadDate = OffsetDateTime.parse(uploadDateStr);
                                } catch (Exception ignored) {
                                }
                            }

                            StreamEntity stream = new StreamEntity(
                                    0L,
                                    item.getInt(JSON_STREAM_SERVICE_ID_KEY),
                                    item.getString(JSON_STREAM_URL_KEY),
                                    item.getString(JSON_STREAM_TITLE_KEY),
                                    StreamType.valueOf(item.getString(JSON_STREAM_TYPE_KEY)),
                                    item.getLong(JSON_STREAM_DURATION_KEY),
                                    item.getString(JSON_STREAM_UPLOADER_KEY),
                                    item.getString(JSON_STREAM_UPLOADER_URL_KEY),
                                    item.getString(JSON_STREAM_THUMBNAIL_URL_KEY),
                                    null,
                                    item.getString(JSON_STREAM_TEXTUAL_UPLOAD_DATE_KEY),
                                    uploadDate,
                                    item.getBoolean(JSON_STREAM_IS_UPLOAD_DATE_APPROXIMATION_KEY));
                            long progress = item.getLong(JSON_STREAM_PROGRESS_MILLIS_KEY, 0);
                            streams.add(new StreamExportEntry(stream, progress));
                        }
                    }
                }

                if (name != null && !name.isEmpty()) {
                    playlists.add(new PlaylistWithStreams(name, streams));
                    if (eventListener != null) {
                        eventListener.onItemCompleted(name);
                    }
                }
            }
        }

        return playlists;
    }

    public static void writeTo(final List<PlaylistWithStreams> playlists, final OutputStream out,
            @Nullable final ImportExportEventListener eventListener) {
        final JsonAppendableWriter writer = JsonWriter.on(out);
        writeTo(playlists, writer, eventListener);
        writer.done();
    }

    public static void writeTo(final List<PlaylistWithStreams> playlists,
            final JsonAppendableWriter writer,
            @Nullable final ImportExportEventListener eventListener) {
        if (eventListener != null) {
            eventListener.onSizeReceived(playlists.size());
        }

        writer.object();

        writer.value(JSON_APP_VERSION_KEY, BuildConfig.VERSION_NAME);
        writer.value(JSON_APP_VERSION_INT_KEY, BuildConfig.VERSION_CODE);

        writer.array(JSON_PLAYLISTS_ARRAY_KEY);
        for (final PlaylistWithStreams playlist : playlists) {
            writer.object();
            writer.value(JSON_PLAYLIST_NAME_KEY, playlist.name);

            writer.array(JSON_PLAYLIST_ITEMS_KEY);
            for (StreamExportEntry entry : playlist.streams) {
                StreamEntity stream = entry.streamEntity;
                writer.object();
                writer.value(JSON_STREAM_SERVICE_ID_KEY, stream.getServiceId());
                writer.value(JSON_STREAM_URL_KEY, stream.getUrl());
                writer.value(JSON_STREAM_TITLE_KEY, stream.getTitle());
                writer.value(JSON_STREAM_TYPE_KEY, stream.getStreamType().name());
                writer.value(JSON_STREAM_DURATION_KEY, stream.getDuration());
                writer.value(JSON_STREAM_UPLOADER_KEY, stream.getUploader());
                writer.value(JSON_STREAM_UPLOADER_URL_KEY, stream.getUploaderUrl());
                writer.value(JSON_STREAM_THUMBNAIL_URL_KEY, stream.getThumbnailUrl());
                writer.value(JSON_STREAM_PROGRESS_MILLIS_KEY, entry.progressMillis);

                if (stream.getTextualUploadDate() != null) {
                    writer.value(JSON_STREAM_TEXTUAL_UPLOAD_DATE_KEY, stream.getTextualUploadDate());
                }
                if (stream.getUploadDate() != null) {
                    writer.value(JSON_STREAM_UPLOAD_DATE_KEY, stream.getUploadDate().toString());
                }
                if (stream.isUploadDateApproximation() != null) {
                    writer.value(JSON_STREAM_IS_UPLOAD_DATE_APPROXIMATION_KEY, stream.isUploadDateApproximation());
                }

                writer.end();
            }
            writer.end();
            writer.end();

            if (eventListener != null) {
                eventListener.onItemCompleted(playlist.name);
            }
        }
        writer.end();

        writer.end();
    }
}
