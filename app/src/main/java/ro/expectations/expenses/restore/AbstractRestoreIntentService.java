/*
 * Copyright (c) 2016 Adrian Videnie
 *
 * This file is part of Expenses.
 *
 * Expenses is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Expenses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Expenses. If not, see <http://www.gnu.org/licenses/>.
 */

package ro.expectations.expenses.restore;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

import ro.expectations.expenses.data.IntegrityFixHelper;

public abstract class AbstractRestoreIntentService extends IntentService {

    public static final String ARG_FILE_URI = "arg_file_uri";

    protected static final String TAG = AbstractRestoreIntentService.class.getSimpleName();

    public AbstractRestoreIntentService() {
        super("RestoreIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.e(TAG, "onHandleIntent");

        String filePath = intent.getStringExtra(AbstractRestoreIntentService.ARG_FILE_URI);
        File file = new File(filePath);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            InputStream decompressedStream = decompressStream(inputStream);
            parse(decompressedStream);
        } catch(IOException e) {
            notifyFailure(e);
            return;
        }

        try {
            emptyDatabase();
            populateDatabase();
            new IntegrityFixHelper(this).fix();
        } catch(RuntimeException e) {
            notifyFailure(e);
            return;
        }

        notifySuccess();
    }

    protected InputStream decompressStream(InputStream input) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(input, 2);
        byte [] signature = new byte[2];
        pb.read(signature);
        pb.unread(signature);

        // check if the signature matches standard GZIP magic number
        int header = ((int) signature[0] & 0xff) | ((signature[1] << 8) & 0xff00);
        if (GZIPInputStream.GZIP_MAGIC == header) {
            return new GZIPInputStream(pb);
        } else {
            return pb;
        }
    }

    protected void emptyDatabase() {

        Log.i(TAG, "Finished emptying existing database");
    }

    protected void populateDatabase() {

        Log.i(TAG, "Finished populating the database with new entries");
    }

    protected abstract void parse(InputStream input) throws IOException;

    protected abstract void notifySuccess();

    protected abstract void notifyFailure(Exception e);
}
