package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

/**
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 25/12/15.
 */
public class TodayWidgetIntentService extends IntentService {

    private static final String LOG_TAG = "TodayWidgetIntentSrv";

    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };

    public static final int COL_MATCHTIME = 0;
    public static final int COL_HOME = 1;
    public static final int COL_AWAY = 2;
    public static final int COL_HOME_GOALS = 3;
    public static final int COL_AWAY_GOALS = 4;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));


        // Get today's data from the ContentProvider
        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd");
        new_date.setTimeZone(TimeZone.getDefault());
        String today = new_date.format(new Date(System.currentTimeMillis()));

        String[] dates = new String[1];
        dates[0] = today;
        Uri scoresTodayUri = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor data = getContentResolver().query(
                scoresTodayUri, SCORES_COLUMNS, null, dates,
                DatabaseContract.scores_table.TIME_COL + " ASC");

        if (data == null) {
            return;
        }

        boolean hasData = data.moveToFirst();

        // Extract the data from the Cursor
        String time = "";
        String homeTeam = "";
        String awayTeam = "";
        String homeGoals = "";
        String awayGoals = "";

        if (hasData) {
            data = selectScore(data);
            time = data.getString(COL_MATCHTIME);
            homeTeam = data.getString(COL_HOME);
            awayTeam = data.getString(COL_AWAY);
            homeGoals = data.getString(COL_HOME_GOALS);
            awayGoals = data.getString(COL_AWAY_GOALS);
        }
        data.close();

        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int layoutId = R.layout.widget_today;
            if (widgetWidth < defaultWidth) {
                layoutId = R.layout.widget_today_small;
            }
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            if (hasData) {
                disableNoDataView(views);
                if (widgetWidth >= defaultWidth) {
                    views.setImageViewResource(R.id.widget_today_home_crest, Utilities.getTeamCrestByTeamName(homeTeam));
                    views.setImageViewResource(R.id.widget_today_away_crest, Utilities.getTeamCrestByTeamName(awayTeam));
                }
                // Content Descriptions for RemoteViews were only added in ICS MR1
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, homeTeam, awayTeam);
                }
                views.setTextViewText(R.id.widget_today_home_name, homeTeam);
                views.setTextViewText(R.id.widget_today_away_name, awayTeam);
                views.setTextViewText(R.id.widget_today_data_textview, time);
                views.setTextViewText(R.id.widget_away_score_textview, String.format("%s - %s",
                        homeGoals.equals("-1") ? "" : homeGoals,
                        awayGoals.equals("-1") ? "" : awayGoals));
            } else {
                enableNoDataView(views);
            }
            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_today_frame, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private void disableNoDataView(RemoteViews views) {
        views.setViewVisibility(R.id.widget_today_home_layout, View.VISIBLE);
        views.setViewVisibility(R.id.widget_today_data_layout, View.VISIBLE);
        views.setViewVisibility(R.id.widget_today_away_layout, View.VISIBLE);
        views.setViewVisibility(R.id.widget_today_no_data, View.GONE);
    }

    private void enableNoDataView(RemoteViews views) {
        views.setViewVisibility(R.id.widget_today_home_layout, View.GONE);
        views.setViewVisibility(R.id.widget_today_data_layout, View.GONE);
        views.setViewVisibility(R.id.widget_today_away_layout, View.GONE);
        views.setViewVisibility(R.id.widget_today_no_data, View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String homeTeamName, String awayTeamName) {
        views.setContentDescription(R.id.widget_today_home_crest, homeTeamName);
        views.setContentDescription(R.id.widget_today_away_crest, awayTeamName);
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }

    private Cursor selectScore(Cursor data) {

        return data;
    }
}
