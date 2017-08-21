package com.example.lonejourneyman.buoydownnofragment;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class QuickAddWidget extends AppWidgetProvider {


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

//        //CharSequence widgetText = context.getString(R.string.appwidget_text);
//        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.quick_add_widget);
//        //views.setTextViewText(R.id.appwidget_text, widgetText);
//

        Intent quickAddIntent = new Intent(context, QuickAddIntent.class);
        quickAddIntent.setAction(QuickAddIntent.ACTION_QUICK_ADD);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                0,
                quickAddIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        //
//        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

//        context.startService(new Intent(context, MainActivity.QuickAddIntentService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        //QuickAddIntent.startActionQuickAdd(context);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (QuickAddIntent.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, QuickAddIntent.class));
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

