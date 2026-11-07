package org.schabi.newpipe.local.holder;

import android.text.TextUtils;
import android.view.ViewGroup;

import org.schabi.newpipe.R;
import org.schabi.newpipe.database.LocalItem;
import org.schabi.newpipe.database.playlist.model.PlaylistRemoteEntity;
import org.schabi.newpipe.local.LocalItemBuilder;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import org.schabi.newpipe.util.Localization;
import org.schabi.newpipe.util.ServiceHelper;
import org.schabi.newpipe.util.image.CoilHelper;

import java.time.format.DateTimeFormatter;

public class RemotePlaylistItemHolder extends PlaylistItemHolder {

    public RemotePlaylistItemHolder(final LocalItemBuilder infoItemBuilder,
            final ViewGroup parent) {
        super(infoItemBuilder, parent);
    }

    RemotePlaylistItemHolder(final LocalItemBuilder infoItemBuilder, final int layoutId,
            final ViewGroup parent) {
        super(infoItemBuilder, layoutId, parent);
    }

    @Override
    public void updateFromItem(final LocalItem localItem,
                               final HistoryRecordManager historyRecordManager,
                               final DateTimeFormatter dateTimeFormatter) {
        if (!(localItem instanceof PlaylistRemoteEntity item)) {
            return;
        }

        itemTitleView.setText(item.getOrderingName());
        final Long totalCount = item.getStreamCount();
        final Long unwatchedCount = item.getUnwatchedCount();

        if (totalCount != null) {
            if (unwatchedCount != null && unwatchedCount > 0 && unwatchedCount < totalCount) {
                itemStreamCountView.setText(itemStreamCountView.getContext()
                        .getString(R.string.playlist_unwatched_count_format,
                                unwatchedCount, totalCount));
            } else {
                itemStreamCountView.setText(Localization.localizeStreamCountMini(
                        itemStreamCountView.getContext(), totalCount));
            }
        } else {
            itemStreamCountView.setText("");
        }
        // Here is where the uploader name is set in the bookmarked playlists library
        if (!TextUtils.isEmpty(item.getUploader())) {
            itemUploaderView.setText(Localization.concatenateStrings(item.getUploader(),
                    ServiceHelper.getNameOfServiceById(item.getServiceId())));
        } else {
            itemUploaderView.setText(ServiceHelper.getNameOfServiceById(item.getServiceId()));
        }

        CoilHelper.INSTANCE.loadPlaylistThumbnail(itemThumbnailView, item.getThumbnailUrl());

        super.updateFromItem(localItem, historyRecordManager, dateTimeFormatter);
    }
}
