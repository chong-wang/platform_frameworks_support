/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.support.media.tv;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.RestrictTo;
import android.support.media.tv.TvContractCompat.WatchNextPrograms;
import android.support.media.tv.TvContractCompat.WatchNextPrograms.WatchNextType;
import android.text.TextUtils;

import java.util.Objects;

/**
 * A convenience class to access {@link WatchNextPrograms} entries in the system content
 * provider.
 *
 * <p>This class makes it easy to insert or retrieve a program from the system content provider,
 * which is defined in {@link TvContractCompat}.
 *
 * <p>Usage example when inserting a "watch next" program:
 * <pre>
 * WatchNextProgram watchNextProgram = new WatchNextProgram.Builder()
 *         .setWatchNextType(WatchNextPrograms.WATCH_NEXT_TYPE_CONTINUE)
 *         .setType(PreviewPrograms.TYPE_MOVIE)
 *         .setTitle("Program Title")
 *         .setDescription("Program Description")
 *         .setPosterArtUri(Uri.parse("http://example.com/poster_art.png"))
 *         // Set more attributes...
 *         .build();
 * Uri watchNextProgramUri = getContentResolver().insert(WatchNextPrograms.CONTENT_URI,
 *         watchNextProgram.toContentValues());
 * </pre>
 *
 * <p>Usage example when retrieving a "watch next" program:
 * <pre>
 * WatchNextProgram watchNextProgram;
 * try (Cursor cursor = resolver.query(watchNextProgramUri, null, null, null, null)) {
 *     if (cursor != null && cursor.getCount() != 0) {
 *         cursor.moveToNext();
 *         watchNextProgram = WatchNextProgram.fromCursor(cursor);
 *     }
 * }
 * </pre>
 */
@TargetApi(26)
public final class WatchNextProgram extends BasePreviewProgram {
    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public static final String[] PROJECTION = getProjection();

    private static final long INVALID_LONG_VALUE = -1;
    private static final int INVALID_INT_VALUE = -1;

    private final String mWatchNextType;
    private final long mLastEngagementTimeUtcMillis;

    private WatchNextProgram(Builder builder) {
        super(builder);
        mWatchNextType = builder.mWatchNextType;
        mLastEngagementTimeUtcMillis = builder.mLastEngagementTimeUtcMillis;
    }

    /**
     * @return The value of {@link WatchNextPrograms#COLUMN_WATCH_NEXT_TYPE} for the program.
     */
    public @WatchNextType String getWatchNextType() {
        return mWatchNextType;
    }

    /**
     * @return The value of {@link WatchNextPrograms#COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS} for the
     * program.
     */
    public long getLastEngagementTimeUtcMillis() {
        return mLastEngagementTimeUtcMillis;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WatchNextProgram)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        WatchNextProgram program = (WatchNextProgram) other;
        return Objects.equals(mWatchNextType, program.mWatchNextType)
                && mLastEngagementTimeUtcMillis == program.mLastEngagementTimeUtcMillis;
    }

    @Override
    public String toString() {
        return "Program{"
                + ", watchNextType=" + mWatchNextType
                + ", lastEngagementTimeUtcMillis=" + mLastEngagementTimeUtcMillis
                + "}";
    }

    /**
     * @return The fields of the Program in the ContentValues format to be easily inserted into the
     * TV Input Framework database.
     */
    public ContentValues toContentValues() {
        return toContentValues(false);
    }

    /**
     * Returns fields of the WatchNextProgram in the ContentValues format to be easily inserted
     * into the TV Input Framework database.
     *
     * @param includeProtectedFields Whether the fields protected by system is included or not.
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public ContentValues toContentValues(boolean includeProtectedFields) {
        ContentValues values = super.toContentValues(includeProtectedFields);
        if (!TextUtils.isEmpty(mWatchNextType)) {
            values.put(WatchNextPrograms.COLUMN_WATCH_NEXT_TYPE, mWatchNextType);
        }
        if (mLastEngagementTimeUtcMillis != INVALID_LONG_VALUE) {
            values.put(WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS,
                    mLastEngagementTimeUtcMillis);
        }
        return values;
    }

    /**
     * Creates a WatchNextProgram object from a cursor including the fields defined in
     * {@link WatchNextPrograms}.
     *
     * @param cursor A row from the TV Input Framework database.
     * @return A Program with the values taken from the cursor.
     */
    public static WatchNextProgram fromCursor(Cursor cursor) {
        // TODO: Add additional API which does not use costly getColumnIndex().
        Builder builder = new Builder();
        BasePreviewProgram.setFieldsFromCursor(cursor, builder);
        int index;
        if ((index = cursor.getColumnIndex(WatchNextPrograms.COLUMN_WATCH_NEXT_TYPE)) >= 0
                && !cursor.isNull(index)) {
            builder.setWatchNextType(cursor.getString(index));
        }
        if ((index = cursor.getColumnIndex(
                WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS)) >= 0
                && !cursor.isNull(index)) {
            builder.setLastEngagementTimeUtcMillis(cursor.getLong(index));
        }
        return builder.build();
    }

    private static String[] getProjection() {
        String[] oColumns = new String[] {
                WatchNextPrograms.COLUMN_WATCH_NEXT_TYPE,
                WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS,
        };
        return CollectionUtils.concatAll(BasePreviewProgram.PROJECTION, oColumns);
    }

    /**
     * This Builder class simplifies the creation of a {@link WatchNextProgram} object.
     */
    public static final class Builder extends BasePreviewProgram.Builder<Builder> {
        private String mWatchNextType;
        private long mLastEngagementTimeUtcMillis = INVALID_LONG_VALUE;

        /**
         * Creates a new Builder object.
         */
        public Builder() {
        }

        /**
         * Creates a new Builder object with values copied from another Program.
         * @param other The Program you're copying from.
         */
        public Builder(WatchNextProgram other) {
            super(other);
            mWatchNextType = other.mWatchNextType;
            mLastEngagementTimeUtcMillis = other.mLastEngagementTimeUtcMillis;
        }

        /**
         * Sets the "watch next" type of this program content.
         *
         * <p>The value should match one of the followings:
         * {@link WatchNextPrograms#WATCH_NEXT_TYPE_CONTINUE},
         * {@link WatchNextPrograms#WATCH_NEXT_TYPE_NEXT}, and
         * {@link WatchNextPrograms#WATCH_NEXT_TYPE_NEW}.
         *
         * @param watchNextType The value of {@link WatchNextPrograms#COLUMN_WATCH_NEXT_TYPE} for
         *                      the program.
         * @return This Builder object to allow for chaining of calls to builder methods.
         */
        public Builder setWatchNextType(@WatchNextType String watchNextType) {
            mWatchNextType = watchNextType;
            return this;
        }

        /**
         * Sets the time when the program is going to begin in milliseconds since the epoch.
         *
         * @param lastEngagementTimeUtcMillis The value of
         * {@link WatchNextPrograms#COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS} for the program.
         * @return This Builder object to allow for chaining of calls to builder methods.
         */
        public Builder setLastEngagementTimeUtcMillis(long lastEngagementTimeUtcMillis) {
            mLastEngagementTimeUtcMillis = lastEngagementTimeUtcMillis;
            return this;
        }

        /**
         * @return A new Program with values supplied by the Builder.
         */
        public WatchNextProgram build() {
            return new WatchNextProgram(this);
        }
    }
}
