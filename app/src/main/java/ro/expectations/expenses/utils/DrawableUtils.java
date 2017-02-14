/*
 * Copyright (c) 2017 Adrian Videnie
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

package ro.expectations.expenses.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;

import ro.expectations.expenses.R;

public class DrawableUtils {

    public static Drawable tint(Context context, @DrawableRes int resId, @ColorRes int colorId) {
        return tint(context, AppCompatResources.getDrawable(context, resId), colorId);
    }

    public static Drawable tint(Context context, Drawable drawable, @ColorRes int colorId) {
        return tintWithColor(drawable, ContextCompat.getColor(context, colorId));
    }

    public static Drawable tintWithColor(Drawable drawable, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable).mutate();
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    @DrawableRes
    public static int getIdentifier(Context context, String iconName, @DrawableRes int defaultResourceId) {
        int iconResourceId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        if (iconResourceId == 0) {
            iconResourceId = defaultResourceId;
        }

        return iconResourceId;
    }

    @DrawableRes
    public static int getIdentifier(Context context, String iconName) {
        return getIdentifier(context, iconName, R.drawable.ic_question_mark_black_24dp);
    }
}
